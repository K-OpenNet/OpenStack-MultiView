package smartx.multiview.sdncontrollers;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

//import scala.util.parsing.json.JSONObject;

//import scala.util.parsing.json.JSONObject;
//import org.json.simple.JSONObject;

public class ControllerFlows {
	private Client client;
	//private long index1=0;
	private Date endDate;
	private boolean indexStatus;
	private String devcontroller, opscontroller;
	
	public ControllerFlows(String elasticsearch, String devcon, String opscon)
	{
		//endDate = end;
		devcontroller = devcon;
		opscontroller = opscon;
		
		//Elasticsearch Properties
        Settings settings = Settings.settingsBuilder()
        				    .put("cluster.name", "elasticsearch")
        				    .build();
        try {
			client = TransportClient.builder().build().addTransportAddress(new  InetSocketTransportAddress(InetAddress.getByName(elasticsearch), 9300));
        } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
        
        //Check Elasticsearch Index Already Exists or not
        indexStatus = client.admin().indices().prepareExists("multi-level-5").execute().actionGet().isExists();
        if (indexStatus)
        {
        	client.admin().indices().prepareDelete("multi-level-5").execute().actionGet();
        }
        //Create Elasticsearch Index
        CreateIndexResponse response = client.admin().indices().prepareCreate("multi-level-5").execute().actionGet();
        
        //Check Elasticsearch Index Already Exists or not
        indexStatus = client.admin().indices().prepareExists("multi-level-6").execute().actionGet().isExists();
        if (indexStatus)
        {
        	client.admin().indices().prepareDelete("multi-level-6").execute().actionGet();
        }
        //Create Elasticsearch Index
        response = client.admin().indices().prepareCreate("multi-level-6").execute().actionGet();
        
        //Check Elasticsearch Index Already Exists or not
        indexStatus = client.admin().indices().prepareExists("multi-level-7").execute().actionGet().isExists();
        if (indexStatus)
        {
        	client.admin().indices().prepareDelete("multi-level-7").execute().actionGet();
        }
        //Create Elasticsearch Index
        response = client.admin().indices().prepareCreate("multi-level-7").execute().actionGet();
        
        
        //CountResponse response1 = client.prepareCount("multi-level-5").setQuery(termQuery("_type", "flows")).execute().actionGet();
		//client.prepareDelete().setIndex("multi-level-5").execute().actionGet();
		
		//System.out.println(response.isAcknowledged());
		//index1=response1.getCount();
	}
    public void getFlowDetailsOps() {

    String user = "admin";
    String password = "admin";
    String baseURL = "http://"+opscontroller+":8080/controller/nb/v2/flowprogrammer";
    //String baseURL = "http://103.22.221.152:8080/controller/nb/v2/topology/default";
    String containerName = "default";
    int index1=0;

    try {

        // Create URL = base URL + container
        URL url = new URL(baseURL + "/" + containerName);

        // Create authentication string and encode it to Base64
        String authStr = user + ":" + password;
        String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());

        // Create Http connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set connection properties
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthStr);
        connection.setRequestProperty("Accept", "application/json");

