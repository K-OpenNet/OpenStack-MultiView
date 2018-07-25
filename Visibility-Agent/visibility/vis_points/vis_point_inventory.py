import os
import logging
import types

from importlib import import_module


class PointInventory:
    def __init__(self):
        self._logger = logging.getLogger(self.__class__.__name__)
        self._logger.setLevel(logging.DEBUG)

        self._import_point_modules()

    def create_point(self, point_name):
        if point_name in globals():
            point_class = globals()[point_name]
            point_instance = point_class()
            self._logger.debug("Point Instance Created: \n{}".format(point_name))
            return point_instance
        else:
            return None

    def _import_point_modules(self):
        for file_name in  os.listdir(os.path.dirname(__file__)):
            classes_in_file = list()
            if file_name.endswith(".py") and file_name not in ["__init__.py", "vis_point_inventory.py"]:
                module = import_module("vis_points.{}".format(file_name.rstrip(".py")))

                module_dict = module.__dict__
                for obj_name in module_dict.keys():
                    obj = module_dict[obj_name]
                    if isinstance(obj, types.ClassType):
                        globals()[obj_name] = obj
                        classes_in_file.append(obj_name)

                if classes_in_file.__len__() != 0:
                    self._logger.debug("Class modules in {} were loaded: {}".format(file_name, str(classes_in_file)))
