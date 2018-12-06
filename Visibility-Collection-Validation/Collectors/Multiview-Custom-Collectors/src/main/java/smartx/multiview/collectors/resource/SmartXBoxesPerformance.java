/**
 * @author Muhammad Usman
 * @version 0.1
 */

package smartx.multiview.collectors.resource;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.Document;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;

import smartx.multiview.DataLake.MongoDB_Connector;

public class SmartXBoxesPerformance implements Runnable{
	private Thread thread;
	private String ThreadName = "SmartX Box Performance Metrics Thread";
	
	private String bootstrapServer;

	private String topic = "snap-pbox-visibility";
	private String ESindex = "smartx-boxes-metrics";
	
	@SuppressWarnings("rawtypes")
	private Map data = new HashMap();
	private Map SmartX_Box_List = new HashMap();
	private Map SmartX_Box_Mgmt_NIC = new HashMap();
	private Map SmartX_Box_Ctrl_NIC = new HashMap();
	private Map SmartX_Box_Data_NIC = new HashMap();
	
	TransportClient client;
	private long index = 0;

	private Date timestamp;

	private KafkaConsumer<String, String> consumer;
	
	private MongoDB_Connector mongoConnector;
	private List<Document> documentsRT = new ArrayList<Document>();
	private FindIterable<Document> SmartXBoxInterfaces;
	private String SmartXBoxInterfacesMongoCollection="pbox-nic-tags";

