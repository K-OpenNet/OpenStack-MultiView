package com.gist.netcs.multiview;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class InstanceStatusClass implements Runnable{
	private String box = "";
	private Thread thread;
	private String ThreadName="vBox Status Thread";
	private Date date;
	private static Logger logger = Logger.getLogger(PingStatusClass.class.getName());
	
	private MongoClient mongoClient;
	private static MongoDatabase db;
	private Document NewDocument;
	private DeleteResult deleteResult;
	
	private String pboxMongoCollection;
	private String vboxMongoCollection;
	private String vboxosMongoCollection;
    
    private FindIterable<Document> pBoxList;
    private FindIterable<Document> pBoxStatus;
    
    public InstanceStatusClass(String dbHost, int dbPort, String dbName, String pbox, String vbox, String vboxos) {
		mongoClient = new MongoClient(dbHost, dbPort);
		db          = mongoClient.getDatabase(dbName);
		pboxMongoCollection   = pbox;
		vboxMongoCollection   = vbox;
		vboxosMongoCollection = vboxos;
	}
	public void update_status() {
		date = new Date();
		pBoxList = db.getCollection(pboxMongoCollection).find();
		deleteResult=db.getCollection(vboxMongoCollection).deleteMany(new Document());
		System.out.println("Successfully Deleted Documents: "+deleteResult.getDeletedCount());
		
		pBoxList.forEach(new Block<Document>() {
		    public void apply(final Document document) {
		        
		        box       = (String) document.get("box");
		        pBoxStatus = db.getCollection(vboxosMongoCollection).find(new Document("host", box).append("timestamp", new Document("$gt",DateUtils.addMinutes(date, -29))));
		        pBoxStatus.forEach(new Block<Document>() 
		        {
		            public void apply(final Document InstanceDocument) 
		            {
		            	NewDocument = new Document();
		            	NewDocument.put("box", box);
		            	NewDocument.put("name", InstanceDocument.get("name"));
		            	NewDocument.put("osusername", InstanceDocument.get("user_id"));
		            	NewDocument.put("ostenantname", InstanceDocument.get("tenant_id"));
		            	NewDocument.put("vlanid", InstanceDocument.get(""));
		            	
		            	if (InstanceDocument.get("state").equals("active"))
		            	{
		            		NewDocument.put("state", "Running");
		            	}
		            	else
		            	{
		            		NewDocument.put("state", InstanceDocument.get("state"));
		            	}
		            	
		            	db.getCollection(vboxMongoCollection).insertOne(NewDocument);
		            	System.out.println(box+" : "+InstanceDocument.get("name")+" : "+InstanceDocument.get("state"));
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
	public void start() {
		System.out.println("Starting vBox Status Thread");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}
