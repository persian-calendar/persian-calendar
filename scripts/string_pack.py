# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import argparse
import collections
import logging
import os
import re
import sys
from typing import Dict, List, Set
from xml.etree import ElementTree

from id_finder import IdFinder

# This must be kept in sync with the `quantityIndex()` method in ParsedStringPack.java
_IDS_FOR_QUANTITY = {"other": 0, "zero": 1, "one": 2, "two": 3, "few": 4, "many": 5}


def normalize_locale(android_config_name):
    if re.match("^[a-z]{2}$", android_config_name):  # xx
        return android_config_name
    elif re.match("^[a-z]{2}-r[A-Z]{2}$", android_config_name):  # xx-rYY
        return android_config_name[:2] + "-" + android_config_name[-2:]
    elif re.match("^b\+[a-z]{2}\+[A-Z][a-z]{3}$", android_config_name):  # b+xx+Zzzz
        return android_config_name[2:4] + "-" + android_config_name[5:]
    else:
        raise NotImplementedError()


def extract_locale_from_file_name(file_name):
    escaped_sep = re.escape(os.path.sep)
    match = re.search(escaped_sep + "values-(.*)" + escaped_sep, file_name)
    assert match is not None
    return normalize_locale(match.group(1))


def unescape(text) -> str:
    if not text:
        return ""
    if len(text) >= 2 and text.startswith('"') and text.endswith('"'):
        return text[1:-1]  # Strip the quotation marks
    else:
        return text.replace(r"\'", "'").replace(r"\"", '"').replace(r"\n", "\n")


class TreeBuilderWithComments(ElementTree.TreeBuilder):
    COMMENT_TAG = "comment"

    def comment(self, data):
        # Comment with 'generated' is put by the script, we can skip it.
        if "\u0040generated" in data:
            return

        self.start(self.COMMENT_TAG, {})
        self.data(data)
        self.end(self.COMMENT_TAG)


def read_string_dict(
    locale, file_name, id_finder, plural_handler, nullify_res_ids: Set = set()
):
    result_dict = {}
    try:
        root = ElementTree.parse(
            file_name, parser=ElementTree.XMLParser(target=TreeBuilderWithComments())
        ).getroot()
    except FileNotFoundError:
        # Missing files are OK. They just mean no strings.
        return result_dict

    last_comment = ""
    for element in root:
        tag = element.tag

        if tag == TreeBuilderWithComments.COMMENT_TAG:
            # See if the comments include any metadata about plurals that we need to pass on.
            last_comment = element.text
            continue

        assert tag in ["string", "plurals"]
        string_name = element.attrib["name"]
        id = id_finder.get_id(string_name)
        if id is None:
            # No integer ID was found for the string. The string was most probably removed,
            # but still remains in the translations (such strings will be cleaned up next time
            # move_strings_for_packing.py is run).
            # TODO: T164393760 Handle 'No integer ID was found' cases
            continue
        if element.tag == "string":
            if f"R.string.{string_name}" in nullify_res_ids:
                continue
            result_dict[id] = unescape(element.text)
        else:  # plurals
            plural_dict = {}
            if f"R.plurals.{string_name}" in nullify_res_ids:
                continue
            for item in element:
                assert item.tag == "item"
                quantity = item.attrib["quantity"]
                if plural_handler(locale, last_comment, quantity):
                    continue
                quantity_id = _IDS_FOR_QUANTITY[quantity]
                plural_dict[quantity_id] = unescape(item.text)
            result_dict[id] = plural_dict
    return result_dict


def blob_append_32_bit(blob, integer):
    assert 0 <= integer < 2**31
    blob.append(integer & 0xFF)
    blob.append((integer & 0xFF00) >> 8)
    blob.append((integer & 0xFF0000) >> 16)
    blob.append((integer & 0xFF000000) >> 24)


def blob_append_16_bit(blob, integer):
    assert 0 <= integer < 2**15
    blob.append(integer & 0xFF)
    blob.append((integer & 0xFF00) >> 8)


def blob_append_locale(blob, locale):
    assert len(locale) in [2, 5, 7]
    blob += locale.encode("ASCII")
    if len(locale) == 2:
        blob += b"\0\0\0\0\0"
    elif len(locale) == 5:
        blob += b"\0\0"


