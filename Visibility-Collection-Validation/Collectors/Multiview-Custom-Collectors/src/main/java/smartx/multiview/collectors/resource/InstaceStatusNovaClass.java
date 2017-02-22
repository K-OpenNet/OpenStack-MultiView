/**
 * @author Muhammad Usman
 * @version 0.1
 */

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class InstaceStatusNovaClass implements Runnable{
	private Thread thread;
	private OSClient client;
	private String ThreadName="vBox Status Thread";
	private String token, key, line = "", neworkList, USER_ID, PASSWORD, PROJECT_ID, END_POINT;
	private String vboxMongoCollection, vboxMongoCollectionRT;
	private String CTRL_Box_IP, CTRL_Box_USER, CTRL_Box_PASSWORD;
	private URL url;
	private HttpURLConnection connection;
	private InputStream content;
	private BufferedReader in;
	private JSONParser jsonParser = new JSONParser();
	private JSONObject jsonObject = null, jsonKeyObject = null, jsonNetworkObject = null, jsonControlNetwork = null;
	private JSONArray jsonServersArray, jsonControlNetworkArray;
	private Hashtable<String, String> regions = new Hashtable<String, String>();
	private Hashtable<String, String> typecBoxes = new Hashtable<String, String>();
	private List<Document> documentsRT = new ArrayList<Document>();
	private Enumeration e, boxKeys;
	private Set<String> networksKeySet;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private Document documentHistory, documentRT;
	private DeleteResult deleteResult;
	private Date timestamp;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Logger LOG = Logger.getLogger("novaUpdateFile");
    
	//@SuppressWarnings("deprecation")
	
	public InstaceStatusNovaClass(String CTRL_IP, String CTRL_USER, String CTRL_PASSWORD, String dbHost, int dbPort, String dbName, String OpenStackUSER, String OpenStackPASSWD, String OpenStackPROJECT, String OpenStackENDPOINT, String vboxhistory, String vboxrt) 
	{
		mongoClient           = new MongoClient(dbHost, dbPort);
		db                    = mongoClient.getDatabase(dbName);
		vboxMongoCollection   = vboxhistory;
		vboxMongoCollectionRT = vboxrt;
		CTRL_Box_IP           = CTRL_IP;
		CTRL_Box_USER         = CTRL_USER;
		CTRL_Box_PASSWORD     = CTRL_PASSWORD;
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

	public void getOSInstanceListTypeB() {
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
			//System.out.println("["+dateFormat.format(timestamp)+"][INFO][NOVA][Region: "+regions.get(key)+" Token: "+token+"]");
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
		        	documentHistory.put("ostenantid",jsonKeyObject.get("tenant_id"));
		        	documentHistory.put("osuserid",jsonKeyObject.get("user_id"));
		        	
		        	documentRT.put("name"       , jsonKeyObject.get("name"));
		        	documentRT.put("uuid"       , "");
		        	documentRT.put("vlanid"     , "");
		        	documentRT.put("ostenantid" , jsonKeyObject.get("tenant_id"));
		    		documentRT.put("osuserid"   , jsonKeyObject.get("user_id"));
		    		documentRT.put("box"        , jsonKeyObject.get("OS-EXT-SRV-ATTR:host"));
		    		
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
		        	
		        	if (jsonKeyObject.get("OS-EXT-STS:vm_state").equals("active"))
	            	{
	            		documentRT.put("state", "Running");
	            		//Update Document to MongoDB
    		    	}
	            	else
	            	{
	            		documentRT.put("state", jsonKeyObject.get("OS-EXT-STS:vm_state"));
	            	}
		        	
		        	//Insert New Documents to MongoDB
		    		//db.getCollection(vboxMongoCollectionRT).insertOne(documentRT);
		    		//db.getCollection(vboxMongoCollectionRT).insertOne(documentRT);
		    		db.getCollection(vboxMongoCollection).insertOne(documentHistory);
	            	LOG.debug("["+dateFormat.format(timestamp)+"][INFO][NOVA][Box: "+jsonKeyObject.get("OS-EXT-SRV-ATTR:host")+" Instance: "+jsonKeyObject.get("name")+" State: "+jsonKeyObject.get("OS-EXT-STS:vm_state")+"]");
	            	//System.out.println("["+dateFormat.format(timestamp)+"][INFO][NOVA][Box: "+jsonKeyObject.get("OS-EXT-SRV-ATTR:host")+" Instance: "+jsonKeyObject.get("name")+" State: "+jsonKeyObject.get("OS-EXT-STS:vm_state")+"]");
	            	documentsRT.add(documentRT);
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
	
	public void getOSInstanceListTypeC()
    {
    	String instanceName, instanceID, instanceStatus, instancePower, instanceNetwork, instancetenantID, BoxName;
    	
    	typecBoxes.put("Type-C-KU", "cat /opt/InstanceList/Box1VMs.list");
    	typecBoxes.put("Type-C-KN", "cat /opt/InstanceList/Box2VMs.list");
    	typecBoxes.put("Type-C-JJ", "cat /opt/InstanceList/Box3VMs.list");
    	typecBoxes.put("Type-C-GJ", "cat /opt/InstanceList/Box4VMs.list");
    	boxKeys = typecBoxes.keys();
    	//sshClient("Type-C-KU", "cat /opt/Box1VMs.list");
    	//sshClient("Type-C-KN", "cat /opt/Box2VMs.list");
    	//sshClient("Type-C-JJ", "cat /opt/Box3VMs.list");
    	//sshClient("Type-C-GJ", "cat /opt/Box4VMs.list");
    	timestamp = new Date();
    	while (boxKeys.hasMoreElements())
    	{
    		BoxName = (String) boxKeys.nextElement();
	    	try
	        {
	        	Connection conn = new Connection(CTRL_Box_IP);
	            conn.connect();
	            boolean isAuthenticated = conn.authenticateWithPassword(CTRL_Box_USER, CTRL_Box_PASSWORD);
	            if (isAuthenticated == false)
	                throw new IOException("Authentication failed.");        
	            ch.ethz.ssh2.Session sess = conn.openSession();
	            sess.execCommand(typecBoxes.get(BoxName)); 
	            
	            InputStream stdout = new StreamGobbler(sess.getStdout());
	            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
	            
	            while (true)
	            {
	            	String line = br.readLine();
	            	if (line == null)
	                    break;
	                if (line!=null)
	                {
	                	if (!(line.contains("+-") || line.contains("Task State")))
	                	{
	                		instanceID       = line.substring(StringUtils.ordinalIndexOf(line, "|", 1)+1, StringUtils.ordinalIndexOf(line, "|", 2)-1).trim();
	                		instanceName     = line.substring(StringUtils.ordinalIndexOf(line, "|", 2)+1, StringUtils.ordinalIndexOf(line, "|", 3)-1).trim();
	                		instancetenantID = line.substring(StringUtils.ordinalIndexOf(line, "|", 3)+1, StringUtils.ordinalIndexOf(line, "|", 4)-1).trim();
	                		instanceStatus   = line.substring(StringUtils.ordinalIndexOf(line, "|", 4)+1, StringUtils.ordinalIndexOf(line, "|", 5)-1).trim();
	                		instancePower    = line.substring(StringUtils.ordinalIndexOf(line, "|", 6)+1, StringUtils.ordinalIndexOf(line, "|", 7)-1).trim();
	                		instanceNetwork  = line.substring(StringUtils.ordinalIndexOf(line, "|", 7)+1, StringUtils.ordinalIndexOf(line, "|", 8)-1).trim();
	                		
	                		documentHistory = new Document();
	    		        	documentRT      = new Document();
	    		        	
	    		        	documentHistory.put("timestamp"  , new Date());
	    		        	documentHistory.put("box"        , BoxName);
	    		        	documentHistory.put("ostenantid"   , instancetenantID);
	    		        	documentHistory.put("name"       , instanceName);
	    		        	documentHistory.put("uuid"       , instanceID);
	    		        	documentHistory.put("Powerstate" , instancePower);
	    		        	documentHistory.put("Network"    , instanceNetwork);
	    		    		
	    		        	documentRT.put("name"            , instanceName);
	    		        	documentRT.put("uuid"            , instanceID);
	    		        	documentRT.put("vlanid"          , "");
	    		        	documentRT.put("ostenantid"      , instancetenantID);
	    		        	documentRT.put("osuserid"        , instancetenantID);
	    		        	documentRT.put("box"             , BoxName);
	    		    		
	    		        	//	UpdateResult result;
	    		    		if (instanceStatus.equals("ACTIVE"))
	    	            	{
	    		    			documentHistory.put("state", "Running");
	    	            		documentRT.put("state", "Running");
	    	            		
	    	            	}
	    	            	else
	    	            	{
	    	            		documentHistory.put("state", instanceStatus);
	    	            		documentRT.put("state", instanceStatus);
	    	            	}
	    		    		
	    		    		db.getCollection(vboxMongoCollection).insertOne(documentHistory);
	    		    		
	    	            	LOG.debug("["+dateFormat.format(timestamp)+"][INFO][NOVA][Box: "+BoxName+" Instance: "+instanceName+" State: "+instanceStatus+"]");
	    	            	documentsRT.add(documentRT);
	                	}
	                }
	            }
	            
	            //System.out.println("ExitCode: " + sess.getExitStatus());
	            sess.close();
	            conn.close();
	        }
	        catch (IOException e)
	        {
	        	LOG.debug("[INFO][OVS-VM][Box : "+CTRL_Box_IP+" Failed]");
	        	System.out.println("[INFO][OVS-VM][Box : "+CTRL_Box_IP+" Failed]");
	            e.printStackTrace(System.err);
	        }
    	}
    }
	
	public void run() 
	{
		while (true)
		{
			getOSInstanceListTypeC();
			getOSInstanceListTypeB();
			
			//Delete Previous Documents from Real Time collection
	    	deleteResult = db.getCollection(vboxMongoCollectionRT).deleteMany(new Document());
	    	
	    	//Insert New Documents for Near-Realtime Visualization
			db.getCollection(vboxMongoCollectionRT).insertMany(documentsRT);
			documentsRT.clear();
			
			try {
				//Sleep For 30 Seconds
				Thread.sleep(30000);
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

