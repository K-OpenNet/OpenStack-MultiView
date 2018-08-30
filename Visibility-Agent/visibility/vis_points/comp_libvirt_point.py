import libvirt
import logging
import sys
import yaml
import json
import zmq
import socket
import time
import signal
from datetime import datetime


class ComputeLibvirtPoint:
    def __init__(self, point_config):
        self._logger = logging.getLogger()
        self._logger.setLevel(logging.INFO)

        self._point = None
        self._level = None
        self._type = None

        self._option = None

        self._mq_context = None
        self._mq_sock = None
        self._libvirt_conn = None

        self._load_config(point_config)
        self._prepare_mq_conn(point_config["msg_queue"])
        self._prepare_libvirt_conn()

        signal.signal(signal.SIGINT, self.signal_handler)
        signal.signal(signal.SIGTERM, self.signal_handler)

    def _load_config(self, point_config):
        self._point = point_config["point"]
        self._level = point_config["level"]
        self._type = point_config["type"]
        self._option = point_config["option"]

    def _prepare_mq_conn(self, mq_config):
        self._mq_context = zmq.Context()
        self._mq_sock = self._mq_context.socket(zmq.PUSH)
        self._mq_sock.connect("tcp://{}:{}".format(mq_config["ipaddress"], mq_config["port"]))
        self._logger.debug("MQ Socket is connected to {}:{}".format(mq_config["ipaddress"], mq_config["port"]))

    def _prepare_libvirt_conn(self):
        self._libvirt_conn = libvirt.open("qemu:///system")
        if self._libvirt_conn is None:
            self._logger.error("Failed to open connection to qemu:///system")
            exit(1)

    def collect(self):
        while True:
            vm_info = dict()
            domidlist = self._libvirt_conn.listDomainsID()
            for dom_id in domidlist:
                self._collect_vm(dom_id, vm_info)
                msg = self._get_influx_msg(vm_info)
                self._send_msg(msg)
            time.sleep(float(self._option["interval"]))

    def _collect_vm(self, vm_id, vm_info):
        dom = self._libvirt_conn.lookupByID(vm_id)
        if not dom.isActive():
            return None

        vm_info["vbox"] = dom.name()

        infos = dom.info()
        vm_info["mem_total"] = infos[1]
        vm_info["cpu_cores"] = infos[3]

        cpu_status = dom.getCPUStats(True)[0]
        for key in cpu_status:
            vm_info["cpu_{}".format(key)] = cpu_status[key]

        mem_status = dom.memoryStats()
        for key in mem_status:
            vm_info["mem_{}".format(key)] = mem_status[key]

    def _get_influx_msg(self, vm_info):
        # measurement: physical_compute (self._type)
        # tags: Box Name
        # fields:
        msg = dict()
        msg["measurement"] = self._type

        msg["time"] = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')

        tags = dict()
        tags["box"] = socket.gethostname()
        tags["vbox"] = str(vm_info.pop("vbox"))
        msg["tags"] = tags
        msg["fields"] = vm_info

        influx_msg = json.dumps([msg])
        return influx_msg

    def _send_msg(self, msg):
        m = msg
        if isinstance(msg, dict):
            m = json.dumps(msg)
        elif isinstance(msg, list):
            m = json.dumps(msg)
        zmq_msg = "{}/{}".format(self._level, m)
        self._logger.debug(zmq_msg)
        self._mq_sock.send_string(zmq_msg)

    def signal_handler(self, signal, frame):
        self._libvirt_conn.close()
        self._logger.info("Visibility Point {} was finished successfully".format(self.__class__.__name__))
        sys.exit(0)


if __name__ == "__main__":
    logging.basicConfig(format="[%(asctime)s / %(levelname)s] %(filename)s,%(funcName)s(#%(lineno)d): %(message)s",
                        level=logging.INFO)
    config_file = None
    point_conf = dict()

    if len(sys.argv) == 1:
        config_file = "comp_libvirt_point.yaml"
    elif len(sys.argv) == 2:
        # Load configuration from a file passed by second argument in the command
        config_file = sys.argv[1]
    else:
        exit(1)

    with open(config_file) as f:
        cfg_str = f.read()
        point_conf = yaml.load(cfg_str)
        logging.getLogger().debug(point_conf)

    point = ComputeLibvirtPoint(point_conf)
    point.collect()
