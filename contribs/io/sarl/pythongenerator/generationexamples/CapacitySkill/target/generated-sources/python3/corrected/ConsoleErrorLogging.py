#!/usr/bin/env python3
# Generated by the SARL compiler the Thu Oct 13 19:57:43 CEST 2022. Do not change this file.

from ErrorLogging import ErrorLogging


class ConsoleErrorLogging(ErrorLogging, object):
    def info(self, text: str):
        print(text)

    def debug(self, text: str):
        print(text)

    def error(self, text: str):
        print(text)

    def __init__(self):
        pass
