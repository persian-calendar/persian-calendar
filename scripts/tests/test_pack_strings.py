# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import unittest

import pack_strings
import string_pack_config
from pack_strings import IdFinder
from tests import test_util


class TestPackStringsMethods(unittest.TestCase):
    def setUp(self):
        self.sp_config = string_pack_config.StringPackConfig()
        self.sp_config.assets_directory = "app/src/main/assets/"

    def test_group_string_files_by_languages_without_mapping(self):
        self.sp_config.languages_to_pack = ["*"]

        grouped_file_paths = pack_strings.group_string_files_by_languages(
            self.sp_config,
            [
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            ],
        )

        self.assertEqual(2, len(grouped_file_paths))
        self.assertSetEqual(
            {
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
            },
            set(grouped_file_paths["cs"]),
        )

        self.assertSetEqual(
            {
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            },
            set(grouped_file_paths["sk"]),
        )

    def test_group_string_files_by_languages_with_mapping(self):
        self.sp_config.languages_to_pack = ["*"]
        self.sp_config.pack_id_mapping = {"sk": "cs"}

        grouped_file_paths = pack_strings.group_string_files_by_languages(
            self.sp_config,
            [
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            ],
        )

        self.assertEqual(1, len(grouped_file_paths))
        self.assertSetEqual(
            {
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            },
            set(grouped_file_paths["cs"]),
        )

    def test_group_string_files_by_languages_two_languages_with_mapping(self):
        self.sp_config.languages_to_pack = ["*"]
        self.sp_config.pack_id_mapping = {"sk": "cs"}

        grouped_file_paths = pack_strings.group_string_files_by_languages(
            self.sp_config,
            [
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/app_src_main_res/values-zh-rCN/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-zh-rCN/strings.xml",
            ],
        )

        self.assertEqual(2, len(grouped_file_paths))

        self.assertSetEqual(
            {
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            },
            set(grouped_file_paths["cs"]),
        )

        self.assertSetEqual(
            {
                "sp/app_src_main_res/values-zh-rCN/strings.xml",
                "sp/coreui_src_main_res/values-zh-rCN/strings.xml",
            },
            set(grouped_file_paths["zh-rCN"]),
        )

    def test_group_string_files_by_custom_languages_to_pack_without_mapping(self):
        self.sp_config.languages_to_pack = ["cs"]

        grouped_file_paths = pack_strings.group_string_files_by_languages(
            self.sp_config,
            [
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            ],
        )

        self.assertEqual(1, len(grouped_file_paths))
        self.assertSetEqual(
            {
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
            },
            set(grouped_file_paths["cs"]),
        )

    def test_group_string_files_by_custom_languages_to_pack_empty_without_mapping(self):
        self.sp_config.languages_to_pack = []

        grouped_file_paths = pack_strings.group_string_files_by_languages(
            self.sp_config,
            [
                "sp/app_src_main_res/values-cs/strings.xml",
                "sp/app_src_main_res/values-sk/strings.xml",
                "sp/coreui_src_main_res/values-cs/strings.xml",
                "sp/coreui_src_main_res/values-sk/strings.xml",
            ],
        )

        self.assertEqual(0, len(grouped_file_paths))

    def test_get_dest_pack_file_path_with_module(self):
        self.sp_config.module = "module"
        pack_file_pack = pack_strings.get_dest_pack_file_path(self.sp_config, "ca")
        self.assertEqual("app/src/main/assets/module_strings_ca.pack", pack_file_pack)

    def test_get_dest_pack_file_path_without_module(self):
        pack_file_pack = pack_strings.get_dest_pack_file_path(self.sp_config, "ca")
        self.assertEqual("app/src/main/assets/strings_ca.pack", pack_file_pack)

    EXPECTED_DICT = {
        "people": 0,
        "no": 1,
        "yes": 2,
    }

    def test_get_parse_id_from_resource_config(self):
        config_path = test_util.get_res_path("expected_resources.txt")
        id_finder = IdFinder.from_resource_config(config_path)
        self.assertDictEqual(id_finder.seen_ids, self.EXPECTED_DICT)

    def test_get_parse_id_from_stringpack_config(self):
        config_path = test_util.get_res_path("expected_resources.txt")
        self.sp_config.resource_config_setting = {"config_file_path": config_path}
        id_finder = IdFinder.from_stringpack_config(self.sp_config)
        self.assertDictEqual(id_finder.seen_ids, self.EXPECTED_DICT)