class StringBuffer(object):
    "A large byte buffer that just holds strings."

    def __init__(self, encoding):
        self.encoding = encoding
        self.store = bytearray()

    def add(self, string_or_plural):
        if type(string_or_plural) is dict:  # Plural
            result = {}
            for quantity_id, string in string_or_plural.items():
                result[quantity_id] = self.add_string(string)
            return result
        else:
            return self.add_string(string_or_plural)

    def add_string(self, string):
        string_bytes = string.encode(encoding=self.encoding)
        bytes_len = len(string_bytes)
        if bytes_len == 0:  # empty string
            return 0, 0
        location = self.store.find(string_bytes)
        if location == -1:
            # Not found. But before trying to add it, see if a prefix of the new string
            # is at the end of the store buffer. If that's the case, we can save a few bytes
            # by sharing that.
            prefix = bytearray(string_bytes[:-1])
            while prefix and not self.store.endswith(prefix):
                del prefix[-1]
            if prefix:
                # Some part of the prefix remains, which means it matches the end of the buffer.
                start = len(self.store) - len(prefix)
                self.store += string_bytes[len(prefix) :]
            else:
                # Add the string to the end of the buffer.
                start = len(self.store)
                self.store += string_bytes
            return start, bytes_len
        else:
            return location, bytes_len


class LocaleStore(object):
    def __init__(self):
        self.strings = {}
        self.plurals = {}

    def add_plural_or_string(self, id, plural_or_string):
        if type(plural_or_string) is dict:
            self.add_plural(id, plural_or_string)
        else:
            self.add_string(id, plural_or_string)

    def add_string(self, id, string):
        assert id not in self.strings
        self.strings[id] = string

    def add_plural(self, id, plural):
        assert id not in self.plurals
        self.plurals[id] = plural

    def get_binary_blob(self):
        blob = bytearray()
        blob_append_16_bit(blob, len(self.strings))
        blob_append_16_bit(blob, len(self.plurals))
        # Write the strings. Note that the parser in ParsedStringPack.java expects this to be
        # sorted by ID.
        # However the ids are already entered in sorted manner. So no need to re-sort them
        for id in self.strings:
            blob_append_16_bit(blob, id)
            start, length = self.strings[id]
            blob_append_32_bit(blob, start)
            blob_append_16_bit(blob, length)
        # Write the plurals
        for id in self.plurals:
            blob_append_16_bit(blob, id)
            plural = self.plurals[id]
            blob.append(len(plural))  # Just one byte
            for quantity_id in sorted(plural):
                blob.append(quantity_id)  # Just one byte
                start, length = plural[quantity_id]
                blob_append_32_bit(blob, start)
                blob_append_16_bit(blob, length)
        return bytes(blob)


# Keep in sync with `ENCODINGS` in ParsedStringPack.java
_ENCODING_ID = {"UTF-8": 0, "UTF-16BE": 1}
# Keep in sync with `_ENCODING_ID`
_ENCODING_INDEX = {0: "UTF-8", 1: "UTF-16BE"}

# 2 bytes for number of locales, 4 bytes for starting index of locale data, 1 byte for the encoding
# of string data, and 4 bytes for starting index of the string data. Totalling 11 bytes.
_HEADER_SIZE = 11

# Each locale takes 11 bytes, right after the header. 7 bytes for the locale itself
# (see blob_append_locale), and 4 bytes for a pointer to where its table starts in
# file.
_LOCALE_HEADER_SIZE = 11


def _write_to_list(text_to_write: str, output_list: List) -> None:
    if output_list is not None:
        output_list.append(text_to_write)


def _read(content: bytearray, offset: int, length: int = 1) -> int:
    return int.from_bytes(content[offset : offset + length], "little")


def _read_locale_from(content: bytearray, offset: int) -> str:
    if _read(content, offset + 2) == 0:
        length = 2
    elif _read(content, offset + 5) == 0:
        length = 5
    else:
        length = 7
    return content[offset : offset + length].decode("ascii")


def _loadString(
    content: bytearray, mapped_id: int, startOfStringData: int, encoding: str
) -> str:
    caret = mapped_id
    stringStart = _read(content, caret, 4)
    caret += 4  # Increment to 4 Bytes which we read above for string starting location
    stringLen = _read(content, caret, 2)
    offset = startOfStringData + stringStart
    return content[offset : offset + stringLen].decode(encoding)


