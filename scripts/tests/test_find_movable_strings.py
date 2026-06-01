# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import filecmp
import shutil
import tempfile
import unittest

import find_movable_strings as sp_find
from tests import test_util


class TestPackStringsMethods(unittest.TestCase):
    def test_find_strings_used_in_xml_with_layout(self):
        self.assertSetEqual(
            {"button", "button_url", "image", "description", "title", "app_title"},
            sp_find.find_strings_used_in_xml(
                test_util.get_res_path("test_layout.xml"), frozenset()
            ),
        )

    def test_find_strings_used_in_xml_with_safe_widget(self):
        self.assertSetEqual(
            {"image", "title", "app_title"},
            sp_find.find_strings_used_in_xml(
                test_util.get_res_path("test_layout.xml"), frozenset({"Button"})
            ),
        )

    def test_find_strings_used_in_xml_with_resources(self):
        self.assertSetEqual(
            {"other_string", "string_array_two", "string_array_one", "style_text"},
            sp_find.find_strings_used_in_xml(
                test_util.get_res_path("test_resources.xml"), frozenset()
            ),
        )

    def test_do_not_pack_strings_not_packed_into_resource(self):
        self.assertSetEqual(
            {"donotpack_string"},
            sp_find.find_donotpack_strings(
                test_util.get_res_path("test_resources.xml")
            ),
        )

    def test_get_resource_file_content(self):
        source_template = test_util.get_res_path("Test.java")
        expected_source_result = test_util.get_res_path("Expected.java")
        expected_config_result = test_util.get_res_path("expected_resources.txt")
        with tempfile.NamedTemporaryFile() as config_output, tempfile.NamedTemporaryFile(
            suffix=".java"
        ) as source_output:
            shutil.copy(source_template, source_output.name)
            output_string_ids_config = {
                "config_file_path": config_output.name,
                "source_file_path": source_output.name,
                "string_offset": "0x7f120000",
                "plurals_offset": "0x7f110000",
                "package_name": "com.example",
            }
            sorted_resources = sorted(
                {("plurals", "people"), ("string", "yes"), ("string", "no")}
            )
            sp_find.output_string_ids_config(output_string_ids_config, sorted_resources)
            self.assertTrue(
                filecmp.cmp(source_output.name, expected_source_result, shallow=False)
            )
            self.assertTrue(
                filecmp.cmp(config_output.name, expected_config_result, shallow=False)
            )