	@SuppressWarnings({ "resource", "unchecked" })
	public SmartXBoxesPerformance(MongoDB_Connector MongoConn, String bootstrapserver, String ElasticHost, int ElasticPort) {
		bootstrapServer = bootstrapserver;
		mongoConnector  = MongoConn;
		try {
			client = new PreBuiltTransportClient(Settings.EMPTY)
					.addTransportAddress(new TransportAddress(InetAddress.getByName(ElasticHost), ElasticPort));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		boolean indexStatus = client.admin().indices().prepareExists(ESindex).execute().actionGet().isExists();
		if (!indexStatus) {
			System.out.println("Index Does not Exist");
			client.admin().indices().prepareCreate(ESindex).execute().actionGet();
		} else {
			SearchHits resp = client.prepareSearch(ESindex).get().getHits();
			index = resp.getTotalHits();
			System.out.println("Total Records in Index: " + index);
		}
		
		//Get List of SmartX Boxes
		SmartXBoxInterfaces = mongoConnector.getDataDB(SmartXBoxInterfacesMongoCollection);
		
		SmartXBoxInterfaces.forEach(new Block<Document>() {
		    public void apply(final Document document) {
		    	
		    	SmartX_Box_List.put((String) document.get("boxName"), (String) document.get("boxName")) ;
		    	SmartX_Box_Mgmt_NIC.put((String) document.get("boxName"), (String) document.get("management"));
		    	SmartX_Box_Ctrl_NIC.put((String) document.get("boxName"), (String) document.get("control"));
		    	SmartX_Box_Data_NIC.put((String) document.get("boxName"), (String) document.get("data"));
			}
		});
	}

	public void Consume() {
		// Kafka & Zookeeper Properties
		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrapServer);
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList(topic));

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(0);
			for (ConsumerRecord<String, String> record : records) {
				this.StoreToDB(record.value());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void StoreToDB(String record) {
		timestamp = new Date();
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		JSONObject json2 = null;
		String keyName = null;
		String boxID = null;
		Float keyValue = null;

		try {
			JSONArray array = (JSONArray) parser.parse(record);
			
			json2 = (JSONObject) ((JSONObject) array.get(0)).get("tags");
			boxID = json2.get("BoxID").toString();
			data.put("BoxID", boxID);
			System.out.println(boxID);
			if (SmartX_Box_List.containsKey(boxID))
			{
				for (int i = 0; i < array.size(); i++) {
					json = (JSONObject) array.get(i);
					keyName = json.get("namespace").toString();

					if (keyName.contains("/intel/psutil/load/load1")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("load1", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/psutil/load/load5")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("load5", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/psutil/load/load15")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("loadfifteen", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/linux/iostat/avg-cpu/%system")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("system", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/linux/iostat/avg-cpu/%nice")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("nice", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/linux/iostat/avg-cpu/%user")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("user", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/linux/iostat/avg-cpu/%iowait")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("iowait", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/linux/iostat/avg-cpu/%steal")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("steal", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/linux/iostat/avg-cpu/%idle")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("idle", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/meminfo/mem_total")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mem_total", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/meminfo/mem_used")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mem_used", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/meminfo/mem_free")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mem_free", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/meminfo/buffers")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("buffers", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/meminfo/cached")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("cached", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/meminfo/mem_available")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mem_available", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Mgmt_NIC.get(boxID) +"/bytes_sent")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mgmt_bytes_sent", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Mgmt_NIC.get(boxID) +"/bytes_recv")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mgmt_bytes_recv", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Mgmt_NIC.get(boxID) +"/packets_sent")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mgmt_packets_sent", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Mgmt_NIC.get(boxID) +"/packets_recv")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("mgmt_packets_recv", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Ctrl_NIC.get(boxID) +"/bytes_sent")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("ctrl_bytes_sent", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Ctrl_NIC.get(boxID) +"/bytes_recv")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("ctrl_bytes_recv", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Ctrl_NIC.get(boxID) +"/packets_sent")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("ctrl_packets_sent", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Ctrl_NIC.get(boxID) +"/packets_recv")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("ctrl_packets_recv", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Data_NIC.get(boxID) +"/bytes_sent")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("data_bytes_sent", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Data_NIC.get(boxID) +"/bytes_recv")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("data_bytes_recv", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Data_NIC.get(boxID) +"/packets_sent")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("data_packets_sent", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/iface/"+ SmartX_Box_Data_NIC.get(boxID) +"/packets_recv")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("data_packets_recv", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/filesystem/rootfs/device_name")) {
						data.put("device_name", json.get("data").toString());
						/*System.out.println(keyName);
						System.out.println(json.get("data").toString());*/
					} else if (keyName.contains("/intel/procfs/filesystem/rootfs/space_used")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("space_used", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					} else if (keyName.contains("/intel/procfs/filesystem/rootfs/space_free")) {
						keyValue = Float.parseFloat(json.get("data").toString());
						data.put("space_free", keyValue);
						/*System.out.println(keyName + ": " + keyValue);*/
					}
				}

				// Insert data into Elasticsearch.
				try {
					XContentBuilder builder = jsonBuilder()
							.startObject()
							.field("@version", "1")
							.field("@timestamp", timestamp).field("BoxID", data.get("BoxID").toString())
							.field("load1", data.get("load1")).field("load5", data.get("load5"))
							.field("load15", data.get("loadfifteen")).field("system", data.get("system"))
							.field("user", data.get("user")).field("steal", data.get("steal"))
							.field("nice", data.get("nice")).field("iowait", data.get("iowait"))
							.field("idle", data.get("idle")).field("mem_total", data.get("mem_total"))
							.field("mem_used", data.get("mem_used")).field("mem_free", data.get("mem_free"))
							.field("mem_available", data.get("mem_available")).field("buffers", data.get("buffers"))
							.field("cached", data.get("cached")).field("device_name", data.get("device_name"))
							.field("space_total",
									Float.parseFloat(data.get("space_used").toString())
											+ Float.parseFloat(data.get("space_free").toString()))
							.field("space_used", data.get("space_used")).field("space_free", data.get("space_free"))
							.field("mgmt_interface", SmartX_Box_Mgmt_NIC.get(boxID))
							.field("mgmt_bytes_sent", data.get("mgmt_bytes_sent"))
							.field("mgmt_bytes_recv", data.get("mgmt_bytes_recv"))
							.field("mgmt_packets_sent", data.get("mgmt_packets_sent"))
							.field("mgmt_packets_recv", data.get("mgmt_packets_recv"))
							.field("ctrl_interface", SmartX_Box_Ctrl_NIC.get(boxID))
							.field("ctrl_bytes_sent", data.get("ctrl_bytes_sent"))
							.field("ctrl_bytes_recv", data.get("ctrl_bytes_recv"))
							.field("ctrl_packets_sent", data.get("ctrl_packets_sent"))
							.field("ctrl_packets_recv", data.get("ctrl_packets_recv"))
							.field("data_interface", SmartX_Box_Data_NIC.get(boxID))
							.field("data_bytes_sent", data.get("data_bytes_sent"))
							.field("data_bytes_recv", data.get("data_bytes_recv"))
							.field("data_packets_sent", data.get("data_packets_sent"))
							.field("data_packets_recv", data.get("data_packets_recv"))
							.endObject();
					index = index + 1;
					//System.out.println(index);
					client.prepareIndex(ESindex, "instance", index + "").setSource(builder).execute();
					data.clear();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			this.Consume();
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}