def _loadPlural(
    content: bytearray, mapped_id: int, startOfStringData: int, encoding: str
) -> Dict:
    caret = mapped_id
    quantityCount = _read(content, caret)
    caret += 1  # Increment by a single byte which are for quantity count
    pluralMap = {}
    for _ in range(quantityCount):
        quantityId = _read(content, caret)
        caret += 1  # Increment by a single byte which are for quantity id
        stringStart = _read(content, caret, 4)
        caret += (
            4  # Increment to 4*8 Bits which we read above for plural starting location
        )
        stringLen = _read(content, caret, 2)
        caret += 2  # Increment to 2*8 Bits which we read above for plural length
        offset = startOfStringData + stringStart
        pluralMap[quantityId] = content[offset : offset + stringLen].decode(encoding)
    return pluralMap


def _map_translations(
    content: bytearray,
    startOfLocaleData: int,
    headerStart: int,
    startOfStringData: int,
    encoding: str,
    unpacked_output: List,
) -> Dict:
    caret = startOfLocaleData + headerStart
    numStrings = _read(content, caret, 2)
    _write_to_list(f"Num strings (2-bytes): {numStrings}", unpacked_output)
    caret += 2
    numPlurals = _read(content, caret, 2)
    _write_to_list(f"Num plurals (2-bytes): {numPlurals}", unpacked_output)
    caret += 2
    result = {"string": {}, "plurals": {}}
    _write_to_list("\n\n", unpacked_output)
    _write_to_list(">>>>>> String data <<<<<<", unpacked_output)
    _write_to_list("\n", unpacked_output)
    for _ in range(numStrings):
        id = _read(content, caret, 2)
        _write_to_list(f"String id (2-bytes): {id}", unpacked_output)
        caret += 2
        result["string"][id] = caret
        _write_to_list(
            f"String Starting Index (4-bytes): {_read(content, caret, 4)}",
            unpacked_output,
        )
        _write_to_list(
            f"String Length (2-bytes): {_read(content, caret+4, 2)}", unpacked_output
        )
        _write_to_list(
            f"String: {_loadString(content, caret, startOfStringData, encoding)}",
            unpacked_output,
        )
        _write_to_list("\n", unpacked_output)
        # Increment by 6 Bytes which is string starting location (4) + string length (2)
        # to be read later when string is fetched
        caret += 6

    _write_to_list("\n\n", unpacked_output)
    _write_to_list(">>>>>> Plural data <<<<<<", unpacked_output)
    _write_to_list("\n", unpacked_output)
    for _ in range(numPlurals):
        id = _read(content, caret, 2)
        _write_to_list(f"Plural id (2-bytes): {id}", unpacked_output)
        caret += 2
        result["plurals"][id] = caret
        quantityCount = _read(content, caret)
        _write_to_list(f"Quantity count (1-byte): {quantityCount}", unpacked_output)
        caret += 1  # Increment by a single byte which are for quantity count read above
        for __ in range(quantityCount):
            # Increment by 7 Bytes which is the
            # quantity id (1) + string starting location (4) + string length (2) to be
            # read later when plural is fetched
            _write_to_list(
                f"\tQuantity Id (1-byte): {_read(content, caret)}", unpacked_output
            )
            _write_to_list(
                f"\tString Starting Index (4-bytes): {_read(content, caret+1, 4)}",
                unpacked_output,
            )
            _write_to_list(
                f"\tString Length (2-bytes): {_read(content, caret+5, 2)}",
                unpacked_output,
            )
            _write_to_list(
                f"\tString: {_loadString(content, caret+1, startOfStringData, encoding)}",
                unpacked_output,
            )
            _write_to_list("\n", unpacked_output)
            caret += 7
    return result


