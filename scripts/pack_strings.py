#!/usr/bin/env python3

# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import argparse
import collections
import glob
import logging
import multiprocessing
import os

import string_pack
import string_pack_config
from id_finder import IdFinder
from string_pack_config import LanguageHandlingCase, StringPackConfig


def group_string_files_by_languages(
    sp_config: StringPackConfig, packable_strings_file_paths
):
    # A map from language (aka, pack ID) to list of string resource files.
    grouped_files = collections.defaultdict(list)

    for strings_file in packable_strings_file_paths:
        values_dir_name = os.path.normpath(strings_file).split(os.sep)[-2]
        language = values_dir_name.replace("values-", "")
        handler_case = sp_config.get_handling_case(language)
        if handler_case == LanguageHandlingCase.PACK:
            pack_id = sp_config.pack_id_mapping.get(language, language)
            grouped_files[pack_id].append(strings_file)

    return grouped_files


def get_dest_pack_file_path(sp_config: StringPackConfig, pack_id):
    prefix = "" if sp_config.module is None else (sp_config.module + "_")
    return os.path.join(sp_config.assets_directory, f"{prefix}strings_{pack_id}.pack")


def pack_strings(sp_config: StringPackConfig, plural_handler):
    id_finder = IdFinder.from_stringpack_config(sp_config)
    packable_strings_file_paths = []

    moved = []
    for directory in sp_config.original_resources_directories:
        if directory in sp_config.destination_stringpack_directories:
            root_dir = sp_config.destination_stringpack_directories[directory]
        else:
            root_dir = directory
        moved.append(os.path.join(root_dir, "string-packs", "strings"))

    for directory in moved:
        string_files = sorted(
            glob.glob(os.path.join(directory, "**/strings.xml"), recursive=True)
        )
        new_string_files = [
            file for file in string_files if file not in packable_strings_file_paths
        ]
        packable_strings_file_paths.extend(new_string_files)

    grouped_strings_file_paths = group_string_files_by_languages(
        sp_config, packable_strings_file_paths
    )

    # Create assets directory in case it does not exist.
    os.makedirs(sp_config.assets_directory, exist_ok=True)

    PackBuilder(
        sp_config, grouped_strings_file_paths, id_finder, plural_handler
    ).build()


class PackBuilder(object):
    def __init__(
        self,
        sp_config: StringPackConfig,
        grouped_strings_file_paths,
        id_finder,
        plural_handler,
    ):
        self.sp_config = sp_config
        self.grouped_strings_file_paths = grouped_strings_file_paths
        self.id_finder = id_finder
        self.plural_handler = plural_handler

    def build(self):
        with multiprocessing.Pool() as pool:
            pool.map(self.build_impl, sorted(self.grouped_strings_file_paths))

    def build_impl(self, pack_id):
        logging.info("Packing: " + pack_id)
        string_resources_file_paths = self.grouped_strings_file_paths[pack_id]

        string_pack.build(
            string_resources_file_paths,
            get_dest_pack_file_path(self.sp_config, pack_id),
            self.id_finder,
            self.plural_handler,
        )


def noop_plural_handler(locale, comment, quantity):
    """Customizable function to return true if an item should not be packed.

    Could be customized to strip out plural cases that are known to not be used.
    For example, if the plural selector in a plural sting is always larger than 1,
    there's no need to pack the 'one' case for many languages."""
    return False


def main():
    arg_parser = argparse.ArgumentParser()
    arg_parser.add_argument("--config", help="Location of JSON config file.")

    args = arg_parser.parse_args()
    sp_config = string_pack_config.load_config(config_json_file_path=args.config)

    pack_strings(sp_config, noop_plural_handler)


if __name__ == "__main__":
    main()
