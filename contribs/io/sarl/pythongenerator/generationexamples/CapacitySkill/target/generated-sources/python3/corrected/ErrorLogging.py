#!/usr/bin/env python3
# Generated by the SARL compiler the Thu Oct 13 19:57:43 CEST 2022. Do not change this file.

from Logging import Logging


class ErrorLogging(Logging, object):
    def error(self, text: str):
        raise Exception("Unimplemented function")

    def __init__(self):
        pass
