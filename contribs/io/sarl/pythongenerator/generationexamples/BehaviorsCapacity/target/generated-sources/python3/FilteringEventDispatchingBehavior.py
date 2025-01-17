#!/usr/bin/env python3
# Generated by the SARL compiler the Sat Oct 22 13:20:32 CEST 2022. Do not change this file.

from io.sarl.core import Behaviors
from io.sarl.lang.core import Behavior
from io.sarl.lang.core import Event
from io.sarl.lang.core import EventListener
from io.sarl.lang.core import Scope
from io.sarl.lang.util import ConcurrentCollection
from org.eclipse.xtext.xbase.lib.Functions import Function1

class FilteringEventDispatchingBehavior(Behaviors,object):
	def asEventListener(self) -> EventListener:
		return FilteringEventListener(self)
	def getRegisteredBehaviors(self) -> ConcurrentCollection<Behavior>:
		return self.behaviorDelegate.getRegisteredBehaviors()
	def hasRegisteredBehavior(self) -> bool:
		return self.behaviorDelegate.hasRegisteredBehavior()
	def registerBehavior(self, attitude : Behavior, filter : Function1, *initializationParameters : object) -> Behavior:
		return self.behaviorDelegate.registerBehavior(attitude, filter, initializationParameters)
	def registerBehavior(self, attitude : Behavior, *initializationParameters : Object[]) -> Behavior:
		return self.registerBehavior(attitude, None, initializationParameters)
	def unregisterBehavior(self, attitude_1 : Behavior) -> Behavior:
		return self.behaviorDelegate.unregisterBehavior(attitude)
	def wake(self, event : Event, scope : Scope) -> void:
		self.behaviorDelegate.wake(^event, scope)
	def wake(self, event : Event) -> void:
		return self.wake(event, None)
	def wake(self, beh : Behavior, event_1 : Event) -> void:
		self.behaviorDelegate.wake(beh, ^event)
	def wake(self, behs : Iterable, event_2 : Event) -> void:
		self.behaviorDelegate.wake(behs, ^event)
	def __init__(self, acceptedType : Class, behaviorDelegate : Behaviors) -> void:
		(self.acceptedType = acceptedType)
		(self.behaviorDelegate = behaviorDelegate)