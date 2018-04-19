package smartx.multiview.workers;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.bson.BSONObject;

import com.mongodb.*;
import com.mongodb.hadoop.BSONFileOutputFormat;
import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.MongoOutputFormat;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;
import scala.Tuple2;
import static org.elasticsearch.common.xcontent.XContentFactory.*;

@SuppressWarnings("deprecation")
public class CorrelatorWorker {
    private static Logger logger = Logger.getLogger(CorrelatorWorker.class.getName());
    private static Mongo mongo;
    private static DB db;
    private long index1, index2, index3, index4;
    private DBCollection collection;
    private Client client;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
    private WriteConcern write=new WriteConcern();
    private BasicDBObject document = new BasicDBObject();
    private DBCollection collectioninsert;
    private Date startDate;// = new Date(System.currentTimeMillis() - 3600 * 1000);
    private Date endDate;// = new Date();
    private String key = "192.168.1.1,8081,192.168.1.4,46351,eth.ip.tcp", srcIP, srcPort, destIP, destPort, protocol, srcpBox, destpBox;
    private Date FlowStart, FlowStop;
    private int packetsent,packetrec;

    @SuppressWarnings({ "deprecation", "unused", "resource" })
    public CorrelatorWorker(String zookeeper, String groupId, String CollectionName, Date start, Date end) {
        startDate = start;
        endDate   = end;
	
    	//MongoDB Properties
        mongo = new Mongo("103.22.221.55", 27017);
        db = mongo.getDB("smartxdb");
        
        //Elasticsearch Properties
         Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
         try {
			client = TransportClient.builder().build().addTransportAddress(new  InetSocketTransportAddress(InetAddress.getByName("103.22.221.55"), 9300));
         } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
         }
         
         //Check Elasticsearch Index Already Exists or not
         boolean indexStatus = client.admin().indices().prepareExists("multi-level-1").execute().actionGet().isExists();
         if (indexStatus)
         {
         	client.admin().indices().prepareDelete("multi-level-1").execute().actionGet();
         }
         //Create Elasticsearch Index
         CreateIndexResponse response = client.admin().indices().prepareCreate("multi-level-1").execute().actionGet();
         
         indexStatus = client.admin().indices().prepareExists("multi-level-2").execute().actionGet().isExists();
         if (indexStatus)
         {
         	client.admin().indices().prepareDelete("multi-level-2").execute().actionGet();
         }
         //Create Elasticsearch Index
         response = client.admin().indices().prepareCreate("multi-level-2").execute().actionGet();
         
         indexStatus = client.admin().indices().prepareExists("multi-level-4").execute().actionGet().isExists();
         if (indexStatus)
         {
         	client.admin().indices().prepareDelete("multi-level-4").execute().actionGet();
         }
         //Create Elasticsearch Index
         response = client.admin().indices().prepareCreate("multi-level-4").execute().actionGet();
         
         CountResponse response1 = client.prepareCount("multi-level-1").setQuery(termQuery("_type", "flow")).execute().actionGet();
         index1=response1.getCount();
         CountResponse response2 = client.prepareCount("multi-level-2").setQuery(termQuery("_type", "instance")).execute().actionGet();
         index2=response2.getCount();
         CountResponse response4 = client.prepareCount("multi-level-4").setQuery(termQuery("_type", "snmp")).execute().actionGet();
         index4=response4.getCount();
         
         //client.prepareDelete("multi-level-1", "flow", "1").setRefresh(true).execute().actionGet(); 
         
