package com.gist.netcs.multiview;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class ovsBridgeStatusClass implements Runnable{
	private String box = "";
	private String m_ip = "";
	private Boolean Mathced=false;
	private Thread thread;
	private String ThreadName="pBox Status Thread";
	private static Logger logger = Logger.getLogger(PingStatusClass.class.getName());
	
	private MongoClient mongoClient;
	private static MongoDatabase db;
	
	private String pboxMongoCollection;
	private String ovsListMongoCollection;
	private String ovsstatusMongoCollection;
	private String [] BoxType;
	private String OVS_STATUS;
	
	private List<String> bridges = new ArrayList<String>();
	
    private FindIterable<Document> pBoxList;
    private FindIterable<Document> ovsList;
    
    public ovsBridgeStatusClass(String dbHost, int dbPort, String dbName, String pbox, String ovslist, String ovsstatus, String [] boxType) {
		mongoClient 			 = new MongoClient(dbHost, dbPort);
		db                       = mongoClient.getDatabase(dbName);
		pboxMongoCollection      = pbox;
		ovsListMongoCollection   = ovslist;
		ovsstatusMongoCollection = ovsstatus;
		BoxType                  = boxType;
	}
    
    public void CheckOVSProcess(String serverIp,String command, String usernameString,String password)
    {
    	try
        {
            Connection conn = new Connection(serverIp);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(usernameString, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");        
            ch.ethz.ssh2.Session sess = conn.openSession();
            sess.execCommand(command);  
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            
            String line = br.readLine();
            System.out.println(command+"  - Result : "+line);
            
        	if (Integer.parseInt(line)>0)
        	{
        		OVS_STATUS="GREEN";
            }
            else
            {
            	OVS_STATUS="ORANGE";
            	
            }
             
            //System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close();
            conn.close();
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);

        }
    }
    
    public void CheckNeutron(String serverIp,String command, String usernameString,String password)
    {
        try
        {
            Connection conn = new Connection(serverIp);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(usernameString, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");        
            ch.ethz.ssh2.Session sess = conn.openSession();
            sess.execCommand(command);  
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            
        	String line = br.readLine();
        	System.out.println(command+"  - Result : "+line);
            
        	if (Integer.parseInt(line) > 2)
            {
              	OVS_STATUS="GREEN";
            }
            else
            {
              	OVS_STATUS="DARKGRAY";
            }
            //System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close();
            conn.close();
            
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
    }
    
    public void SSHClient(String serverIp,String command, String usernameString,String password)
    {
        System.out.println(serverIp+" Server Bridges:");
        try
        {
            Connection conn = new Connection(serverIp);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(usernameString, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");        
            ch.ethz.ssh2.Session sess = conn.openSession();
            sess.execCommand(command);  
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while (true)
            {
            	String line = br.readLine();
                System.out.println(line);
                if (line == null)
                    break;
                
                if (line!=null)
                {
                	bridges.add(line);
                	//System.out.println(m_ip+" BRIDGES-STATUS "+OVS_STATUS);
                }
            }
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close();
            conn.close();
            
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);

        }
    }
    
	public void update_status() 
	{
		//pBoxList = db.getCollection(pboxMongoCollection).find(new Document("type", BoxType));
		pBoxList = db.getCollection(pboxMongoCollection).find(new Document("$or", asList(new Document("type", BoxType[0]),new Document("type", BoxType[1]))));
		pBoxList.forEach(new Block<Document>() {
		    public void apply(final Document document) {
		    	
		        box  = (String) document.get("box");
		        m_ip = (String) document.get("management_ip");
		        
		        //Check Status of OVS Process in each Box/OVS-VM
		        CheckOVSProcess(m_ip,"service openvswitch-switch status | grep -c running", "root","fn!xo!ska!");
		        if (OVS_STATUS.equals("ORANGE"))
		        {
		        	UpdateResult result= db.getCollection(ovsstatusMongoCollection).updateMany(new Document("box", box),
			           	        new Document("$set", new Document("status", OVS_STATUS)));
		        	System.out.println("Box : "+box+" Status : "+OVS_STATUS+" Records Updated : "+result.getModifiedCount());
			    }
			    
			    else
		        {
			    	//Check Status of OpenStack Neutron Service
			        CheckNeutron(m_ip,"ps aux | grep -c neutron-openvswitch-agent", "root","fn!xo!ska!");
			        if (OVS_STATUS.equals("DARKGRAY"))
			        {
			        	UpdateResult result= db.getCollection(ovsstatusMongoCollection).updateMany(new Document("box", box),
				           	        new Document("$set", new Document("status", OVS_STATUS)));
			        	System.out.println("Box : "+box+" Status : "+OVS_STATUS+" Records Updated : "+result.getModifiedCount());
				    }
				    
				    else
			        {   
				    	//Check OVS bridge configurations for existance of bridge
				    	SSHClient(m_ip,"ovs-vsctl list-br", "root","fn!xo!ska!");
				        
				        ovsList = db.getCollection(ovsListMongoCollection).find(new Document("type", "B**"));
		        		ovsList.forEach(new Block<Document>() 
				        {
				            public void apply(final Document ovsDocument) 
				            {
				            	System.out.println(ovsDocument.get("bridge"));
				            	for (int i=0; i<bridges.size(); i++)
				            	{
				            		if (bridges.get(i).equals(ovsDocument.get("bridge")))
				            		{
				            			UpdateResult result= db.getCollection(ovsstatusMongoCollection).updateOne(new Document("box", box).append("bridge", bridges.get(i)),
				                		        new Document("$set", new Document("status", OVS_STATUS)));
				                		System.out.println("Records Updated :"+result.getModifiedCount());
				            			Mathced=true;
				            			System.out.println(Mathced);
				                		break;
				            		}
				            	}
				            	
				            	//When bridge is not Found in Box but exist in Model
				            	if (Mathced.equals(false))
				            	{
				            		System.out.println("False "+ ovsDocument.get("bridge")+ " m_ip: "+m_ip+" status: "+OVS_STATUS);
				            		UpdateResult result= db.getCollection(ovsstatusMongoCollection).updateOne(new Document("box", box).append("bridge", ovsDocument.get("bridge")),
				    		        		        new Document("$set", new Document("status", "RED")));
				    		        System.out.println("Records Updated :"+result.getModifiedCount());
				            		Mathced=false;
				            		//System.out.println(Mathced);
				            	}
				            	Mathced=false;
				            }
				        });
			        }
		        }
        		bridges=new ArrayList<String>();
        		OVS_STATUS="";
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
	public void start() {
		System.out.println("Starting ovsBridge Status Thread");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
		
	}
}
