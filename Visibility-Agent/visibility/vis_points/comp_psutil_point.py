import psutil
import logging
import sys
import yaml
import json
import zmq
import socket
import time
import signal
from datetime import datetime


class ComputePsutilPoint:
    def __init__(self, point_config):
        self._logger = logging.getLogger()
        self._logger.setLevel(logging.INFO)

        self._point = None
        self._level = None
        self._type = None

        self._option = None

        self._mq_context = None
        self._mq_sock = None

        self._load_config(point_config)
        self._prepare_mq_conn(point_config["msg_queue"])

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
        self._logger.info("MQ Socket is connected to {}:{}".format(mq_config["ipaddress"], mq_config["port"]))

    def collect(self):
        comp_all_info = dict()

        while True:
            self._collect_cpu(comp_all_info)
            self._collect_mem(comp_all_info)
            msg = self._get_influx_msg(comp_all_info)
            self._send_msg(msg)
            time.sleep(float(self._option["interval"]))

    def _collect_cpu(self, comp_all_info):
        cpu_percent = psutil.cpu_percent(interval=None)
        if cpu_percent == 0:
            cpu_percent = psutil.cpu_percent(interval=None)
        comp_all_info["cpu_percent"] = cpu_percent

        cpu_time_info = psutil.cpu_times()
        for key in cpu_time_info._fields:
            value = getattr(cpu_time_info, key)
            comp_all_info["cpu_{}".format(key)] = str(value)

        cpu_stat_info = psutil.cpu_stats()
        for key in cpu_stat_info._fields:
            value = getattr(cpu_stat_info, key)
            comp_all_info["cpu_{}".format(key)] = str(value)

    def _collect_mem(self, comp_all_info):
        vmem_info = psutil.virtual_memory()
        swap_info = psutil.swap_memory()

        for key in vmem_info._fields:
            value = getattr(vmem_info, key)
            comp_all_info["mem_{}".format(key)] = value

        for key in swap_info._fields:
            value = getattr(swap_info, key)
            comp_all_info["swap_{}".format(key)] = value

    def _get_influx_msg(self, comp_all_info):
        # measurement: physical_compute (self._type)
        # tags: Box Name
        # fields:
        msg = dict()
        msg["measurement"] = self._type

        msg["time"] = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')

        tags = dict()
        tags["box"] = socket.gethostname()
        msg["tags"] = tags
        msg["fields"] = comp_all_info

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
        self._logger.info("Visibility Point {} was finished successfully".format(self.__class__.__name__))
        sys.exit(0)


if __name__ == "__main__":
    logging.basicConfig(format="[%(asctime)s / %(levelname)s] %(filename)s,%(funcName)s(#%(lineno)d): %(message)s",
                        level=logging.INFO)
    config_file = None
    point_conf = dict()

    if len(sys.argv) == 1:
        config_file = "comp_psutil_point.yaml"
    elif len(sys.argv) == 2:
        # Load configuration from a file passed by second argument in the command
        config_file = sys.argv[1]
    else:
        exit(1)

    with open(config_file) as f:
        cfg_str = f.read()
        point_conf = yaml.load(cfg_str)
        logging.getLogger().debug(point_conf)

    point = ComputePsutilPoint(point_conf)
    point.collect()
