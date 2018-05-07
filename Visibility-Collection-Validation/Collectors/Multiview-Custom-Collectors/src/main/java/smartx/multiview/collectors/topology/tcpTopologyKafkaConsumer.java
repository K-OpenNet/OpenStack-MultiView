/**
 * @author Muhammad Ahmad
 * @version 0.1
 */

package smartx.multiview.collectors.topology;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.bson.Document;

import smartx.multiview.DataLake.MongoDB_Connector;

public class tcpTopologyKafkaConsumer implements Runnable{
	private Thread thread;
	private String iovisorMongoCollection = "flow-iovisor-data"; //Change collection name
	private String bootstrapServer;
	private String topic = "iovisor"; //Change Topic name
	private String ThreadName = "TCP Topology Thread";
    
    private MongoDB_Connector mongoConnector;
    private Document document;
    private Date timestamp;
	
	private Logger LOG = Logger.getLogger("IOVisorKafka");
    
    public tcpTopologyKafkaConsumer(String bootstrapserver, MongoDB_Connector MongoConn) 
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
            	System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                this.StoreToDB(record.value());
		    }
        }
     }
    
    public void StoreToDB(String record)
    {
    	timestamp = new Date();
    	document = new Document();
		
    	// Extract values and add as key-value in MongoDB Collection
    	document.put("timestamp",   timestamp);
		document.put("packetdata",  record);
		
		mongoConnector.getDbConnection().getCollection(iovisorMongoCollection).insertOne(document);
		document.clear();
    }
    
    public void run() 
	{
		System.out.println("Running "+ThreadName);
		this.Consume();
	}
	public void start() {
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}



