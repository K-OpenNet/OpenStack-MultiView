package smartx.multiview.collectors.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class PingStatusCollectClass implements Runnable
{
	private Thread thread;
	private String ThreadName="Physical Path Thread";
	private String VisibilityCenter, BoxIP, pingResult = "";
	private String pboxMongoCollection, pboxstatusMongoCollection, pboxstatusMongoCollectionRT;
	private Date timestamp;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private MongoClient mongoClient;
	private MongoDatabase db;
	private Document NewDocument;
	private BasicDBObject document;
	private DeleteResult deleteResult;
	private FindIterable<Document> pBoxList;
	
	public PingStatusCollectClass(String visibilityCenter, String dbHost, int dbPort, String dbName, String pbox, String pboxstatus, String pboxstatusRT, String [] boxType) 
	{
		VisibilityCenter            = visibilityCenter;
		mongoClient 		        = new MongoClient(dbHost, dbPort);
		db                          = mongoClient.getDatabase(dbName);
		pboxMongoCollection         = pbox;
		pboxstatusMongoCollection   = pboxstatus;
		pboxstatusMongoCollectionRT = pboxstatusRT;
	}
	
	public void writeDB(String json1, Date timestamp, String src, String dest, String stat)
	{
		NewDocument = new Document();
		NewDocument.put("timestamp", new Date());
		NewDocument.put("source", src);
		NewDocument.put("destination", dest);
		NewDocument.put("status", stat);
		
		db.getCollection(pboxstatusMongoCollection).insertOne(NewDocument);
		db.getCollection(pboxstatusMongoCollectionRT).insertOne(NewDocument);
		
		//document.clear();
	}
	
	public void run() 
	{
		while (true)
		{
			//Remove Previous records from Collection (For good performance)
			deleteResult = db.getCollection(pboxstatusMongoCollectionRT).deleteMany(new Document());
			//System.out.println("Successfully Deleted Documents: "+deleteResult.getDeletedCount());
			
			//Get List of SmartX Boxes
			pBoxList = db.getCollection(pboxMongoCollection).find();
			
			pBoxList.forEach(new Block<Document>() {
			    public void apply(final Document document) {
			    	pingResult = "";
			    	BoxIP = (String) document.get("management_ip");
			    	
			    	String pingCmd = "nmap -sP -R " + BoxIP;
		            try 
		            {
						Runtime r = Runtime.getRuntime();
						Process p = r.exec(pingCmd);
		
						BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String inputLine;
						timestamp = new Date();
						while ((inputLine = in.readLine()) != null) {
							pingResult += inputLine;
						}
						
						if (pingResult.contains("Host seems down")==true)
						{
							writeDB("", new Date(),VisibilityCenter,BoxIP,"Down");
							System.out.println("["+dateFormat.format(timestamp)+"][INFO][PING][Box: "+BoxIP+" Management Status: Down]");
						}
						else
						{
							writeDB("", new Date(),VisibilityCenter,BoxIP,"Up");
							System.out.println("["+dateFormat.format(timestamp)+"][INFO][PING][Box: "+BoxIP+" Management Status: Up]");
						}
						in.close();
		            } catch (IOException e) {
		            System.out.println(e);
		            }
			    }
			});
			
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
		System.out.println("Starting Nmap Ping Test");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}


