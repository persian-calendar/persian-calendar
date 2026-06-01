# StringPacks - Unit Tests

This folder contains the unit tests for the StringPacks python scripts.

## Run Tests

Those tests have to be run as python module in the **parent directory** with
python3.

To run all the tests:
```bash
python3 -m unittest discover tests 
```

To run specific test file, just reference file name:
```bash
python3 -m unittest tests/test_pack_strings.py
```

For more information about python `unittest`, check [the official documentation](https://docs.python.org/3/library/unittest.html).