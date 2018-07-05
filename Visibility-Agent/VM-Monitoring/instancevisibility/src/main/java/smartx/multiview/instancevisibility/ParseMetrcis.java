/**
 * @author Muhammad Usman
 * @version 0.1
*/

package smartx.multiview.instancevisibility;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHits;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ParseMetrcis 
{
	static TransportClient client;
    private static long index;
    private MongoClient mongoClient;
	private MongoDatabase db;
	private static final String FILENAME1 = "instancemetrics.log";
	private static final String FILENAME2 = "memmetrics.log";
	private static final String FILENAME3 = "dfmetrics.log";
	
	@SuppressWarnings("unchecked")
	public void setClient(String dbHost, int dbPort)
	{
	    try {
	    	
	    	client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new  TransportAddress(InetAddress.getByName(dbHost), dbPort));
	    } catch (UnknownHostException e) {
			e.printStackTrace();
	    }
	}
	
	public boolean checkIndexExist(String indexName)
	{
	    boolean indexStatus = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
	    return indexStatus;
	}
    
    public void createIndex(String indexName)
    {
    	if (!checkIndexExist(indexName))
    	{
    		System.out.println("Index Does not Exist");
    		CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).execute().actionGet();
        }
    	
    	SearchHits resp = client.prepareSearch(indexName).get().getHits();
    	index = resp.getTotalHits();
    }
	    
    //Create Database Connection
	public void setDbConnection(String dbHost, int dbPort, String dbName) {
		mongoClient  = new MongoClient(dbHost , dbPort);
		this.db      = mongoClient.getDatabase(dbName);
	}
	
	//Get Database Connection Object
	public MongoDatabase getDbConnection() {
		return db;
	}
	
	public static void main( String[] args )
    {
        ParseMetrcis pm = new ParseMetrcis();
        
        pm.setDbConnection("103.22.221.56", 27017, "multiviewdb");
        pm.setClient("103.22.221.56", 9300);
        
        // This will reference one line at a time
        String line = null;
        
        while(true)
        {
        	try {
                // FileReader reads text files in the default encoding.
                FileReader fileReader1 = new FileReader(FILENAME1);
                FileReader fileReader2 = new FileReader(FILENAME2);
                FileReader fileReader3 = new FileReader(FILENAME3);
                
                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader = new BufferedReader(fileReader1);
                Document NewDocument = new Document();
                
                while((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                    String[] metrics = line.split(",");
                    String [] IPs = metrics[4].split(" ");
                    
                    NewDocument.put("timestamp",   new Date());
            		NewDocument.put("vmname",      metrics[0]);
            		NewDocument.put("os",          metrics[1]);
            		NewDocument.put("kernel",      metrics[3]);
            		NewDocument.put("controlip",   IPs[0]);
            		NewDocument.put("dataip",      IPs[1]);
            		if (metrics[5].equals("<!DOCTYPE html>")) {
            			NewDocument.put("cpuload1",    0);
                		NewDocument.put("cpuload5",    0);
                		NewDocument.put("cpuload15",   0);
            		}
            		else {
            			NewDocument.put("cpuload1",    metrics[6]);
                		NewDocument.put("cpuload5",    metrics[7]);
                		NewDocument.put("cpuload15",   metrics[8]);
            		}
            	}   
                
                int count = 1;
                bufferedReader = new BufferedReader(fileReader2);
                while((line = bufferedReader.readLine()) != null) {
                    if (count == 1) {
                    	String[] metrics = line.split(" ");
                    	NewDocument.put("total_memory",     metrics[0]);
                    	NewDocument.put("used_memory",      metrics[1]);
                    	NewDocument.put("free_memory",      metrics[2]);
                    	NewDocument.put("shared_memory",    metrics[3]);
                    	NewDocument.put("buff_memory",      metrics[4]);
                    	NewDocument.put("available_memory", metrics[5]);
                    	System.out.println(line);
                    }
                    count++;
                }  
                
                count = 1;
                bufferedReader = new BufferedReader(fileReader3);
                while((line = bufferedReader.readLine()) != null) {
                	if (count == 1) {
                    	String[] metrics = line.split(" ");
                    	NewDocument.put("total_disk",     metrics[0]);
                    	NewDocument.put("used_disk",      metrics[1]);
                    	NewDocument.put("free_disk",      metrics[2]);
                    	NewDocument.put("use_perc_disk",  metrics[3]);
                    	System.out.println(line);
                    }
                    count++;
                }  
                
                pm.getDbConnection().getCollection("resource_instance_metrics").insertOne(NewDocument);
                pm.createIndex("resource_instance_metrics");
                try {
            		XContentBuilder builder = jsonBuilder()
            			.startObject()
            				.field("@version", "1")
            				.field("@timestamp", new Date())
            				.field("vmname", NewDocument.get("vmname"))
            				.field("os", NewDocument.get("os"))
            				.field("kernel", NewDocument.get("kernel"))
            				.field("controlip", NewDocument.get("controlip"))
            				.field("dataip", NewDocument.get("dataip"))
            				.field("cpuload1", NewDocument.get("cpuload1"))
            				.field("cpuload5", NewDocument.get("cpuload5"))
            				.field("cpuload15", NewDocument.get("cpuload15"))
            				.field("total_memory", NewDocument.get("total_memory"))
            				.field("used_memory", NewDocument.get("used_memory"))
            				.field("free_memory", NewDocument.get("free_memory"))
            				.field("shared_memory", NewDocument.get("shared_memory"))
            				.field("buff_memory", NewDocument.get("buff_memory"))
            				.field("available_memory", NewDocument.get("available_memory"))
            				.field("total_disk", NewDocument.get("total_disk"))
            				.field("used_disk", NewDocument.get("used_disk"))
            				.field("free_disk", NewDocument.get("free_disk"))
            				.field("use_perc_disk", NewDocument.get("use_perc_disk"))
            			.endObject();
            		index=index+1;
            		client.prepareIndex("resource_instance_metrics", "instance", index+"").setSource(builder).execute();
            	}
            	catch (IOException e) {
            		e.printStackTrace();
            	}
                
                bufferedReader.close();         
            }
            catch(FileNotFoundException ex) {
                ex.printStackTrace();
            }
            catch(IOException ex) {
            	ex.printStackTrace();
            }
        	try {
        		System.out.println("Record inserted...");
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
