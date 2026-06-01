# Copyright (c) Facebook, Inc. and its affiliates. All rights reserved.
#
# This source code is licensed under the Apache 2.0 license found in
# the LICENSE file in the root directory of this source tree.

import os


_RES_FOLDER_NAME = "res"


def get_res_path(file_name):
    current_folder = os.path.dirname(__file__)
    return os.path.join(current_folder, _RES_FOLDER_NAME, file_name)
