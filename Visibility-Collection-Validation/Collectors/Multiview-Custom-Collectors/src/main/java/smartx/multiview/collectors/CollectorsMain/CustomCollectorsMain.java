package smartx.multiview.collectors.CollectorsMain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import smartx.multiview.collectors.flow.*;
import smartx.multiview.collectors.resource.*;
import smartx.multiview.DataLake.*;

public class CustomCollectorsMain 
{
	private String VISIBILITY_CENTER;
	private String MONGO_DB_HOST;
	private int    MONGO_DB_PORT;
	private String MONGO_DB_DATABASE;
	private String OPENSTACK_PASSWORD;
	private String OPENSTACK_USER_ID;
	private String OPENSTACK_PROJECT_ID;
	private String OPENSTACK_ENDPOINT;
	private String devopscontrollers;
	private String ControllerPassword;
	private String ControllerUser;
	private String SmartXBox_USER;
	private String SmartXBox_PASSWORD;
	private String OVS_VM_USER;
	private String OVS_VM_PASSWORD;
	private String pboxMongoCollection                  = "configuration-pbox-list";
	private String vboxMongoCollection                  = "resourcelevel-os-instance-detail";
	private String vboxMongoCollectionRT                = "configuration-vbox-list";
	private String pboxstatusMongoCollection            = "resourcelevel-ppath";
	private String pboxstatusMongoCollectionRT          = "resourcelevel-ppath-rt";
	private String ovsListMongoCollection               = "configuration-vswitch-list";
	private String ovsstatusMongoCollection             = "configuration-vswitch-status";
	private String flowConfigMongoCollection            = "flow-configuration-sdn-controller";
	private String flowConfigMongoCollectionRT          = "flow-configuration-sdn-controller-rt";
	private String flowStatsMongoCollection             = "flow-stats-sdn-controller";
	private String flowStatsMongoCollectionRT           = "flow-stats-sdn-controller-rt";
	private String flowConfigOpenStackMongoCollection   = "flow-stats-openstack-bridges";
	private String flowConfigOpenStackMongoCollectionRT = "flow-stats-openstack-bridges-rt";
	private String [] BoxType = {"B**", "C**"};
	
	public String getVISIBILITY_CENTER() {
		return VISIBILITY_CENTER;
	}
	
	public String getMONGO_DB_HOST() {
		return MONGO_DB_HOST;
	}

	public int getMONGO_DB_PORT() {
		return MONGO_DB_PORT;
	}

	public String getMONGO_DB_DATABASE() {
		return MONGO_DB_DATABASE;
	}

	public String getOPENSTACK_PASSWORD() {
		return OPENSTACK_PASSWORD;
	}

	public String getOPENSTACK_USER_ID() {
		return OPENSTACK_USER_ID;
	}

	public String getOPENSTACK_PROJECT_ID() {
		return OPENSTACK_PROJECT_ID;
	}

	public String getOPENSTACK_ENDPOINT() {
		return OPENSTACK_ENDPOINT;
	}

	public String getDevopscontrollers() {
		return devopscontrollers;
	}

	public String getControllerPassword() {
		return ControllerPassword;
	}

	public String getControllerUser() {
		return ControllerUser;
	}

	public String getSmartXBox_USER() {
		return SmartXBox_USER;
	}

	public String getSmartXBox_PASSWORD() {
		return SmartXBox_PASSWORD;
	}

	public String getOVS_VM_USER() {
		return OVS_VM_USER;
	}

	public String getOVS_VM_PASSWORD() {
		return OVS_VM_PASSWORD;
	}

	public String getPboxMongoCollection() {
		return pboxMongoCollection;
	}

	public String getVboxMongoCollection() {
		return vboxMongoCollection;
	}

	public String getVboxMongoCollectionRT() {
		return vboxMongoCollectionRT;
	}

	public String getPboxstatusMongoCollection() {
		return pboxstatusMongoCollection;
	}

	public String getPboxstatusMongoCollectionRT() {
		return pboxstatusMongoCollectionRT;
	}

	public String getOvsListMongoCollection() {
		return ovsListMongoCollection;
	}

	public String getOvsstatusMongoCollection() {
		return ovsstatusMongoCollection;
	}

	public String getFlowConfigMongoCollection() {
		return flowConfigMongoCollection;
	}

	public String getFlowConfigMongoCollectionRT() {
		return flowConfigMongoCollectionRT;
	}

	public String getFlowStatsMongoCollection() {
		return flowStatsMongoCollection;
	}

	public String getFlowStatsMongoCollectionRT() {
		return flowStatsMongoCollectionRT;
	}

