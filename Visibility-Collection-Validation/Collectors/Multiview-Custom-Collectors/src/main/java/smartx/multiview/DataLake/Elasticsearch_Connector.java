/**
 * @author Muhammad Usman
 * @version 0.1
 */
 
package smartx.multiview.DataLake;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;







/*import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;*/
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class Elasticsearch_Connector {
	//private Client client;
	TransportClient client;
    private long index;
    
	public void setClient(String dbHost, int dbPort)
	{
		//Elasticsearch Properties
		
	    //Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
	   //# Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
	    try {
	    	
	    	client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new  InetSocketTransportAddress(InetAddress.getByName(dbHost), dbPort));
	    	System.out.println(client.connectedNodes());
	  //#  	client = new PreBuiltTransportClient(settings);
			//client = TransportClient.builder().build().addTransportAddress(new  InetSocketTransportAddress(InetAddress.getByName(dbHost), dbPort));
	    } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    }
	}
	
	public boolean checkIndexExist(String indexName)
	{
		//Check Elasticsearch Index Already Exists or not
	    boolean indexStatus = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
	    //if (indexStatus)
	    //{
	    //	client.admin().indices().prepareDelete(indexName).execute().actionGet();
	    //}
	    return indexStatus;
	}
	
    
    public void createIndex(String indexName)
    {
    	//Create Elasticsearch Index
    	if (!checkIndexExist(indexName))
    	{
    		CreateIndexResponse response = client.admin().indices().prepareCreate(indexName).execute().actionGet();
        }
    	
//    	SearchResponse response1 = client.prepareSearch(indexName).setQuery(termQuery("_type", "mirror")).get().get;// node.client.prepareCount(indexName).setQuery(termQuery("_type", "mirror")).execute().actionGet();
//    	index = response1.getAggregations().get("index").
    	//CountResponse response1 = client.prepareCount(indexName).setQuery(termQuery("_type", "mirror")).execute().actionGet();
        //index=response1.getCount();
    }
    
    public void insertData(String indexName, Date timestamp, String flowKey, String TLProtocol, String agentBox, float dataBytes, float frameSize)
    {
    	try {
    		XContentBuilder builder = jsonBuilder()
    			.startObject()
    				.field("@version", "1")
    				.field("@timestamp", timestamp)
    				.field("AgentID", agentBox)
    				.field("TransportProtocol", TLProtocol)
    				.field("flowKey", flowKey)
    				.field("Bytes", dataBytes)
    				.field("FrameSize", frameSize)
    			.endObject();
    		index=index+1;
    		client.prepareIndex(indexName, "mirror", index+"").setSource(builder).execute();
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
