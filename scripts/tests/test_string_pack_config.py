# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import os
import unittest
from unittest import mock

import string_pack_config


class TestStringPackConfig(unittest.TestCase):
    def test_is_valid_language_qualifier(self):
        self.assertTrue(string_pack_config.is_valid_language_qualifier("en"))
        self.assertTrue(string_pack_config.is_valid_language_qualifier("id"))

        self.assertTrue(string_pack_config.is_valid_language_qualifier("fr-rCA"))
        self.assertTrue(string_pack_config.is_valid_language_qualifier("id-rER"))

        self.assertTrue(string_pack_config.is_valid_language_qualifier("b+es+419"))

        self.assertFalse(string_pack_config.is_valid_language_qualifier("chinese"))
        self.assertFalse(string_pack_config.is_valid_language_qualifier("zh-xCN"))
        self.assertFalse(string_pack_config.is_valid_language_qualifier("b+es-419"))

    @mock.patch.object(string_pack_config.StringPackConfig, "load_from_file")
    @mock.patch.object(string_pack_config.StringPackConfig, "load_from_dict")
    def test_load_config_with_default(self, load_from_dict, load_from_file):
        string_pack_config.load_config()

        load_from_dict.assert_not_called()
        load_from_file.assert_called_once()
        self.assertEqual(
            "default_config.json", os.path.basename(load_from_file.call_args[0][0])
        )

    @mock.patch.object(string_pack_config.StringPackConfig, "load_from_file")
    @mock.patch.object(string_pack_config.StringPackConfig, "load_from_dict")
    def test_load_config_with_config_file(self, load_from_dict, load_from_file):
        config_file = "test_config.json"
        string_pack_config.load_config(config_json_file_path=config_file)

        load_from_dict.assert_not_called()
        self.assertTrue(2, load_from_file.method_calls)
        load_from_file.assert_called_with(config_file)

    @mock.patch.object(string_pack_config.StringPackConfig, "load_from_file")
    @mock.patch.object(string_pack_config.StringPackConfig, "load_from_dict")
    def test_load_config_with_dictionary(self, load_from_dict, load_from_file):
        config_dict = {"key": "value"}
        string_pack_config.load_config(config_dict, skip_default_config=True)

        load_from_dict.assert_called_once_with(config_dict)
        load_from_file.assert_not_called()
