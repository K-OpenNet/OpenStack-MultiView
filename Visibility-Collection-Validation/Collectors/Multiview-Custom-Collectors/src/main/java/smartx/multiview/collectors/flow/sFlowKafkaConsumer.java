/**
 * @author Muhammad Usman
 * @version 0.1
 */

package smartx.multiview.collectors.flow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import smartx.multiview.DataLake.MongoDB_Connector;

public class sFlowKafkaConsumer {
	private Date timestamp;
	private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
	
    private String sFlowMongoCollection;
	private String bootstrapServer;
	private String topic = "sFlow";
    
    
    private MongoDB_Connector mongoConnector;
    private Document document;
    private List<Document> documentsRT = new ArrayList<Document>();
    
    private KafkaConsumer<String, String> consumer;
	
	private Logger LOG = Logger.getLogger("sFlowKafka");
    
    public sFlowKafkaConsumer(String bootstrapserver, MongoDB_Connector MongoConn, String sflowCollection, String [] boxType) 
    {
    	bootstrapServer      = bootstrapserver;
    	mongoConnector       = MongoConn;
    	sFlowMongoCollection = sflowCollection;
    }
    
    public void Consume(){
    	//Kafka & Zookeeper Properties
    	Properties props = new Properties();
        //props.put("zookeeper.connect", "103.22.221.55:2181");
        props.put("bootstrap.servers", bootstrapServer);
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
    	consumer.subscribe(Arrays.asList(topic));
    	
        while (true) 
        {
            ConsumerRecords<String, String> records = consumer.poll(0);
            for (ConsumerRecord<String, String> record : records)
            {
            	//System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                	
                this.StoreToDB(record.value());
		    }
        }
    	//String record="{\"UDPFlowDetail\":[{\"AgentID\":\"103.26.47.237\",\"FlowKey\":\"101,192.168.1.3,192.168.1.5,6,53622\",\"Bytes\":\"47.90954198962552\",\"FrameSize\":15},{\"AgentID\":\"x.x.x.x\",\"FlowKey\":\"0,0.0.0.0,0.0.0.0,0,0\",\"Bytes\":\"10\",\"FrameSize\":5}]}";
     }
    
    public void StoreToDB(String record)
    {
    	timestamp = new Date();
    	String agentBox, flowKey, TLProtocol;
    	float dataBytes, frameSize; 
    
    	JSONParser parser = new JSONParser();
		JSONObject json;
		try 
		{
			json = (JSONObject) parser.parse(record);
		
    		if (json.containsKey("UDPFlowDetail"))
			{
				//System.out.println(json.get("UDPFlowDetail"));
		        JSONArray array = (JSONArray) json.get("UDPFlowDetail");
		        //System.out.println(array.get(0));
		        //System.out.println(array.size());
		        
		        for (int i=0; i< array.size(); i++)
		        {
		        	json =(JSONObject)  array.get(i);
		        	
					flowKey    = json.get("FlowKey").toString();
					TLProtocol = "UDP";
					agentBox   = json.get("AgentID").toString();
					dataBytes  = Float.parseFloat(json.get("Bytes").toString());
					frameSize  = Float.parseFloat(json.get("FrameSize").toString());
					
					System.out.println("[AgentID - "+agentBox+"][FlowKey - "+flowKey+"][ Bytes - "+dataBytes+"][FrameSize - "+frameSize+"]");
		        	
					document = new Document();
					document.put("timestamp",         timestamp);
					document.put("AgentID",           agentBox);
					document.put("TransportProtocol", TLProtocol);
		    		document.put("Flowkey",           flowKey);
		    		document.put("Bytes",             dataBytes);
		            document.put("FrameSize",         frameSize);
		            
		            documentsRT.add(document);
		            
		            //mongoConnector.insertDataDB(sFlowMongoCollection, document);
		            //collection.insert(document,write);
		            //document.clear();*/
		        }
		    }
			else if (json.containsKey("TCPFlowDetail"))
			{
				//System.out.println(json.get("TCPFlowDetail"));
		        JSONArray array = (JSONArray) json.get("TCPFlowDetail");
		        //System.out.println(array.get(0));
		        
		        //System.out.println(array.size());
		        for (int i=0; i< array.size(); i++)
		        {
		        	json =(JSONObject)  array.get(i);
		        	
			        flowKey    = json.get("FlowKey").toString();
					TLProtocol = "TCP";
					agentBox   = json.get("AgentID").toString();
					dataBytes  = Float.parseFloat(json.get("Bytes").toString());
					frameSize  = Float.parseFloat(json.get("FrameSize").toString());
					
					System.out.println("[AgentID - "+agentBox+"][FlowKey - "+flowKey+"][ Bytes - "+dataBytes+"][FrameSize - "+frameSize+"]");
		        	
		        	document = new Document();
					document.put("timestamp",         timestamp);
					document.put("AgentID",           agentBox);
					document.put("TransportProtocol", TLProtocol);
		    		document.put("Flowkey",           flowKey);
		    		document.put("Bytes",             dataBytes);
		            document.put("FrameSize",         frameSize);
		            
		            documentsRT.add(document);
		            
		           // mongoConnector.insertDataDB(sFlowMongoCollection, document);
		           // document.clear();*/
		        }
			
			}
			else
			{
				System.out.println("Unknown Key in JSON.");
			}
    		
    		//Insert New Documents for Near-Realtime Visualization
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

