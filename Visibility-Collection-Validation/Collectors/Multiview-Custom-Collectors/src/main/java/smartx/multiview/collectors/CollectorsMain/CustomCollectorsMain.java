package smartx.multiview.collectors.CollectorsMain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import smartx.multiview.collectors.flow.*;
import smartx.multiview.collectors.resource.*;

public class CustomCollectorsMain 
{
	private static String VISIBILITY_CENTER;
	private static String MONGO_DB_HOST;
	private static int    MONGO_DB_PORT;
	private static String MONGO_DB_DATABASE;
	private static String OPENSTACK_PASSWORD;
	private static String OPENSTACK_USER_ID;
	private static String OPENSTACK_PROJECT_ID;
	private static String OPENSTACK_ENDPOINT;
	private static String devopscontrollers;
	private static String ControllerPassword;
	private static String ControllerUser;
	private static String SmartXBox_USER;
	private static String SmartXBox_PASSWORD;
	private static String OVS_VM_USER;
	private static String OVS_VM_PASSWORD;
	private static String pboxMongoCollection                  = "configuration-pbox-list";
	private static String vboxMongoCollection                  = "resourcelevel-os-instance-detail";
	private static String vboxMongoCollectionRT                = "configuration-vbox-list";
	private static String pboxstatusMongoCollection            = "resourcelevel-ppath";
	private static String pboxstatusMongoCollectionRT          = "resourcelevel-ppath-rt";
	private static String ovsListMongoCollection               = "configuration-vswitch-list";
	private static String ovsstatusMongoCollection             = "configuration-vswitch-status";
	private static String flowConfigMongoCollection            = "flow-configuration-sdn-controller";
	private static String flowConfigMongoCollectionRT          = "flow-configuration-sdn-controller-rt";
	private static String flowStatsMongoCollection             = "flow-stats-sdn-controller";
	private static String flowStatsMongoCollectionRT           = "flow-stats-sdn-controller-rt";
	private static String flowConfigOpenStackMongoCollection   = "flow-stats-openstack-bridges";
	private static String flowConfigOpenStackMongoCollectionRT = "flow-stats-openstack-bridges-rt";
	private static String [] BoxType = {"B**", "C**"};
				
    public static void main( String[] args )
    {
    	Properties prop = new Properties();
    	InputStream input = null;
    	try {

    		input = new FileInputStream("../MultiView-Configurations/Custom_Collectors.properties");

    		// load a properties file
    		prop.load(input);
    		
    		//Visibility Center IP
    		VISIBILITY_CENTER    = prop.getProperty("VISIBILITY_CENTER");
    		
    		//MongoDB Properties
    		MONGO_DB_HOST        = prop.getProperty("MONGODB_HOST");
    		MONGO_DB_PORT        = Integer.parseInt(prop.getProperty("MONGODB_PORT"));
    		MONGO_DB_DATABASE    = prop.getProperty("MONGODB_DATABASE");
    		
    		//OpenStack Properties
    		OPENSTACK_USER_ID    = prop.getProperty("OPENSTACK_USER_ID");
    		OPENSTACK_PASSWORD   = prop.getProperty("OPENSTACK_PASSWORD");
    		OPENSTACK_PROJECT_ID = prop.getProperty("OPENSTACK_PROJECT_ID");
    		OPENSTACK_ENDPOINT   = prop.getProperty("OPENSTACK_ENDPOINT");
    		
    		//OpenDayLight Properties
    		devopscontrollers    = prop.getProperty("devopscontrollers");
    		ControllerUser       = prop.getProperty("CONTROLLER_USER");
    		ControllerPassword   = prop.getProperty("CONTROLLER_PASSWORD");
    		
    		//SmartX Box Properties
    		SmartXBox_USER       = prop.getProperty("SmartXBox_USER");
    		SmartXBox_PASSWORD   = prop.getProperty("SmartXBox_PASSWORD");
    		
    		//OVS-VM Properties
    		OVS_VM_USER       = prop.getProperty("OVS_VM_USER");
    		OVS_VM_PASSWORD   = prop.getProperty("OVS_VM_PASSWORD");
    		
    		} catch (IOException ex) {
    		 ex.printStackTrace();
    	 }	finally {
    		 if (input != null) {
    			 try {
    				 input.close();
    			 } catch (IOException e) {
				e.printStackTrace();
    			 }
    		 }
    	 }
    	
    	//Start Visibility Data Collection for Ping Data from SmartX Boxes
    	PingStatusCollectClass pingStatusCollect = new PingStatusCollectClass(VISIBILITY_CENTER, MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, pboxMongoCollection, pboxstatusMongoCollection,pboxstatusMongoCollectionRT, BoxType);
    	pingStatusCollect.start();
    	try {
			TimeUnit.SECONDS.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//Update Instant Visibility Collection for Box status using Ping Data
    	PingStatusUpdateClass pingStatusUpdate = new PingStatusUpdateClass(SmartXBox_USER, SmartXBox_PASSWORD, MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, pboxMongoCollection, pboxstatusMongoCollectionRT, BoxType, OVS_VM_USER, OVS_VM_PASSWORD);
    	pingStatusUpdate.start(); 
        try {
			TimeUnit.SECONDS.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //Start Visibility Collection for VM's Data
        InstaceStatusNovaClass instanceNovaStatus = new InstaceStatusNovaClass(MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, OPENSTACK_USER_ID, OPENSTACK_PASSWORD, OPENSTACK_PROJECT_ID, OPENSTACK_ENDPOINT, vboxMongoCollection, vboxMongoCollectionRT);
        instanceNovaStatus.start();
        
        //Start Instant Visibility Collection for OVS Data
        ovsBridgeStatusClass bridgeStatus  = new ovsBridgeStatusClass(SmartXBox_USER, SmartXBox_PASSWORD, MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, pboxMongoCollection, ovsListMongoCollection, ovsstatusMongoCollection, BoxType, OVS_VM_USER, OVS_VM_PASSWORD);
        bridgeStatus.start();
        
        //Start Visibility Collection for OpenStack Bridges Data
        OpenStackBridgesStatus osBridgeStatus  = new OpenStackBridgesStatus(SmartXBox_USER, SmartXBox_PASSWORD, MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, pboxMongoCollection, flowConfigOpenStackMongoCollection, flowConfigOpenStackMongoCollectionRT, BoxType);
        osBridgeStatus.start();
        
        //Start Visibility Collection for ODL Flow Rules Data
        SDNControllerStatus sdnStatus = new SDNControllerStatus(MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, flowConfigMongoCollection, flowConfigMongoCollectionRT, devopscontrollers, ControllerUser, ControllerPassword);
        sdnStatus.start();
        
        //Start Visibility Collection for ODL Statistics Data
        SDNControllerStats sdnStats = new SDNControllerStats(MONGO_DB_HOST, MONGO_DB_PORT, MONGO_DB_DATABASE, flowStatsMongoCollection, flowStatsMongoCollectionRT, devopscontrollers, ControllerUser, ControllerPassword);
        sdnStats.start();
    }
}
