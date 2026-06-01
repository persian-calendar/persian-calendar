#!/usr/bin/env python3

# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import os
import re
from typing import List, Optional

from string_pack_config import StringPackConfig


class IdFinder(object):
    def __init__(self, all_matches: List):
        self.seen_ids = {}
        for i in range(len(all_matches)):
            self.seen_ids[all_matches[i]] = i

    @classmethod
    def from_resource_config(cls, config_file_path: str) -> "IdFinder":
        assert os.path.exists(
            config_file_path
        ), f"Config file {config_file_path} does not exist"
        all_matches = []
        with open(config_file_path, "rt") as fd:
            id_data = fd.read()
            all_matches = re.findall(
                r"\:(?:string|plurals)\/(\w+) =", id_data, flags=re.DOTALL
            )
        return cls(all_matches)

    @classmethod
    def from_stringpack_config(cls, sp_config: StringPackConfig) -> "IdFinder":
        if sp_config.pack_ids_class_file_path is not None:
            with open(sp_config.pack_ids_class_file_path, "rt") as fd:
                id_data = fd.read()
            all_matches = re.findall(
                r"R\.(?:string|plurals)(?:.*?)\.(\w+),", id_data, flags=re.DOTALL
            )
            return cls(all_matches)
        else:
            return cls.from_resource_config(
                sp_config.resource_config_setting["config_file_path"]
            )

    def get_id(self, resource_name: str) -> Optional[int]:
        return self.seen_ids.get(resource_name)