class TranslationDict(object):
    def __init__(self):
        self.store = collections.defaultdict(dict)

    def add_for_locale(self, locale, string_dict: Dict):
        locale_dict = self.store[locale]
        for key, value in string_dict.items():
            locale_dict[key] = value

    def add_translation(self, translation_dict: Dict):
        for locale, dictionary in translation_dict.items():
            self.add_for_locale(locale, dictionary)

    def remove_unused_translation(self, id_finder: IdFinder, unused_strings: List):
        store = self.store
        for locale_dict in store.values():
            for unused_string in unused_strings:
                id = id_finder.get_id(unused_string)
                if id is not None and id in locale_dict:
                    del locale_dict[id]
        for locale in list(store.keys()):
            if len(store[locale]) == 0:
                del store[locale]

    def remap_entries(self, id_remapping: dict[int, int]):
        new_store = collections.defaultdict(dict)
        for locale in self.store:
            locale_dict = self.store[locale]
            updated_dict = {}
            for resource_id in locale_dict:
                if resource_id in id_remapping:
                    updated_dict[id_remapping[resource_id]] = locale_dict[resource_id]
            if len(updated_dict) != 0:
                new_store[locale] = updated_dict
        self.store = new_store




class StringPack(object):
    "The full string pack, with information about locales, ids, plurals, etc"

    def __init__(self, encoding: str, translation: TranslationDict):
        assert encoding in _ENCODING_ID
        self.encoding = encoding
        self.store = translation.store

    @staticmethod
    def from_file(file_name: str, unpacked_output: List = None) -> Dict:
        with open(file_name, mode="rb") as file:  # b is important -> binary
            content = file.read()
        numLocales = _read(content, 0, 2)
        _write_to_list(f"Num Locales (2-bytes): {numLocales}", unpacked_output)
        startOfLocaleData = _read(content, 2, 4)
        _write_to_list(
            f"Starting Index of Locale Data (4-bytes): {startOfLocaleData}",
            unpacked_output,
        )
        encodingId = _read(content, 6)
        assert encodingId in _ENCODING_INDEX.keys(), "Unrecognized encoding"
        encoding = _ENCODING_INDEX[encodingId]
        _write_to_list(f"Encoding (1-byte): {encoding}", unpacked_output)
        startOfStringData = _read(content, 7, 4)
        _write_to_list(
            f"Starting Index of string data (4-bytes): {startOfStringData}",
            unpacked_output,
        )

        caret = _HEADER_SIZE
        locale_starts = {}
        for _ in range(numLocales):
            resourceLocale = _read_locale_from(content, caret)
            _write_to_list(f"Locale (7-bytes): {resourceLocale}", unpacked_output)
            locale_starts[resourceLocale] = caret
            _write_to_list(
                f'Starting Index of Locale "{resourceLocale}" data (4-bytes)\': {_read(content, caret + 7, 4)}',
                unpacked_output,
            )
            caret += _LOCALE_HEADER_SIZE
        translation_dict = {}
        for locale, locale_start in locale_starts.items():
            locale_dict = {}
            translation_dict[locale] = locale_dict
            headerStart = _read(content, locale_start + 7, 4)
            _write_to_list(
                f'>>> Information about locale: "{locale}" <<<', unpacked_output
            )
            mapping = _map_translations(
                content,
                startOfLocaleData,
                headerStart,
                startOfStringData,
                encoding,
                unpacked_output,
            )
            string_mapping = mapping["string"]
            for android_id, string_pack_id in string_mapping.items():
                locale_dict[android_id] = _loadString(
                    content, string_pack_id, startOfStringData, encoding
                )
            plural_mapping = mapping["plurals"]
            for android_id, string_pack_id in plural_mapping.items():
                locale_dict[android_id] = _loadPlural(
                    content, string_pack_id, startOfStringData, encoding
                )
        return translation_dict

    def compile(self):
        self.string_buffer = StringBuffer(encoding=self.encoding)
        locales = sorted(self.store.keys())
        self.locales_info = bytearray()
        locale_blobs_total_size = 0
        self.locale_blobs = []
        for locale in locales:
            blob_append_locale(self.locales_info, locale)
            locale_store = LocaleStore()
            for id in sorted(self.store[locale].keys()):
                value = self.store[locale][id]
                locale_store.add_plural_or_string(id, self.string_buffer.add(value))
            locale_blob = bytes(locale_store.get_binary_blob())
            blob_append_32_bit(self.locales_info, locale_blobs_total_size)  # start
            locale_blobs_total_size += len(locale_blob)
            self.locale_blobs.append(locale_blob)
        self.header_blob = bytearray()
        blob_append_16_bit(self.header_blob, len(locales))  # Number of locales
        blob_append_32_bit(
            self.header_blob, _HEADER_SIZE + len(locales) * _LOCALE_HEADER_SIZE
        )  # Start of locale data
        self.header_blob.append(_ENCODING_ID[self.encoding])  # Just one byte
        blob_append_32_bit(
            self.header_blob,
            _HEADER_SIZE
            + len(locales) * _LOCALE_HEADER_SIZE
            + sum([len(blob) for blob in self.locale_blobs]),
        )  # Start of string data

    def string_buffer_size(self):
        return len(self.string_buffer.store)

    def write_to_file(self, pack_file_name):
        with open(pack_file_name, "wb") as pack_file:
            pack_file.write(self.header_blob)
            pack_file.write(self.locales_info)
            for locale_blob in self.locale_blobs:
                pack_file.write(locale_blob)
            pack_file.write(self.string_buffer.store)