         //System.out.println("Context Size:"+response2.getCount());
    }
    
    //Calculate Flow Statistics
    public void function1()
    {
    	srcIP    = key.substring(0,StringUtils.ordinalIndexOf(key, ",", 1));
    	srcPort  = key.substring(StringUtils.ordinalIndexOf(key, ",", 1)+1,StringUtils.ordinalIndexOf(key, ",", 2));
    	destIP   = key.substring(StringUtils.ordinalIndexOf(key, ",", 2)+1,StringUtils.ordinalIndexOf(key, ",", 3));
    	destPort = key.substring(StringUtils.ordinalIndexOf(key, ",", 3)+1,StringUtils.ordinalIndexOf(key, ",", 4));
    	protocol = key.substring(StringUtils.ordinalIndexOf(key, ",", 4)+1);
    	
    	collection = db.getCollection("flowlevel-data");
    	
    	BasicDBObject whereQuery1 = new BasicDBObject("key", key).append("timestamp", new BasicDBObject("$gte",startDate).append("$lte", endDate)).append("srcBox", "SmartX-BPlus-GIST");
    	BasicDBObject selectedFields1 = new BasicDBObject("key",1).append("srcHost", 1).append("destHost", 1).append("timestamp",1);
    	System.out.println(whereQuery1.toString());
    	
    	DBCursor curs = collection.find(whereQuery1, selectedFields1).sort(new BasicDBObject("timestamp",1)); 
    	
    	Iterator<DBObject> fields1 = curs.iterator(); 
    	packetsent = curs.size();
    	
    	DBObject curs1 = collection.findOne(whereQuery1, selectedFields1);
    	DBCursor curs2 = collection.find(whereQuery1, selectedFields1).sort(new BasicDBObject("timestamp",-1)).limit(1);
    	
    	   	
    	FlowStart = (Date) curs1.get("timestamp");
    	FlowStop  = (Date) curs2.iterator().next().get("timestamp");
    	
    	try {
        	
        	XContentBuilder builder = jsonBuilder()
        			.startObject()
        				.field("@version", "1")
        				.field("@timestamp", endDate)
        				.field("Source Host", srcIP)
        				.field("Destination Host", destIP)
        				.field("Source Port", srcPort)
        				.field("Destination Port", destPort)
        				.field("Protocol", protocol)
        				.field("First Packet", FlowStart)
        				.field("Last Packet", FlowStop)
        				.field("Packets Sent", packetsent)
        				.field("pBox","SmartX-BPlus-GIST")
        			.endObject();
        	index1=index1+1;
            client.prepareIndex("multi-level-1","flow", index1+"").setSource(builder).execute();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    	System.out.println("First Packet: "+FlowStart+ " Last Packet: "+FlowStop+" Source Host: "+srcIP+" Source Port: "+srcPort+" Destination Host: "+destIP+" Destination Port: "+destPort+" Protocol: "+protocol+" Packets Sent: "+packetsent);
    	
    	whereQuery1 = new BasicDBObject("key", key).append("timestamp", new BasicDBObject("$gte",startDate).append("$lte", endDate)).append("srcBox", "Smartx-BPlus-ID");
    	curs = collection.find(whereQuery1, selectedFields1).sort(new BasicDBObject("timestamp",1)); 
    	
    	fields1 = curs.iterator(); 
    	packetsent = curs.size();
    	
    	curs1 = collection.findOne(whereQuery1, selectedFields1);
    	curs2 = collection.find(whereQuery1, selectedFields1).sort(new BasicDBObject("timestamp",-1)).limit(1);
    	
    	   	
    	FlowStart = (Date) curs1.get("timestamp");
    	FlowStop  = (Date) curs2.iterator().next().get("timestamp");
    	try {
        	
        	XContentBuilder builder = jsonBuilder()
        			.startObject()
        				.field("@version", "1")
        				.field("@timestamp", endDate)
        				.field("Source Host", srcIP)
        				.field("Destination Host", destIP)
        				.field("Source Port", srcPort)
        				.field("Destination Port", destPort)
        				.field("Protocol", protocol)
        				.field("First Packet", FlowStart)
        				.field("Last Packet", FlowStop)
        				.field("Packets Sent", packetsent)
        				.field("pBox","Smartx-BPlus-ID")
        			.endObject();
        	index1=index1+1;
            client.prepareIndex("multi-level-1","flow", index1+"").setSource(builder).execute();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    	System.out.println("First Packet: "+FlowStart+ " Last Packet: "+FlowStop+" Source Host: "+srcIP+" Source Port: "+srcPort+" Destination Host: "+destIP+" Destination Port: "+destPort+" Protocol: "+protocol+" Packets Sent: "+packetsent);
    }
    
    //Find OpenStack Instance Statistics
    public void function2()
    {
    	collection = db.getCollection("resourcelevel-os-instance-detail");
    	System.out.println("Collection"+collection.getFullName());
    	srcIP = "192.168.1.2";
    	BasicDBObject whereQuery2   = new BasicDBObject("data_address", srcIP)
    									  .append("timestamp", new BasicDBObject("$gte",startDate)
    									  .append("$lte", endDate));
    	BasicDBObject selectfields2 = new BasicDBObject("host",1).append("state", 1)
    									  .append("status", 1)
    								      .append("timestamp",1)
    								      .append("created", 1)
    								      .append("launched_at", 1)
    								      .append("updated", 1)
    								      .append("kvm_instance_name", 1);
    	System.out.println(whereQuery2.toString());
    	
    	DBCursor curs3 = collection.find(whereQuery2, selectfields2).sort(new BasicDBObject("timestamp",1)); 
    	curs3.batchSize(1000);
    	
    	Iterator<DBObject> fields2 = curs3.iterator(); 
    	int count2 = curs3.size();
    	System.out.println("OpenStack VM Documents: "+count2);
    	
    	String vcpus="", cputime="", usedMemory="", instanceName="", state="", status="", created="", started="", updated="";
    	while(curs3.hasNext())
    	{
    		DBObject obj = curs3.next();
            state = (String)obj.get("state");
            status = (String)obj.get("status");
            srcpBox = (String)obj.get("host");
            created = (String)obj.get("created");
            started = (String)obj.get("launched_at");
            updated = (String)obj.get("updated");
            instanceName = (String)obj.get("kvm_instance_name");
            //ObjectId id = (ObjectId)obj.get("_id");
    		
    		//System.out.println("Index: "+ index1+" First Packet: "+FlowStart+ " Last Packet: "+FlowStop+" Source Host: "+srcIP+" Source Port: "+srcPort+" Destination Host: "+destIP+" Destination Port: "+destPort+" Protocol: "+protocol+" Packets Sent: "+packetsent+" Source Box: " +srcpBox+" Created: "   +created+" Launched At: "   +started+" Updated: "   +updated+" State: "   +state+" Status: "   +status+" instance: "+instanceName);
    		index2++;
    		
    		try {
            	XContentBuilder builder = jsonBuilder()
            			.startObject()
            				.field("@version", "1")
            				.field("@timestamp", endDate)
            				.field("Host", srcpBox)
            				.field("Instance Created", created)
            				.field("Instance Started", started)
            				.field("Instance Updated", updated)
            				.field("Instance Name", instanceName)
            				.field("Instance State", state)
            				.field("Instance Status", status)
            			.endObject();
            	
                client.prepareIndex("multi-level-2","instance", index2+"").setSource(builder).execute();
                index2=index2+1;
            }
            catch (IOException e) {
              e.printStackTrace();
            }
    	}
    }
    
    //Find OpenStack Instance Performance Statistics
    public void function3()
    {
    	//For VM Perforamnce Data
    }
    
    //Find pBox Performance Statistics    
    public void function4()
    {
    	collection = db.getCollection("resourcelevel-performance");
    	System.out.println("Collection"+collection.getFullName());
    	BasicDBObject whereQuery4 = new BasicDBObject("box", srcpBox).append("timestamp", new BasicDBObject("$gte",startDate)
    								.append("$lte", endDate));
    	BasicDBObject selectfields4 = new BasicDBObject("timestamp",1).append("CPU Load 5 Min", 1).append("Ram Used GB", 1).append("Ram Free GB", 1).append("Ram Used GB", 1).append("Disk Used Space GB", 1).append("Disk Free Space GB", 1).append("Disk Used Space GB", 1).append("Description", 1);
    								  
    	System.out.println(whereQuery4.toString());
    	
    	DBCursor curs5 = collection.find(whereQuery4, selectfields4).sort(new BasicDBObject("timestamp",1)); 
    	curs5.batchSize(1000);
    	
    	Iterator<DBObject> fields4 = curs5.iterator(); 
    	int count4 = curs5.size();
    	System.out.println("SNMP Documents: "+count4);
    	
    	Date timestamp4;
    	Double ramused, cpuload;
    	Long diskused, ramfree, diskfree;
    	String description;
    	//int index2=1;
    	while(curs5.hasNext())
    	{
    		DBObject obj = curs5.next();
            timestamp4 = (Date)obj.get("timestamp");
            cpuload = (Double)obj.get("CPU Load 5 Min");
            ramused = (Double)obj.get("Ram Used GB");
            ramfree = (Long)obj.get("Ram Free GB");
            diskused = (Long)obj.get("Disk Used Space GB");
            diskfree = (Long)obj.get("Disk Free Space GB");
            description = (String)obj.get("Description");
            
    		try {
            	XContentBuilder builder = jsonBuilder()
            			.startObject()
            				.field("@version", "1")
            				.field("@timestamp", timestamp4)
            				.field("caltime", endDate)
            				.field("Source Host", srcIP)
            				.field("Destination Host", destIP)
            				.field("CPU Load 5 Min", cpuload)
            				.field("Memory Used", ramused)
            				.field("Memory Free", ramfree)
            				.field("Disk Used", diskused)
            				.field("Disk Free", diskfree)
            				.field("Description", description)
            			.endObject();
            	
                client.prepareIndex("multi-level-4","snmp", index4+"").setSource(builder).execute();
                index4=index4+1;
            }
            catch (IOException e) {
              e.printStackTrace();
            }
    		System.out.println("Index: "+ index4+" Timestamp: "+timestamp4+ " Description: "+description+" CPU Load: "+cpuload+" Ram Used (GB): "+ramused+" Ram Free (GB): "+ramfree+" Disk Used (GB): "+diskused+" Disk Free (GB): "+diskfree);
    	}
    }
    
    //Execute Relating Functions
    public void Correlate(String keyValue) {
    	function1();
    	function2();
    	function4();
    	
    	mongo.close();
    	
    	/*// Set configuration options for the MongoDB Hadoop Connector.
        Configuration mongodbConfig = new Configuration();
        // MongoInputFormat allows us to read from a live MongoDB instance.
        mongodbConfig.set("mongo.job.input.format", "com.mongodb.hadoop.MongoInputFormat");
        // MongoDB connection string naming a collection to use. 
        mongodbConfig.set("mongo.input.uri", "mongodb://103.22.221.55:27017/smartxdb.flowlevel-data");
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateString = " 2015-11-10 11:10:00";
        Date currentDate = new Date(System.currentTimeMillis() - 3600 * 1000);
        
        //new ISODate('2015-11-10T11:10:00.000Z')
        try {
			Date dateObject = sdf.parse(dateString);
			System.out.println("Test: "+dateObject);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        mongodbConfig.set("mongo.input.query", "{'key':"+keyValue+",'timestamp':{'$gt':"+currentDate+"}}");
        //mongodbConfig.set("mongo.input.query", "{'key':"+keyValue+" , 'timestamp': {'$gt':'2015-11-10T11:10:00.000Z',  '$lt':'2015-11-10T11:14:00.000Z'}}");
        
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Correlation Application");
        JavaSparkContext sc = new JavaSparkContext(conf);
        
		// Create an RDD backed by the MongoDB collection.
        JavaPairRDD<Object, BSONObject> documents = sc.newAPIHadoopRDD(
            mongodbConfig,            // Configuration
            MongoInputFormat.class,   // InputFormat: read from a live cluster.
            Object.class,             // Key class
            BSONObject.class          // Value class
        );
        
        System.out.println("Entering App: ");
        System.out.println("******************************************************************************");
        System.out.println("Total Documents: "+documents.count());
        System.out.println("******************************************************************************");
        
        
        
        PrintWriter writer;
		try {
			writer = new PrintWriter("/home/netcs/result11.txt", "UTF-8");
			writer.println("The first line");
			writer.println(new Date());
	        writer.println("Total Records:"+documents.count());
	        writer.println("First Record:"+documents.first());
	        
	        //System.out.println(documents.collect());
	        List<Tuple2<Object, BSONObject>> result = documents.collect();
	        for (Tuple2<?, ?> tuple : result)
	        {
	        	writer.println(tuple._1+":"+tuple._2);
	        }
	        Tuple2<Object, BSONObject> a=documents.first();
	        writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        */
		
        
    	
        
  /*      // Create a separate Configuration for saving data back to MongoDB.
        Configuration outputConfig = new Configuration();
        outputConfig.set("mongo.output.uri","mongodb://103.22.221.55:27017/output.collection");

        // Save this RDD as a Hadoop "file".
        // The path argument is unused; all documents will go to 'mongo.output.uri'.
        documents.saveAsNewAPIHadoopFile(
            "file:///this-is-completely-unused",
            Object.class,
            BSONObject.class,
            MongoOutputFormat.class,
            outputConfig
        );

        // We can also save this back to a BSON file.
        documents.saveAsNewAPIHadoopFile(
            "hdfs://localhost:8020/user/spark/bson-demo",
            Object.class,
            BSONObject.class,
            BSONFileOutputFormat.class,
            new Configuration()
        );*/
    }
}