	public String getFlowConfigOpenStackMongoCollection() {
		return flowConfigOpenStackMongoCollection;
	}

	public String getFlowConfigOpenStackMongoCollectionRT() {
		return flowConfigOpenStackMongoCollectionRT;
	}

	public String[] getBoxType() {
		return BoxType;
	}
				
    public void getProperties(){
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
    }
    
    public static void main( String[] args )
    {
    	CustomCollectorsMain ccMain = new CustomCollectorsMain();
		ccMain.getProperties();
		
		MongoDB_Connector MongoConnector = new MongoDB_Connector();
		MongoConnector.setDbConnection(ccMain.MONGO_DB_HOST, ccMain.MONGO_DB_PORT, ccMain.MONGO_DB_DATABASE);

    	//Start Visibility Data Collection for Ping Data from SmartX Boxes
    	PingStatusCollectClass pingStatusCollect = new PingStatusCollectClass(ccMain.VISIBILITY_CENTER, MongoConnector, ccMain.pboxMongoCollection, ccMain.pboxstatusMongoCollection,ccMain. pboxstatusMongoCollectionRT, ccMain.BoxType);
    	pingStatusCollect.start();
    	try {
			TimeUnit.SECONDS.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//Update Instant Visibility Collection for Box status using Ping Data
    	PingStatusUpdateClass pingStatusUpdate = new PingStatusUpdateClass(ccMain.SmartXBox_USER, ccMain.SmartXBox_PASSWORD, MongoConnector, ccMain.pboxMongoCollection, ccMain.pboxstatusMongoCollectionRT, ccMain.BoxType, ccMain.OVS_VM_USER, ccMain.OVS_VM_PASSWORD);
    	pingStatusUpdate.start(); 
        try {
			TimeUnit.SECONDS.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //Start Visibility Collection for VM's Data
        InstaceStatusNovaClass instanceNovaStatus = new InstaceStatusNovaClass(ccMain.MONGO_DB_HOST, ccMain.MONGO_DB_PORT, ccMain.MONGO_DB_DATABASE, ccMain.OPENSTACK_USER_ID, ccMain.OPENSTACK_PASSWORD, ccMain.OPENSTACK_PROJECT_ID, ccMain.OPENSTACK_ENDPOINT, ccMain.vboxMongoCollection, ccMain.vboxMongoCollectionRT);
        instanceNovaStatus.start();
        
        //Start Instant Visibility Collection for OVS Data
        ovsBridgeStatusClass bridgeStatus  = new ovsBridgeStatusClass(ccMain.SmartXBox_USER, ccMain.SmartXBox_PASSWORD, ccMain.MONGO_DB_HOST, ccMain.MONGO_DB_PORT, ccMain.MONGO_DB_DATABASE, ccMain.pboxMongoCollection, ccMain.ovsListMongoCollection, ccMain.ovsstatusMongoCollection, ccMain.BoxType, ccMain.OVS_VM_USER, ccMain.OVS_VM_PASSWORD);
        bridgeStatus.start();
        
        //Start Visibility Collection for OpenStack Bridges Data
        OpenStackBridgesStatus osBridgeStatus  = new OpenStackBridgesStatus(ccMain.SmartXBox_USER, ccMain.SmartXBox_PASSWORD, ccMain.MONGO_DB_HOST, ccMain.MONGO_DB_PORT, ccMain.MONGO_DB_DATABASE, ccMain.pboxMongoCollection, ccMain.flowConfigOpenStackMongoCollection, ccMain.flowConfigOpenStackMongoCollectionRT, ccMain.BoxType);
        osBridgeStatus.start();
        
        //Start Visibility Collection for ODL Flow Rules Data
        SDNControllerStatus sdnStatus = new SDNControllerStatus(ccMain.MONGO_DB_HOST, ccMain.MONGO_DB_PORT, ccMain.MONGO_DB_DATABASE, ccMain.flowConfigMongoCollection, ccMain.flowConfigMongoCollectionRT, ccMain.devopscontrollers, ccMain.ControllerUser, ccMain.ControllerPassword);
        sdnStatus.start();
        
        //Start Visibility Collection for ODL Statistics Data
        SDNControllerStats sdnStats = new SDNControllerStats(ccMain.MONGO_DB_HOST, ccMain.MONGO_DB_PORT, ccMain.MONGO_DB_DATABASE, ccMain.flowStatsMongoCollection, ccMain.flowStatsMongoCollectionRT, ccMain.devopscontrollers, ccMain.ControllerUser, ccMain.ControllerPassword);
        sdnStats.start();
    }
}
