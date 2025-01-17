from pysarl.io.sarl.lang.core.EventSpace import EventSpace
from pysarl.io.sarl.lang.core.Space import Space
from vm.builtin.service.Context import Context
class PythonContext(Context):
    def __init__(self):
        self.defaultSpace = EventSpace()
        self.spaces = []

    def getDefaultSpace(self) -> Space:
        return self.defaultSpace

    def createSpace(self, uuid):
        self.spaces.append(EventSpace())