        // Get the response from connection's inputStream
        InputStream content = (InputStream) connection.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(content));
        String line = "";
        
        //JSONParser jsonParser = null;
        line = in.readLine();
        System.out.println(line);
        JSONObject jsonObject = new JSONObject(line);
        JSONArray jsonArray = jsonObject.getJSONArray("flowConfig");
        for (int i=0 ; i<jsonArray.length(); i++)
        {
        	System.out.print("Node "+jsonArray.getJSONObject(i).get("node"));
        	System.out.print(" Name "+jsonArray.getJSONObject(i).get("name"));
        	System.out.print(" Install In Hw "+jsonArray.getJSONObject(i).get("installInHw"));
        	System.out.print(" Ingress Port "+jsonArray.getJSONObject(i).get("ingressPort"));
        	System.out.println(" Actions "+jsonArray.getJSONObject(i).get("actions"));
        	//System.out.println(jsonArray.getJSONObject(i).get("id"));
        	
        	try {
            	
            	XContentBuilder builder = jsonBuilder()
            			.startObject()
            				.field("@version", "1")
            				.field("@timestamp", endDate)
            				.field("Node", jsonArray.getJSONObject(i).get("node"))
            				.field("Name", jsonArray.getJSONObject(i).get("name"))
            				.field("Install In Hw", jsonArray.getJSONObject(i).get("installInHw"))
            				.field("Ingress Port", jsonArray.getJSONObject(i).get("ingressPort"))
            				.field("Actions", jsonArray.getJSONObject(i).get("actions"))
            			.endObject();
            	index1=index1+1;
                client.prepareIndex("multi-level-5","opsflows", index1+"").setSource(builder).execute();
            }
            catch (IOException e) {
              e.printStackTrace();
            }
        }
       // while ((line = in.readLine()) != null) {
       // System.out.println(line);
       // jsonObject = (JSONObject) jsonParser.parse(line);
        
       // }
        
        
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
    
    public void getFlowDetailsDev() {

        String user = "admin";
        String password = "admin";
        String baseURL = "http://"+devcontroller+":8080/controller/nb/v2/flowprogrammer";
        String containerName = "default";
        int index=0;

        try {

            // Create URL = base URL + container
            URL url = new URL(baseURL+"/default");

            // Create authentication string and encode it to Base64
            String authStr = user + ":" + password;
            String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());

            // Create Http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set connection properties
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuthStr);
            connection.setRequestProperty("Accept", "application/json");

            // Get the response from connection's inputStream
            InputStream content = (InputStream) connection.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line = "";
            line=in.readLine();
            System.out.println(line);
            JSONObject jsonObject = new JSONObject(line);
            JSONArray jsonArray = jsonObject.getJSONArray("flowConfig");
            for (int i=0 ; i<jsonArray.length(); i++)
            {
            	System.out.print("Node "+jsonArray.getJSONObject(i).get("node"));
            	System.out.print(" Name "+jsonArray.getJSONObject(i).get("name"));
            	System.out.print(" Install In Hw "+jsonArray.getJSONObject(i).get("installInHw"));
            	System.out.print(" Ingress Port "+jsonArray.getJSONObject(i).get("ingressPort"));
            	System.out.println(" Actions "+jsonArray.getJSONObject(i).get("actions"));
            	//System.out.println(jsonArray.getJSONObject(i).get("id"));
            	
            	try {
                	
                	XContentBuilder builder = jsonBuilder()
                			.startObject()
                				.field("@version", "1")
                				.field("@timestamp", endDate)
                				.field("Node", jsonArray.getJSONObject(i).get("node"))
                				.field("Name", jsonArray.getJSONObject(i).get("name"))
                				.field("Install In Hw", jsonArray.getJSONObject(i).get("installInHw"))
                				.field("Ingress Port", jsonArray.getJSONObject(i).get("ingressPort"))
                				.field("Actions", jsonArray.getJSONObject(i).get("actions"))
                			.endObject();
                	index=index+1;
                    client.prepareIndex("multi-level-7","devflows", index+"").setSource(builder).execute();
                }
                catch (IOException e) {
                  e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    
    public void getTopology() {

        String user = "admin";
        String password = "admin";
        String baseURL = "http://"+devcontroller+":8080/controller/nb/v2/topology";
        String containerName = "default";
        String ovs_port="", head="",tail="";
        int index2=0;

        try {

            // Create URL = base URL + container
            URL url = new URL(baseURL + "/" + containerName);

            // Create authentication string and encode it to Base64
            String authStr = user + ":" + password;
            String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());

            // Create Http connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set connection properties
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + encodedAuthStr);
            connection.setRequestProperty("Accept", "application/json");

            // Get the response from connection's inputStream
            InputStream content = (InputStream) connection.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line = "";
            
            line = in.readLine();
            System.out.println(line);
            JSONObject jsonObject = new JSONObject(line), jsonObject2, jsonObject3;
            
            JSONArray jsonArray = jsonObject.getJSONArray("edgeProperties");
            JSONArray jsonArray2;
            for (int i=0 ; i<jsonArray.length(); i++)
            {
            	//System.out.println(jsonArray.getJSONObject(i).get("properties"));
            	
            	jsonObject2=(JSONObject) jsonArray.getJSONObject(i).get("properties");
            	//System.out.println(jsonObject2.get("timeStamp"));
            	//jsonObject3=(JSONObject) jsonObject2.get("timeStamp");
            	//System.out.println(jsonObject3.get("name"));
                jsonObject3=(JSONObject) jsonObject2.get("name");
                ovs_port = (String) jsonObject3.get("value");
                //System.out.println(ovs_port);
            	
            	
                jsonObject2=(JSONObject) jsonArray.getJSONObject(i).get("edge");
                jsonObject3=(JSONObject) jsonObject2.get("headNodeConnector");
                jsonObject3=(JSONObject) jsonObject3.get("node");
                head = (String) jsonObject3.get("id");
                jsonObject3=(JSONObject) jsonObject2.get("tailNodeConnector");
                jsonObject3=(JSONObject) jsonObject3.get("node");
                tail = (String) jsonObject3.get("id");
                
            	System.out.print("OVS Port: "+ovs_port);
            	System.out.print(" Head Node: "+head);
            	System.out.println(" Tail Node: "+tail);
            	
            	//Store in Elasticsearch
            	try {
                	
                	XContentBuilder builder = jsonBuilder()
                			.startObject()
                				.field("@version", "1")
                				.field("@timestamp", endDate)
                				.field("OVS Port", ovs_port)
                				.field("Head Node", head)
                				.field("Tail Node", tail)
                			.endObject();
                	index2=index2+1;
                    client.prepareIndex("multi-level-6","topology", index2+"").setSource(builder).execute();
                }
                catch (IOException e) {
                  e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        	}
       	}

    public void getFlowStatsDev(){
   // 	http://103.22.221.150:8080/controller/nb/v2/statistics/default/flow
    }
}
