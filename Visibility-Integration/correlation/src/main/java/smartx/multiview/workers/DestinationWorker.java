package smartx.multiview.workers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.Logger;
import com.mongodb.*;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import smartx.multiview.integrationMain.CorrelatorGUIWindow;

@SuppressWarnings("deprecation")

public class DestinationWorker {
	private static Logger logger = Logger.getLogger(CorrelatorWorker.class.getName());
    private static Mongo mongo;
    private static DB db;
    private long index1, index2, index3, index4, index8;
    private DBCollection collection;
    private Client client;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
    private WriteConcern write=new WriteConcern();
    private BasicDBObject document = new BasicDBObject();
    private Date startDate;
    private Date endDate;
    private String key = "", srcIP, srcPort, destIP, destPort, protocol, srcpBox;
    private Date FlowStart, FlowStop;
    private int packetsent;
    private CorrelatorGUIWindow GuiWindow;
    
    public DestinationWorker(String zookeeper, String mongodb, String elasticsearch, String groupId, String CollectionName, String Inputkey, Date start, Date end, String src, String dest, String srcP, String prot, CorrelatorGUIWindow window) {
    	GuiWindow = window;
    	startDate = start;
        endDate   = end;
    	key       = Inputkey;
    	srcIP     = src;
    	destIP    = dest;
    	srcPort   = srcP;
    	protocol  = prot;
    	
    	//MongoDB Properties
        mongo = new Mongo(mongodb, 27017);
        db = mongo.getDB("smartxdb");
        
    	//Elasticsearch Properties
         Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
         try {
			client = TransportClient.builder().build().addTransportAddress(new  InetSocketTransportAddress(InetAddress.getByName(elasticsearch), 9300));
         } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
         }
         
         boolean indexStatus = client.admin().indices().prepareExists("multi-level-8").execute().actionGet().isExists();
         if (indexStatus)
         {
         	client.admin().indices().prepareDelete("multi-level-8").execute().actionGet();
         }
         //Create Elasticsearch Index
         CreateIndexResponse response = client.admin().indices().prepareCreate("multi-level-8").execute().actionGet();
         
         CountResponse response1 = client.prepareCount("multi-level-1").setQuery(termQuery("_type", "flow")).execute().actionGet();
         index1=response1.getCount();
         CountResponse response2 = client.prepareCount("multi-level-2").setQuery(termQuery("_type", "instance")).execute().actionGet();
         index2=response2.getCount();
         CountResponse response8 = client.prepareCount("multi-level-8").setQuery(termQuery("_type", "snmp")).execute().actionGet();
         index8=response8.getCount();
         
         //System.out.println("Context Size:"+response2.getCount());
    }
    
    //Calculate Flow Statistics
    public void function1()
    {
    	collection = db.getCollection("flowlevel-data");
    	
    	BasicDBObject whereQuery1     = new BasicDBObject("key", key).append("timestamp", new BasicDBObject("$gte",startDate).append("$lte", endDate)).append("srcBox", "Smartx-BPlus-ID");
    	BasicDBObject selectedFields1 = new BasicDBObject("key",1).append("srcHost", 1).append("destHost", 1).append("timestamp",1);
    	
    	GuiWindow.textArea.setText(GuiWindow.textArea.getText()+"\nFlow Query : "+whereQuery1.toString());
    	DBCursor curs = collection.find(whereQuery1, selectedFields1).sort(new BasicDBObject("timestamp",1)); 
    	packetsent = curs.size();
    	
    	DBObject curs1 = collection.findOne(whereQuery1, selectedFields1);
    	DBCursor curs2 = collection.find(whereQuery1, selectedFields1).sort(new BasicDBObject("timestamp",-1)).limit(1);
    	
    	if (packetsent==0)
    	{
    		System.out.println("No Flow Entries Exist");
    		
    	}
    	else
    	{
    		FlowStart = (Date) curs1.get("timestamp");
    		FlowStop  = (Date) curs2.iterator().next().get("timestamp");
    	}
    	   	
    	try {
        	
        	XContentBuilder builder = jsonBuilder()
        			.startObject()
        				.field("@version", "1")
        				.field("@timestamp", endDate)
        				.field("Source Host", srcIP)
        				.field("Destination Host", destIP)
        				.field("Source Port", srcPort)
        				//.field("Destination Port", destPort)
        				.field("Protocol", protocol)
        				.field("First Packet", FlowStart)
        				.field("Last Packet", FlowStop)
        				.field("Packets Sent", packetsent)
        				.field("pBox","Smartx-BPlus-ID")
        			.endObject();
        	//index1=index1+1;
            client.prepareIndex("multi-level-1","flow", "2").setSource(builder).execute();
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
    	BasicDBObject whereQuery2   = new BasicDBObject("data_address", "192.168.1.7")
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
    	BasicDBObject whereQuery4 = new BasicDBObject("box", "Smartx-BPlus-ID").append("timestamp", new BasicDBObject("$gte",startDate)
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
            	
                client.prepareIndex("multi-level-8","snmp", index8+"").setSource(builder).execute();
                index8=index8+1;
            }
            catch (IOException e) {
              e.printStackTrace();
            }
    		System.out.println("Index: "+ index8+" Timestamp: "+timestamp4+ " Description: "+description+" CPU Load: "+cpuload+" Ram Used (GB): "+ramused+" Ram Free (GB): "+ramfree+" Disk Used (GB): "+diskused+" Disk Free (GB): "+diskfree);
    	}
    }
    
    //Execute Relating Functions
    public void Correlate(String keyValue) {
    	function1();
    	function2();
    	function4();
    	
    	mongo.close();
    }
}
