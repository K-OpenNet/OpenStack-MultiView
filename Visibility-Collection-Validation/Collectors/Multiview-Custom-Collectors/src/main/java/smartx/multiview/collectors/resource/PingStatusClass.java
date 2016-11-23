package smartx.multiview.collectors.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import static java.util.Arrays.asList;

public class PingStatusClass implements Runnable {
	private String m_status;
	private String m_status_new;
	private String d_status;
	private String box = "";
	private String m_ip = "";
	private String d_ip = "";
	private Thread thread;
	private String ThreadName="pBox Status Thread";
	private String [] BoxType;
	private static Logger logger = Logger.getLogger(PingStatusClass.class.getName());
	private MongoClient mongoClient;
	private MongoDatabase db;
	private String pboxMongoCollection;
	private String pboxstatusMongoCollection;
	private FindIterable<Document> pBoxList;
    private FindIterable<Document> pBoxStatus;
    private List<String> bridges = new ArrayList<String>();
    private Date timestamp;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public PingStatusClass(String dbHost, int dbPort, String dbName, String pbox, String pboxstatus, String [] boxType) {
		mongoClient 		      = new MongoClient(dbHost, dbPort);
		db                        = mongoClient.getDatabase(dbName);
		BoxType                   = boxType;  
		pboxMongoCollection       = pbox;
		pboxstatusMongoCollection = pboxstatus;
	}
	
	public String SSHClient(String serverMgmtIp,String serverDataIp, String command, String usernameString,String password)
    {
		String InterfaceStatus = null;
        //System.out.println("Box : "+serverMgmtIp+" Command: "+command);
        try
        {
            Connection conn = new Connection(serverMgmtIp);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(usernameString, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");        
            ch.ethz.ssh2.Session sess = conn.openSession();
            sess.execCommand(command);  
            InputStream stdout = new StreamGobbler(sess.getStdout());
            InputStream stdout2;
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader br2;
            
            if (br.readLine()!=null)
            {
            	int index = 0;
            	InterfaceStatus="GREEN";
            	//System.out.println("Box : "+serverMgmtIp+" Data Interface Status: "+InterfaceStatus);
            	ch.ethz.ssh2.Session sess2 = conn.openSession();
            	sess2.execCommand("ovs-ofctl show brcap | grep ovs_vxlan | cut -f 1 -d : | cut -f 2 -d '(' | cut -f 1 -d ')'");
            	stdout = new StreamGobbler(sess2.getStdout());
            	br     = new BufferedReader(new InputStreamReader(stdout));
            	while(true)
            	{
            		String host = br.readLine();
            		if (host == null)
            		{
            			if (index==0)
            				InterfaceStatus="ORANGE";
            			index=1;
            			break;
            		}
                    
            		index++;
            		//System.out.print(host);
                    if (host!=null)
                    {
                    	ch.ethz.ssh2.Session sess3 = conn.openSession();
                    	sess3.execCommand("ping -c 1 `ovs-vsctl show | grep -A2 "+host+" | grep remote_ip | cut -d '\"' -f 2` | grep ttl");
                    	stdout2 = new StreamGobbler(sess3.getStdout());
                    	br2     = new BufferedReader(new InputStreamReader(stdout2));
                    	if (br2.readLine() == null)
                    	{
                    		//System.out.println(" If");
                    		InterfaceStatus="ORANGE";
                    		break;
                    	}
                    	else
                    	{
                    		//System.out.println(" Else");
                    		InterfaceStatus="GREEN";
                    	}
                    	
                    }
            	}
            	sess.close();
            	conn.close();
            }
            else
            {
            	InterfaceStatus="RED";
            	//System.out.println("Box : "+serverMgmtIp+" Data Interface Status: "+InterfaceStatus);
            	sess.close();
            	conn.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        
		return InterfaceStatus;
    }
	
	public void update_status() {
		timestamp = new Date();
		//pBoxList = db.getCollection(pboxMongoCollection).find(new Document("type", BoxType).append("type", "C**"));
		pBoxList = db.getCollection(pboxMongoCollection).find(new Document("$or", asList(new Document("type", BoxType[0]),new Document("type", BoxType[1]))));
		
		pBoxList.forEach(new Block<Document>() {
		    public void apply(final Document document) 
		    {
		        box       = (String) document.get("box");
		        m_ip      = (String) document.get("management_ip");
		        d_ip      = (String) document.get("data_ip");
		        m_status  = (String) document.get("management_ip_status");
		        
		        //Get Management Plane Status
		        pBoxStatus = db.getCollection(pboxstatusMongoCollection).find(new Document("destination", m_ip));
		        pBoxStatus.forEach(new Block<Document>() 
		        {
		            public void apply(final Document document2) 
		            {
		            	m_status_new = document2.get("status").toString().toUpperCase();
		            	
		            	//Get Data Plane Status
		            	if (m_status_new.equalsIgnoreCase("UP"))
		            	{
		            		m_status_new="GREEN";
		            		d_status=SSHClient(m_ip,d_ip,"netstat -ie | grep 'inet addr:"+d_ip+"' | cut -f 2 -d :", "root","fn!xo!ska!");
		            	}
		            	else
		            	{
		            		m_status_new="RED";
		            		d_status="RED";
		            	}
		            	
		            	UpdateResult result= db.getCollection(pboxMongoCollection).updateOne(new Document("management_ip", m_ip),
		            	        new Document("$set", new Document("management_ip_status", m_status_new).append("data_ip_status", d_status)));
		            	System.out.println("["+dateFormat.format(timestamp)+"][INFO][PING][Box: "+m_ip+" Management Status: "+m_status_new+" Data Status: "+d_status+" Records Updated :"+result.getModifiedCount()+"]");
		            }
		        });
		    }
		});
	}
	
	public void run() 
	{
		while (true)
		{
			update_status();
			try {
				//Sleep For 5 Minutes
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void start() 
	{
		System.out.println("Starting pBox Status Thread");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
		
	}
}
