/**
 * @author Muhammad Usman
 * @version 0.2
 */

package smartx.multiview.collectors.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import smartx.multiview.DataLake.Elasticsearch_Connector;
import smartx.multiview.DataLake.MongoDB_Connector;

public class sFlowKafkaConsumer {
	private String sFlowMongoCollection;
	private String bootstrapServer;

	private String topic   = "sFlow";
	private String ESindex = "flow-sampling-data";

	private MongoDB_Connector mongoConnector;
	private Elasticsearch_Connector ESConnector;

	private Document document;
	private List<Document> documentsRT = new ArrayList<Document>();
	private FindIterable<Document> Tenant_VLAN_List;
	private String tenant_VLAN_MongoCollection;
	
	private String VLANIDPacket="", VLANIDDB="", TenantName="";

	private Date timestamp;

	private Logger LOG = Logger.getLogger("sFlowKafka");

	public sFlowKafkaConsumer(String bootstrapserver, MongoDB_Connector MongoConn, Elasticsearch_Connector ESConn, String sflowCollection, String[] boxType, String tenantvlanmongocollection) {
		bootstrapServer = bootstrapserver;
		mongoConnector = MongoConn;
		sFlowMongoCollection = sflowCollection;
		tenant_VLAN_MongoCollection = tenantvlanmongocollection;
		ESConnector = ESConn;
		ESConnector.createIndex(ESindex);
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
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList(topic));

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(0);
			for (ConsumerRecord<String, String> record : records) {
				// System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(),
				// record.key(), record.value());
				this.StoreToDB(record.value());
			}
		}
	}

	public void StoreToDB(String record)
    {
    	timestamp = new Date();
    	String agentBox, flowKey, TLProtocol;
    	float dataBytes, frameSize; 
    	
    	//Get List of tenant-VLAN Mappings
    	Tenant_VLAN_List = mongoConnector.getDataDB(tenant_VLAN_MongoCollection);
    	
    	JSONParser parser = new JSONParser();
		JSONObject json;

		try 
		{
			json = (JSONObject) parser.parse(record);
		
    		if (json.containsKey("UDPFlowDetail"))
			{
				JSONArray array = (JSONArray) json.get("UDPFlowDetail");
		        //System.out.println(array.get(0));
		        //System.out.println(array.size());
		        
		        for (int i=0; i< array.size(); i++)
		        {
		        	json =(JSONObject)  array.get(i);
		        	
					flowKey      = json.get("FlowKey").toString();
					VLANIDPacket = flowKey.substring(0, StringUtils.ordinalIndexOf(flowKey, ",", 1)).trim();
					TLProtocol   = "UDP";
					agentBox     = json.get("AgentID").toString();
					dataBytes    = Float.parseFloat(json.get("Bytes").toString());
					
					Tenant_VLAN_List.forEach(new Block<Document>() {
						public void apply(final Document document) {
							VLANIDDB = (String) document.get("vlan");
					    	if (VLANIDPacket.equals(VLANIDDB))
					    	{
					    		TenantName = (String) document.get("tenantname");
					    	}
					    }
			    	});
					
					if (TenantName.equals(""))
					{
						TenantName = "OpenFlow";
					}
					
					if (json.get("FrameSize")==null)
					{
						frameSize = 0;
					}
					else
					{
						frameSize  = Float.parseFloat(json.get("FrameSize").toString());
					}
					
					document = new Document();
					document.put("timestamp",         timestamp);
					document.put("AgentID",           agentBox);
					document.put("VLANID",            VLANIDPacket);
					document.put("Tenant",            TenantName);
					document.put("TransportProtocol", TLProtocol);
		    		document.put("Flowkey",           flowKey);
		    		document.put("Bytes",             dataBytes);
		            document.put("FrameSize",         frameSize);
		            
		            documentsRT.add(document);
		            
		            ESConnector.insertData(ESindex, timestamp, flowKey, VLANIDDB, TenantName, TLProtocol, agentBox, dataBytes, frameSize);
		            LOG.debug("[AgentID - "+agentBox+"][FlowKey - "+flowKey+"][ Bytes - "+dataBytes+"][FrameSize - "+frameSize+"]");
		            
		            TenantName = "";
		            VLANIDDB = "";
				}
		    }
			else if (json.containsKey("TCPFlowDetail"))
			{
			    JSONArray array = (JSONArray) json.get("TCPFlowDetail");
		        for (int i=0; i< array.size(); i++)
		        {
		        	json =(JSONObject)  array.get(i);
		        	
			        flowKey    = json.get("FlowKey").toString();
			        VLANIDPacket = flowKey.substring(0, StringUtils.ordinalIndexOf(flowKey, ",", 1)).trim();
					TLProtocol = "TCP";
					agentBox   = json.get("AgentID").toString();
					dataBytes  = Float.parseFloat(json.get("Bytes").toString());
					frameSize  = Float.parseFloat(json.get("FrameSize").toString());
					
					Tenant_VLAN_List.forEach(new Block<Document>() {
						public void apply(final Document document) {
							VLANIDDB = (String) document.get("vlan");
					    	if (VLANIDPacket.equals(VLANIDDB))
					    	{
					    		TenantName = (String) document.get("tenantname");
					    	}
					    }
			    	});
					
					if (TenantName.equals(""))
					{
						TenantName = "OpenFlow";
					}
					
					document = new Document();
					document.put("timestamp",         timestamp);
					document.put("AgentID",           agentBox);
					document.put("VLANID",            VLANIDPacket);
					document.put("Tenant",            TenantName);
					document.put("TransportProtocol", TLProtocol);
		    		document.put("Flowkey",           flowKey);
		    		document.put("Bytes",             dataBytes);
		            document.put("FrameSize",         frameSize);
		            
		            documentsRT.add(document);
		            
		            ESConnector.insertData(ESindex, timestamp, flowKey, VLANIDDB, TenantName, TLProtocol, agentBox, dataBytes, frameSize);
		            LOG.debug("[AgentID - "+agentBox+"][FlowKey - "+flowKey+"][ Bytes - "+dataBytes+"][FrameSize - "+frameSize+"]");
		            
		            TenantName = "";
		            VLANIDDB = "";
		        }
			}
			else if (json.containsKey("ICMPFlowDetail"))
			{
				JSONArray array = (JSONArray) json.get("ICMPFlowDetail");
		        for (int i=0; i< array.size(); i++)
		        {
		        	json =(JSONObject)  array.get(i);
		        	
			        flowKey    = json.get("FlowKey").toString();
			        VLANIDPacket = flowKey.substring(0, StringUtils.ordinalIndexOf(flowKey, ",", 1)).trim();
					TLProtocol = "ICMP";
					agentBox   = json.get("AgentID").toString();
					dataBytes  = Float.parseFloat(json.get("Bytes").toString());
					frameSize  = Float.parseFloat(json.get("FrameSize").toString());
					
					System.out.println("[AgentID - "+agentBox+"][FlowKey - "+flowKey+"][ Bytes - "+dataBytes+"][FrameSize - "+frameSize+"]");
		        	
		        	document = new Document();
					document.put("timestamp",         timestamp);
					document.put("AgentID",           agentBox);
					document.put("VLANID",            VLANIDPacket);
					document.put("Tenant",            TenantName);
					document.put("TransportProtocol", TLProtocol);
		    		document.put("Flowkey",           flowKey);
		    		document.put("Bytes",             dataBytes);
		            document.put("FrameSize",         frameSize);
		            
		            Tenant_VLAN_List.forEach(new Block<Document>() {
						public void apply(final Document document) {
							VLANIDDB = (String) document.get("vlan");
					    	if (VLANIDPacket.equals(VLANIDDB))
					    	{
					    		TenantName = (String) document.get("tenantname");
					    	}
					    }
			    	});
		            
		            if (TenantName.equals(""))
					{
						TenantName = "OpenFlow";
					}
		            
		            documentsRT.add(document);
		            
		            ESConnector.insertData(ESindex, timestamp, flowKey, VLANIDDB, TenantName, TLProtocol, agentBox, dataBytes, frameSize);
		            LOG.debug("[AgentID - "+agentBox+"][FlowKey - "+flowKey+"][ Bytes - "+dataBytes+"][FrameSize - "+frameSize+"]");
		            
		            TenantName = "";
		            VLANIDDB = "";
		        }
			}
			else
			{
				System.out.println("Unknown Key in JSON.");
			}
    		
    		//Insert New Documents to MongoDB Collection
    		if (documentsRT.size()>0)
    		{
    			mongoConnector.getDbConnection().getCollection(sFlowMongoCollection).insertMany(documentsRT);
    			documentsRT.clear();
    		}
			
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
