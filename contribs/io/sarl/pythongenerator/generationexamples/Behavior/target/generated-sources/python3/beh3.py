#!/usr/bin/env python3
# Generated by the SARL compiler the Thu Oct 13 19:59:53 CEST 2022. Do not change this file.

from io.sarl.lang.core import Behavior

class beh3(Behavior,object):
	def __on_Initialize__(self, occurrence):
		self.info(u"I\'m initializing my behavior")
	def __on_Destroy__(self, occurrence):
		self.info(u"Destroying the behavior")
	def __guard_io_sarl_core_Initialize__(self, occurrence):
		it = occurrence
		__event_handles = list
		__event_handles.add(__on_Initialize__)
		return __event_handles
	
	def __guard_io_sarl_core_Destroy__(self, occurrence):
		it = occurrence
		__event_handles_1 = list
		__event_handles_1.add(__on_Destroy__)
		return __event_handles_1
	def __init__(self):
		pass