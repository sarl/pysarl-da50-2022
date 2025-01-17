from vm.builtin.service.LifecycleService import LifecycleService
from vm.builtin.service.LoggingService import LoggingService
from vm.builtin.skill.SreDynamicSkillProvider import SreDynamicSkillProvider


class Kernel:

    def __init__(self):
        self.__lifecycleService = LifecycleService(SreDynamicSkillProvider())
        self.__loggingService = LoggingService()

    def start(self, bootAgentClass, *parameters):
        self.__lifecycleService.createAgent(bootAgentClass, None, None, *parameters)
        self.__lifecycleService.killAgentsLeft()