def build_with_dict(output_file_name: str, translation_dict: TranslationDict) -> None:
    """Builds the string pack and writes it to a file.

    It tries both UTF-8 and UTF-16 to see which one is smaller, and then writes
    the string pack in that encoding."""
    packs = []
    for encoding in _ENCODING_ID.keys():
        full_store = StringPack(encoding=encoding, translation=translation_dict)
        full_store.compile()
        packs.append(full_store)
    smallest_pack = min(packs, key=lambda p: p.string_buffer_size())
    smallest_pack.write_to_file(output_file_name)


def build(
    input_file_names: List, output_file_name: str, id_finder: IdFinder, plural_handler
):
    translation_dict = TranslationDict()
    for input_file_name in input_file_names:
        locale = extract_locale_from_file_name(input_file_name)
        translation_dict.add_for_locale(
            locale,
            read_string_dict(locale, input_file_name, id_finder, plural_handler),
        )
    build_with_dict(output_file_name, translation_dict)


def get_unused_resource(nullified_resource: str) -> List[str]:
    with open(nullified_resource, "r") as file:
        lines = file.read().splitlines()
        return [line[line.rfind(".") + 1 :] for line in lines]


def repack(
    resource_config: str, original_pack: str, nullified_resource: str, output: str
) -> None:
    translation = TranslationDict()
    input_dict = StringPack.from_file(original_pack)
    translation.add_translation(input_dict)
    id_finder = IdFinder.from_resource_config(resource_config)
    unused_resource = get_unused_resource(nullified_resource)
    translation.remove_unused_translation(id_finder, unused_resource)
    build_with_dict(output, translation)

def build_id_remapping(remapping_file: str) -> dict[int, int]:
    # Expects a file filled with lines like "10 12", meaning
    # the string/plural with the string ID 10 is remapped to 12
    with open(remapping_file, "r") as file:
        remapping = {}
        for line in file:
            ids = line.split(' ')
            remapping[int(ids[0].strip())] = int(ids[1].strip())
        return remapping

def remap(
    remapping_file: str, original_pack: str, output: str
) -> None:
    translation = TranslationDict()
    input_dict = StringPack.from_file(original_pack)
    translation.add_translation(input_dict)
    id_remapping = build_id_remapping(remapping_file)
    translation.remap_entries(id_remapping)
    build_with_dict(output, translation)


def unpack(original_pack: str, output_file: str) -> None:
    unpacked_output = []
    StringPack.from_file(original_pack, unpacked_output)
    with open(output_file, "wt") as unpacked_file:
        unpacked_file.writelines("\n".join(unpacked_output))


def main():
    arg_parser = argparse.ArgumentParser()
    arg_parser.add_argument(
        "--resource-config",
        help="Location of Android resource config file mapping used by --stable-ids.",
    )
    arg_parser.add_argument(
        "--original-pack",
        help="Location of original string pack files to be optimized.",
    )
    arg_parser.add_argument(
        "--nullified-resource", help="Location of unused Android resource ids."
    )
    arg_parser.add_argument(
        "--output-file", help="Location of output trimmed string pack file."
    )

    args = arg_parser.parse_args()
    repack(
        args.resource_config,
        args.original_pack,
        args.nullified_resource,
        args.output_file,
    )


if __name__ == "__main__":
    main()
