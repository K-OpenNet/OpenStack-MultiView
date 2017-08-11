/**
 * @author Muhammad Usman
 * @version 0.1
 */

package smartx.multiview.collectors.flow;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.bson.Document;

import smartx.multiview.DataLake.MongoDB_Connector;

public class IOVisorKafkaConsumer {
	private String iovisorMongoCollection = "flow-iovisor-data";
	private String bootstrapServer;
	private String topic = "iovisor";
    
    private MongoDB_Connector mongoConnector;
    private Document document;
    private Date timestamp;
	
	private Logger LOG = Logger.getLogger("sFlowKafka");
    
    public IOVisorKafkaConsumer(String bootstrapserver, MongoDB_Connector MongoConn) 
    {
    	bootstrapServer        = bootstrapserver;
    	mongoConnector         = MongoConn;
    }
    
    public void Consume(){
    	//Kafka & Zookeeper Properties
    	Properties props = new Properties();
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
     }
    
    public void StoreToDB(String record)
    {
    	timestamp = new Date();
    	document = new Document();
		document.put("timestamp",   timestamp);
		document.put("packetdata",  record);
		
		mongoConnector.getDbConnection().getCollection(iovisorMongoCollection).insertOne(document);
		document.clear();
    }
    
    
}



