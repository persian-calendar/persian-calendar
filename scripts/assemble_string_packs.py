#!/usr/bin/env python3

# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.


"""This is an integrated script that runs all three StringPacks process steps (find/move/pack) at the same time."""

import argparse
import logging

import find_movable_strings
import move_strings_for_packing
import pack_strings
import string_pack_config


def create_arg_parser():
    arg_parser = argparse.ArgumentParser(description="Assemble String Packs.")
    arg_parser.add_argument(
        "--config", help="Location of JSON config file.", required=True
    )

    return arg_parser


def main():
    logging.basicConfig(level=logging.INFO)

    arg_parser = create_arg_parser()

    args = arg_parser.parse_args()

    sp_config = string_pack_config.load_config(config_json_file_path=args.config)

    find_movable_strings.find_movable_strings(sp_config, print_reverse=False)
    move_strings_for_packing.move_all_strings(sp_config, keep_dest=True)
    pack_strings.pack_strings(sp_config, pack_strings.noop_plural_handler)


if __name__ == "__main__":
    main()
