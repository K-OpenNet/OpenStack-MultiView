package com.gist.netcs.multiview;

public class ConfigUpdaterMain 
{
	private static String dbHost = "103.22.221.55";
	private static int    dbPort = 27017;
	private static String dbName = "smartxdb";
	private static String pboxMongoCollection          = "configuration-pbox-list";
	private static String vboxMongoCollection          = "configuration-vbox-list";
	private static String vboxosMongoCollection        = "resourcelevel-os-instance-detail";
	private static String pboxstatusMongoCollection    = "resourcelevel-ppath-rt";
	private static String ovsListMongoCollection       = "configuration-vswitch-list";
	private static String ovsstatusMongoCollection     = "configuration-vswitch-status";
	private static String flowConfigMongoCollection    = "flow-configuration-sdn-controller";
	private static String flowConfigMongoCollectionRT  = "flow-configuration-sdn-controller-rt";
	private static String flowStatsMongoCollection     = "flow-stats-sdn-controller";
	private static String flowStatsMongoCollectionRT   = "flow-stats-sdn-controller-rt";
	private static String devopscontrollers            = "103.22.221.152";
	private static String ControllerPassword           = "admin";
	private static String ControllerUser               = "admin";
	private static String [] BoxType = {"B**", "C**"};
			
    public static void main( String[] args )
    {
        PingStatusClass pingStatus         = new PingStatusClass(dbHost, dbPort, dbName, pboxMongoCollection, pboxstatusMongoCollection, BoxType);
        pingStatus.start(); 
        InstanceStatusClass instanceStatus = new InstanceStatusClass(dbHost, dbPort, dbName, pboxMongoCollection, vboxMongoCollection, vboxosMongoCollection);
        instanceStatus.start();
        ovsBridgeStatusClass bridgeStatus  = new ovsBridgeStatusClass(dbHost, dbPort, dbName, pboxMongoCollection, ovsListMongoCollection, ovsstatusMongoCollection, BoxType);
        bridgeStatus.start();
        SDNControllerStatusClass sdnStatus = new SDNControllerStatusClass(dbHost, dbPort, dbName, flowConfigMongoCollection, flowConfigMongoCollectionRT, devopscontrollers, ControllerUser, ControllerPassword);
        sdnStatus.start();
        SDNControllerStatsClass sdnStats = new SDNControllerStatsClass(dbHost, dbPort, dbName, flowStatsMongoCollection, flowStatsMongoCollectionRT, devopscontrollers, ControllerUser, ControllerPassword);
        sdnStats.start();
    }
}
