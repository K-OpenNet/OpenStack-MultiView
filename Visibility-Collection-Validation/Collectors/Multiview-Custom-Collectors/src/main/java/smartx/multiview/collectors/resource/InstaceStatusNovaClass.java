package smartx.multiview.collectors.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class InstaceStatusNovaClass implements Runnable{
	private OSClient client;
	private String token, key, line = "", neworkList, USER_ID, PASSWORD, PROJECT_ID, END_POINT;
	private String ThreadName="vBox Status Thread";
	private String vboxMongoCollection;
	private String vboxMongoCollectionRT;
	private URL url;
	private HttpURLConnection connection;
	private InputStream content;
	private BufferedReader in;
	private JSONParser jsonParser = new JSONParser();
	private JSONObject jsonObject = null, jsonKeyObject = null, jsonNetworkObject = null, jsonControlNetwork = null;
	private JSONArray jsonServersArray, jsonControlNetworkArray;
	private Hashtable<String, String> regions = new Hashtable<String, String>();
	private Enumeration e;
	private Set<String> networksKeySet;
	private Thread thread;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private Document documentHistory, documentRT;
	private DeleteResult deleteResult;
	private Date timestamp;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
	//@SuppressWarnings("deprecation")
	
	public InstaceStatusNovaClass(String dbHost, int dbPort, String dbName, String OpenStackUSER, String OpenStackPASSWD, String OpenStackPROJECT, String OpenStackENDPOINT, String vboxhistory, String vboxrt) 
	{
		mongoClient           = new MongoClient(dbHost, dbPort);
		db                    = mongoClient.getDatabase(dbName);
		vboxMongoCollection   = vboxhistory;
		vboxMongoCollectionRT = vboxrt;
		USER_ID               = OpenStackUSER;
		PASSWORD              = OpenStackPASSWD;
		PROJECT_ID            = OpenStackPROJECT;
		END_POINT             = OpenStackENDPOINT;
		/*this.client = OSFactory.builder().endpoint("http://103.22.221.51:5000/v2.0").credentials(USER_ID,PASSWORD).tenantName(PROJECT_ID).authenticate();*/
		
		//Setting Regions
		regions.put("GIST","103.22.221.170");
		regions.put("ID","167.205.51.36");
		regions.put("MYREN","103.26.47.228");
		regions.put("PH","202.90.150.4");
		regions.put("PKS","111.68.98.228");
		//regions.put("GJ-TEST","103.22.221.31");
		//regions.put("MY","203.80.21.4");
		//regions.put("TH","161.200.25.99");
	}

	public void getOSInstanceList() {
		//Delete Previous Documents from Real Time collection
    	deleteResult=db.getCollection(vboxMongoCollectionRT).deleteMany(new Document());
		//System.out.println("["+timestamp+"][INFO][Successfully Deleted Documents: "+deleteResult.getDeletedCount()+"]");
		e = regions.keys();
		while (e.hasMoreElements())
		{
			timestamp = new Date();
			this.client = OSFactory.builder()
	                	  .endpoint(END_POINT)
	                	  .credentials(USER_ID,PASSWORD)
	                	  .tenantName(PROJECT_ID)
	                	  .authenticate();
			//Get the box key
			key = (String) e.nextElement();
			//Get the Identity Token
			token = client.getToken().getId();
			//System.out.println("**************************************");
			System.out.println("["+dateFormat.format(timestamp)+"][INFO][NOVA][Region: "+regions.get(key)+" Token: "+token+"]");
			//System.out.println("Token : "+ token);
			//System.out.println("Region: "+ regions.get(key));
			//System.out.println("**************************************");
			//URL String to call OpentStack RestAPI
			String urlStr = "http://"+regions.get(key)+":8774/v2/ec8174bf08414e39b0b0f0ced69955d4/servers/detail?all_tenants=1";
			//String urlStr = "http://103.22.221.170:8774/v2/ec8174bf08414e39b0b0f0ced69955d4/servers/detail?all_tenants=1";
		    
			try 
		    {
		    	//Create URL from url STRING
				url= new URL(urlStr);
				
				// Create Http connection
		    	connection = (HttpURLConnection) url.openConnection();
		    	
		    	// Set connection properties
		        connection.setRequestMethod("GET");
		        connection.setRequestProperty("X-Auth-Token", token);
		        connection.setRequestProperty("Accept", "application/json");
		        
		        // Get the response from connection's inputStream
		        content = (InputStream) connection.getInputStream();
		        in = new BufferedReader(new InputStreamReader(content));
		        
		        while ((line = in.readLine()) != null)
		        	jsonObject = (JSONObject) jsonParser.parse(line);
		        
		        //Close Streams
		        in.close();
		        content.close();
		        
		        
		        //Process JSON Data
		        jsonServersArray = (JSONArray) jsonObject.get("servers");
		        for (int i=0; i<jsonServersArray.size(); i++)
		        {
		        	documentHistory = new Document();
		        	documentRT      = new Document();
		        	jsonKeyObject   = (JSONObject) jsonServersArray.get(i);
		        	
		        	documentHistory.put("timestamp", new Date());
		        	documentHistory.put("region",key);
		        	documentHistory.put("name",jsonKeyObject.get("name"));
		        	documentHistory.put("kvm_instance_name",jsonKeyObject.get("OS-EXT-SRV-ATTR:instance_name"));
		        	documentHistory.put("created",jsonKeyObject.get("created"));
		        	documentHistory.put("launched_at",jsonKeyObject.get("OS-SRV-USG:launched_at"));
		        	documentHistory.put("updated",jsonKeyObject.get("updated"));
		        	documentHistory.put("state",jsonKeyObject.get("OS-EXT-STS:vm_state"));
		        	documentHistory.put("status",jsonKeyObject.get("status"));
		        	documentHistory.put("host",jsonKeyObject.get("OS-EXT-SRV-ATTR:host"));
		        	documentHistory.put("key_name",jsonKeyObject.get("key_name"));
		        	documentHistory.put("tenant_id",jsonKeyObject.get("tenant_id"));
		        	documentHistory.put("user_id",jsonKeyObject.get("user_id"));
		        	
		        	documentRT.put("box"          , jsonKeyObject.get("OS-EXT-SRV-ATTR:host"));
		    		documentRT.put("name"         , jsonKeyObject.get("name"));
		    		documentRT.put("osusername"   , jsonKeyObject.get("user_id"));
		    		documentRT.put("ostenantname" , jsonKeyObject.get("tenant_id"));
		    		documentRT.put("vlanid"       , "");
	            	
	            	if (jsonKeyObject.get("OS-EXT-STS:vm_state").equals("active"))
	            	{
	            		documentRT.put("state", "Running");
	            	}
	            	else
	            	{
	            		documentRT.put("state", jsonKeyObject.get("OS-EXT-STS:vm_state"));
	            	}
		    		
		        	//Network Information Extraction
		        	jsonNetworkObject = (JSONObject) jsonKeyObject.get("addresses");
		        	networksKeySet    = jsonNetworkObject.keySet();
		        	
		        	for (String network : networksKeySet) 
		        	{
		        		jsonControlNetworkArray = (JSONArray) jsonNetworkObject.get(network);
		        		//Use jsonControlNetworkArray.size() if want to get floating address as well
		        		for (int j=0; j<1; j++)
		        		{
		        			jsonControlNetwork = (JSONObject) jsonControlNetworkArray.get(j);
		        			
			        		//System.out.println(jsonControlNetwork.get(network));
			        		
		        			neworkList=network+" Address:"+jsonControlNetwork.get("addr")+" MAC:"+jsonControlNetwork.get("OS-EXT-IPS-MAC:mac_addr")+" ";
			        		if (network.contains("control")==true)
			        		{
			        			//System.out.println("control");
			        			documentHistory.put("control_network",network);
			        			documentHistory.put("control_address",jsonControlNetwork.get("addr"));
			        			documentHistory.put("control_mac",jsonControlNetwork.get("OS-EXT-IPS-MAC:mac_addr"));
			        		}
			        		else
			        		{
			        			//System.out.println("data");
			        			documentHistory.put("data_network",network);
			        			documentHistory.put("data_address",jsonControlNetwork.get("addr"));
			        			documentHistory.put("data_mac",jsonControlNetwork.get("OS-EXT-IPS-MAC:mac_addr"));
			        		}
			        	}
		        	}
		        	
		        	//Insert New Documents to MongoDB
		    		db.getCollection(vboxMongoCollectionRT).insertOne(documentRT);
	            	db.getCollection(vboxMongoCollection).insertOne(documentHistory);
	            	System.out.println("["+dateFormat.format(timestamp)+"][INFO][NOVA][Box: "+jsonKeyObject.get("OS-EXT-SRV-ATTR:host")+" Instance: "+jsonKeyObject.get("name")+" State: "+jsonKeyObject.get("OS-EXT-STS:vm_state")+"]");
		    	}
		    } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void run() 
	{
		while (true)
		{
			getOSInstanceList();
			try {
				//Sleep For 5 Minutes
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void start() {
		System.out.println("Starting vBox Status Thread");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}

