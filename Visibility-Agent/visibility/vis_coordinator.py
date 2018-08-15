import logging
import yaml
import influxdb
import json
import zmq


class VisibilityCoordinator:
    def __init__(self, config):
        self._logger = logging.getLogger(self.__class__.__name__)

        self._config = config
        self._tr = None
        self._mq = None

        self._zmq_context = None
        self._zmq_sock = None

        self._logger.debug(config)

    # def add_point(self, point_instance):
    #     self._point_list.add_point(point_instance)
    #
    # def delete_point(self, point_instance):
    #     pass
    #
    # class WorkingPointList(threading.Thread):
    #     def __init__(self):
    #         super(VisibilityCoordinator.WorkingPointList, self).__init__()
    #         self.point_list = list()
    #         self._logger = logging.getLogger(self.__class__.__name__)
    #
    #     def add_point(self, point_instance):
    #         self.point_list.append(point_instance)
    #
    #     def delete_point(self, point_instance):
    #         self.point_list.remove(point_instance)

    def prepare(self):
        self._prepare_transfer(self._config.get("visibility_coordinator").get("data_transfer"))
        self._prepare_msg_queue(self._config.get("visibility_coordinator").get("msg_queue"))

    def _prepare_transfer(self, conf_transfers):
        self._logger.debug(conf_transfers)
        conf_t = conf_transfers[0]
        if conf_t.get("type") == "influxdb":
            self._tr = self._get_influx_client(conf_t)

    def _get_influx_client(self, conf_influx):
        self._logger.debug(conf_influx)
        client = influxdb.InfluxDBClient(conf_influx.get("ipaddress"),
                                         conf_influx.get("port"),
                                         conf_influx.get("id"),
                                         conf_influx.get("password"),
                                         None)
        return client

    def _prepare_msg_queue(self, conf_mq):
        self._zmq_context = zmq.Context()
        self._zmq_sock = self._zmq_context.socket(zmq.PULL)
        self._zmq_sock.bind("tcp://{}:{}".format(conf_mq.get("ipaddress"), conf_mq.get("port")))

    def run(self):
        while True:
            recv_str = self._zmq_sock.recv()
            self._logger.debug(recv_str)
            self._send_collected_data(recv_str)

    def _send_collected_data(self, msg):
        self._logger.debug(msg)
        msgs = msg.split('/')
        database = msgs[0]
        data = json.loads(msgs[1])
        self._logger.debug(data)
        self._tr.switch_database(database)
        self._tr.write_points(data)


if __name__ == "__main__":
    logging.basicConfig(format="[%(asctime)s / %(levelname)s / %(filename)s, %(funcName)s (#%(lineno)d)] %(message)s",
                        level=logging.INFO)
    with open("config.yaml") as f:
        config_str = f.read()
        coor_config = yaml.load(config_str)
    vis_coordi = VisibilityCoordinator(coor_config)
    vis_coordi.prepare()
    vis_coordi.run()
