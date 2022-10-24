from contribs.io.sarl.pythongenerator.vm.builtin.service.LifecycleService import LifecycleService
from contribs.io.sarl.pythongenerator.vm.builtin.skill.SreDynamicSkillProvider import SreDynamicSkillProvider


class Kernel:

    def __init__(self):
        self.__lifecycleService = LifecycleService(SreDynamicSkillProvider())

    def start(self, bootAgentClass):
        self.__lifecycleService.createAgent(bootAgentClass)
