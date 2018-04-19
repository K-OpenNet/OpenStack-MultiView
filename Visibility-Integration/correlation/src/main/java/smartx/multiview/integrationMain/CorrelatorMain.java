package smartx.multiview.integrationMain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.PropertyConfigurator;
import smartx.multiview.workers.*;
import smartx.multiview.sdncontrollers.*;

public class CorrelatorMain 
{
	protected Date startDate        = new Date();//new Date(System.currentTimeMillis() - 3600 * 4 * 1000);
    private Date endDate          = new Date();//new Date(System.currentTimeMillis() - 3600 * 1 * 1000);
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
    private String key            = "192.168.1.1,8081,192.168.1.4,eth.ip.tcp", srcIP, srcPort, destIP, destPort, protocol, srcpBox, destpBox;
    private String topic          = "FlowVisibility";
    private String collection     = "flowlevel-data";
    private String zookeeper      = "x.x.x.x:2181";
    private String mongodb        = "x.x.x.x";
    private String elasticsearch  = "x.x.x.x";
    
    public CorrelatorMain(String sDate, String eDate, String src, String dest, String srcP, String devontroller, String prot, String opscontroller, CorrelatorGUIWindow GuiWindow) 
    {
    	PropertyConfigurator.configure("log4j.properties");
    	System.out.println("Starting Consumer ...");
    	srcIP    = src;
    	destIP   = dest;
    	srcPort  = srcP;
    	protocol = prot;
    	key = srcIP+","+srcPort+","+destIP+","+protocol;
    	
    	try {
			startDate = dateFormat.parse(sDate);
			endDate = dateFormat.parse(eDate);
		}catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	SourceWorker srcWorker = new SourceWorker(zookeeper, mongodb, elasticsearch, "flow-consumer1", collection, key, startDate, endDate, srcIP, destIP, srcPort, protocol, GuiWindow);
    	srcWorker.function1();
    	srcWorker.function2();
    	srcWorker.function4();
    	
    	DestinationWorker destWorker = new DestinationWorker(zookeeper, mongodb, elasticsearch, "flow-consumer1", collection, key, startDate, endDate, srcIP, destIP, srcPort, protocol, GuiWindow);
    	destWorker.function1();
    	destWorker.function2();
    	destWorker.function4();
    	
    	ControllerFlows controllerflows=new ControllerFlows(elasticsearch, devontroller, opscontroller);
    	controllerflows.getFlowDetailsOps();
    	controllerflows.getFlowDetailsDev();
    	controllerflows.getTopology();
    }
}






