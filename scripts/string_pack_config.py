# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import enum
import json
import os
from importlib import resources


def load_config(
    config_json_dict=None, config_json_file_path=None, skip_default_config=False
) -> "StringPackConfig":
    if (
        config_json_dict is None
        and config_json_file_path is None
        and skip_default_config
    ):
        raise ValueError(
            "Please specify at least one type for loading StringPackConfig."
        )

    sp_config = StringPackConfig()

    if not skip_default_config:
        with resources.path("resources", "default_config.json") as path:
            sp_config.load_from_file(path)

    if config_json_file_path is not None:
        sp_config.load_from_file(config_json_file_path)

    if config_json_dict is not None:
        sp_config.load_from_dict(config_json_dict)

    return sp_config


def is_valid_language_qualifier(resource_qualifier):
    if len(resource_qualifier) == 2:
        # language only, e.g.: en
        return True

    if len(resource_qualifier) == 6 and resource_qualifier[2:4] == "-r":
        # language with region. e.g.: fr-rCA
        return True

    if resource_qualifier.startswith("b+") and "-" not in resource_qualifier:
        # special language: e.g.: b+es+419
        return True

    return False


class LanguageHandlingCase(enum.Enum):
    """Determine how should string packs handles strings for that language"""

    # Keep original file in project, don't pack them. This could happen for non-language qualifier, e.g. values-land.
    KEEP_ORIGINAL = 0
    # Pack them at eventually, so move the string resources to intermediate file
    PACK = 1
    # Remove the strings from original resource file, but don't move to intermediate file
    # See StringPackConfig.languages_to_drop
    DROP = 2


class StringPackConfig:
    """
    Configurations for running string packs scripts.

    This provides the configuration information for string pack scripts to generate the intermediate files, and save
    final content at the right place.

    """

    FIELDS_TO_OVERRIDE = [
        "module",
        "original_resources_directories",
        "destination_stringpack_directories",
        "find_resource_files_command",
        "languages_to_pack",
        "languages_to_drop",
        "assets_directory",
        "pack_ids_class_file_path",
        "resource_config_setting",
        "pack_id_mapping",
        "pack_scripts_directory",
    ]

    def __init__(self):
        # The project module that string packs are used for.
        self.module = None

        # The directories above the Android resources directories, where the script can find values/strings.xml and values-xx/ directories.
        # (i.e. app/src/main)
        # These directories will also hold the files of the packable strings.
        # FIND operation checks each directory's res subdirectory for values/strings.xml and values-xx/.strings.xml files. The ids are stored in pack_ids_class_file_path.
        # MOVE operation moves packable strings found in FIND to the directory's string-packs/strings subdirectory.
        # PACK operation packs the files in the directory's string-packs/strings subdirectory into .pack files in assets_directory.
        self.original_resources_directories = []

        # The directories where the script will save the strings.xml that need to be stringpacked after the MOVE command.
        # Format:
        # {"original_resource_directory": "destination_stringpack_xml_directory"}
        # This allows saving values-xx/strings.xml (candidates for PACK command) files for a module at any custom location and not necessarily at the
        # original_resource_directory/string-packs/strings subdirectory.
        # The script will create the destination_stringpack_xml_directory if it doesn't exist.
        # If the destination_stringpack_xml_directory is not specified, the script will save the files at the original_resource_directory/string-packs/strings subdirectory.
        # If the destination_stringpack_xml_directory is specified, the script will save the files at the destination_stringpack_xml_directory/string-packs/strings subdirectory.
        # MOVE - If a original_resource_directory key is present, it will copy to the new destination directory, else original_resource_directory
        # PACK - If a original_resource_directory key is present, it will pack from the new destination directory, else original_resource_directory
        self.destination_stringpack_directories = {}

        # Executable command line that returns all resource files for parsing movable strings.
        self.find_resource_files_command = None

        # List of languages that need to be packed, or ["*"] means all languages.
        self.languages_to_pack = []

        # List of languages that don't need to be packed, and they could be safely removed.
        # e.g.: "zh-rTW" could be drop if "zh-rHK" is set the same, the app would pick it correctly.
        self.languages_to_drop = []

        # The full class name of custom widgets that are already using StringPack resources reader.
        self.safe_widget_classes = set()

        # The assets directory where to save the generated string pack files.
        self.assets_directory = None

        # File path to the class where stores the map from android string key to pack id. Not compatible with `resource_config_setting`.
        self.pack_ids_class_file_path = None

        # Settings dict for aapt2 config that overrides the android string id. Not compatible with `pack_ids_class_file_path`.
        # All properties below are mandatory entries in the dict.
        # - `config_file_path` file path for aapt2 config to set stable id
        # - `source_file_path` for file path of generated Java source
        # - `string_offset` a hex string for string id offset (usually "0x7f120000")
        # - `plurals_offset` a hex string for plural id offset (usually "0x7f100000")
        # - `package_name` for package name.
        #
        # Change the apk build script to ensure this takes effect in build process.
        #     android.androidResources.additionalParameters "--stable-ids", config_file_path
        self.resource_config_setting = None

        # A dictionary that maps a specific language to its pack ID. The default pack ID is the language code, but the
        # app may decide to pack similar languages to one pack file.
        # For example, it may save space to pack Czech and Slovak in one pack file, assuming the app knows where to
        # look for them.
        self.pack_id_mapping = {}

        # The directory that holds all the python scripts
        self.pack_scripts_directory = None

    def load_from_file(self, config_json_file_path):
        """Load configuration from json file."""

        with open(config_json_file_path) as file_content:
            config_json_dict = json.load(file_content)

        self.load_from_dict(config_json_dict)

    def load_from_dict(self, config_dict):
        """Load configuration from a dictionary which servers as json.

        All configuration values would be overridden with the value that provided in config_json, except
        `safe_widget_classes` which new widget class names would be append to it.
        """

        for field in StringPackConfig.FIELDS_TO_OVERRIDE:
            if field in config_dict:
                setattr(self, field, config_dict[field])

        # Append instead of override
        if "safe_widget_classes" in config_dict:
            self.safe_widget_classes.update(config_dict["safe_widget_classes"])

        return self

    def get_default_string_files(self):
        return [
            os.path.join(source_directory, "res", "values", "strings.xml")
            for source_directory in self.original_resources_directories
        ]

    def get_handling_case(self, resource_qualifier):
        """Determine how to handle the string for given resource qualifier if it's a language qualifier."""

        if resource_qualifier in self.languages_to_drop:
            return LanguageHandlingCase.DROP

        if self.languages_to_pack == ["*"]:
            # Match all valid language
            if is_valid_language_qualifier(resource_qualifier):
                return LanguageHandlingCase.PACK
        elif resource_qualifier in self.languages_to_pack:
            return LanguageHandlingCase.PACK

        return LanguageHandlingCase.KEEP_ORIGINAL
