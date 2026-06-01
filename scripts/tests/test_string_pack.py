#!/usr/bin/env python3
# Copyright (c) Meta Platforms, Inc. and affiliates.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import json
import tempfile
import unittest
from typing import Dict, Optional

import pack_strings
import string_pack
from tests import test_util


class FakeIdFinder(object):
    TEST_DATA = {"first_plurals": 0, "first_string": 1, "second_string": 2}

    def get_id(self, resource_name: str) -> Optional[int]:
        return FakeIdFinder.TEST_DATA.get(resource_name)


def _compare_dict_deep(d1: Dict, d2: Dict) -> bool:
    return json.dumps(d1, sort_keys=True) == json.dumps(d2, sort_keys=True)


class TestStringPackMethods(unittest.TestCase):
    def test_not_nullified(self):
        self.assertDictEqual(
            {
                0: {0: "many plurals", 1: "zero plurals", 2: "one plural"},
                1: "first string",
            },
            string_pack.read_string_dict(
                "en",  # Don't care
                test_util.get_res_path("test_resources_en.xml"),
                FakeIdFinder(),
                pack_strings.noop_plural_handler,
            ),
        )

    def test_nullified_string(self):
        self.assertDictEqual(
            {0: {0: "many plurals", 1: "zero plurals", 2: "one plural"}},
            string_pack.read_string_dict(
                "en",  # Don't care
                test_util.get_res_path("test_resources_en.xml"),
                FakeIdFinder(),
                pack_strings.noop_plural_handler,
                {"R.string.first_string"},
            ),
        )

    def test_nullified_plurals(self):
        self.assertDictEqual(
            {1: "first string"},
            string_pack.read_string_dict(
                "en",  # Don't care
                test_util.get_res_path("test_resources_en.xml"),
                FakeIdFinder(),
                pack_strings.noop_plural_handler,
                {"R.plurals.first_plurals"},
            ),
        )

    TEST_TRANSLATION = {
        "en-US": {
            0: {0: "many colors", 1: "zero color", 2: "one color"},
            10: "first color",
        },
        "en-GB": {
            0: {0: "many colours", 1: "zero colour", 2: "one colour"},
            2: "first cheque",
            4: "first diet",
        },
    }

    EXPECTED_TRANSLATION = {
        "en-US": {
            10: "first color",
        },
        "en-GB": {
            4: "first diet",
        },
    }

    def test_UTF_8(self):
        self._test_unpacking("UTF-8")

    def test_UTF_16(self):
        self._test_unpacking("UTF-16BE")

    def _test_unpacking(self, encoding):
        translation = string_pack.TranslationDict()
        translation.add_translation(self.TEST_TRANSLATION)
        full_store = string_pack.StringPack(encoding=encoding, translation=translation)
        full_store.compile()
        with tempfile.NamedTemporaryFile(suffix=".pack") as pack:
            filename = pack.name
            full_store.write_to_file(filename)
            self.assertTrue(
                _compare_dict_deep(
                    string_pack.StringPack.from_file(filename), self.TEST_TRANSLATION
                )
            )

    def test_build_with_dict(self):
        translation = string_pack.TranslationDict()
        translation.add_translation(self.TEST_TRANSLATION)
        with tempfile.NamedTemporaryFile(suffix=".pack") as pack:
            filename = pack.name
            string_pack.build_with_dict(filename, translation)
            self.assertTrue(
                _compare_dict_deep(
                    string_pack.StringPack.from_file(filename), self.TEST_TRANSLATION
                )
            )

    def test_repacking_no_removal(self):
        translation = string_pack.TranslationDict()
        translation.add_translation(self.TEST_TRANSLATION)
        translation.remove_unused_translation(FakeIdFinder(), [])
        self.assertTrue(_compare_dict_deep(translation.store, self.TEST_TRANSLATION))

    def test_repacking_removal(self):
        translation = string_pack.TranslationDict()
        translation.add_translation(self.TEST_TRANSLATION)
        translation.remove_unused_translation(
            FakeIdFinder(), ["first_plurals", "first_string", "second_string"]
        )
        self.assertTrue(
            _compare_dict_deep(
                translation.store,
                self.EXPECTED_TRANSLATION,
            )
        )

    def test_repacking_removal_empty_locale(self):
        translation = string_pack.TranslationDict()
        translation.add_translation({"en": {0: {}, 1: ""}})
        translation.remove_unused_translation(
            FakeIdFinder(), ["first_plurals", "first_string", "second_string"]
        )
        self.assertTrue(
            _compare_dict_deep(
                translation.store,
                {},
            )
        )

    def test_parse_nullified_resource_file(self):
        self.assertListEqual(
            ["people", "yes", "irrelevant"],
            string_pack.get_unused_resource(
                test_util.get_res_path("unused_resource.txt"),
            ),
        )

    def test_repack_integration(self):
        translation = string_pack.TranslationDict()
        translation.add_translation(self.TEST_TRANSLATION)
        with tempfile.NamedTemporaryFile(
            suffix=".pack"
        ) as input_pack, tempfile.NamedTemporaryFile(suffix=".pack") as output_pack:
            string_pack.build_with_dict(input_pack.name, translation)
            string_pack.repack(
                test_util.get_res_path("expected_resources.txt"),
                input_pack.name,
                test_util.get_res_path("unused_resource.txt"),
                output_pack.name,
            )
            self.assertTrue(
                _compare_dict_deep(
                    string_pack.StringPack.from_file(output_pack.name),
                    self.EXPECTED_TRANSLATION,
                )
            )
