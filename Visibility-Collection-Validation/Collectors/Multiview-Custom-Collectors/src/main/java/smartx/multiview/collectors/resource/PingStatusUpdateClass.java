/**
 * @author Muhammad Usman
 * @version 0.1
 */

package smartx.multiview.collectors.resource;

import static java.util.Arrays.asList;

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

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;
import smartx.multiview.DataLake.MongoDB_Connector;

public class PingStatusUpdateClass implements Runnable {
	private Thread thread;
	private String ThreadName = "pBox Status Update Thread";
	private String SmartXBox_USER, SmartXBox_PASSWORD, ovsVM_USER, ovsVM_PASSWORD;
	private String m_status, m_status_new, d_status;
	private String boxName = "", activeVM, m_ip = "", d_ip = "", ovsVM1ip, ovsVM2ip, boxtype;
	private String pboxMongoCollection, pboxstatusMongoCollectionRT;
	private String[] BoxType;
	private FindIterable<Document> pBoxList;
	private FindIterable<Document> pBoxStatus;
	private List<String> bridges = new ArrayList<String>();
	private MongoDB_Connector mongoConnector;
	private Date timestamp;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Logger LOG = Logger.getLogger("pingUpdateFile");

	public PingStatusUpdateClass(String boxUser, String boxPassword, MongoDB_Connector MongoConn, String pbox,
			String pboxstatus, String[] boxType, String ovsVMUser, String ovsVMPass) {
		SmartXBox_USER = boxUser;
		SmartXBox_PASSWORD = boxPassword;
		ovsVM_USER = ovsVMUser;
		ovsVM_PASSWORD = ovsVMPass;
		mongoConnector = MongoConn;
		BoxType = boxType;
		pboxMongoCollection = pbox;
		pboxstatusMongoCollectionRT = pboxstatus;
	}

	public void getActiveVM(String serverIp, String command, String usernameString, String password) {
		try {
			Connection conn = new Connection(serverIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(usernameString, password);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			ch.ethz.ssh2.Session sess = conn.openSession();
			sess.execCommand(command);
			InputStream stdout = new StreamGobbler(sess.getStdout());
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (line != null) {
					activeVM = line;
				}
			}
			br.close();
			sess.close();
			conn.close();
		} catch (IOException e) {
			System.out.println("[INFO][OVS-VM][Box : " + serverIp + " Failed");
			LOG.debug("[" + dateFormat.format(timestamp) + "][INFO][PING][UPDATE]" + serverIp + " Failed"
					+ e.getStackTrace());
			e.printStackTrace(System.err);
		}
	}

	public String getBoxStatus(String serverMgmtIp, String serverDataIp, String command, String usernameString,
			String password, String type) {
		String InterfaceStatus = null;
		try {
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

			if (br.readLine() != null) {
				int index = 0;
				InterfaceStatus = "GREEN";
				ch.ethz.ssh2.Session sess2 = conn.openSession();
				sess2.execCommand(
						"sudo ovs-ofctl show brcap | grep vxlan | cut -f 1 -d : | cut -f 2 -d '(' | cut -f 1 -d ')'");
				stdout = new StreamGobbler(sess2.getStdout());
				br = new BufferedReader(new InputStreamReader(stdout));
				while (true) {
					String host = br.readLine();
					System.out.println(host);
					if (host == null) {
						if (index == 0)
							InterfaceStatus = "ORANGE";
						index = 1;
						// System.out.println("InterfaceStatus : "+InterfaceStatus);
						break;
					}

					index++;
					// System.out.print(host);
					if (host != null) {
						// System.out.println("Host : "+host);
						ch.ethz.ssh2.Session sess3 = conn.openSession();

						if (type.equals("B"))
							sess3.execCommand("ping -c 1 `sudo ovs-vsctl show | grep -A2 " + host
									+ " | grep remote_ip | cut -d '\"' -f 2` | grep ttl");
						else
							sess3.execCommand("ping -c 1 `sudo ovs-vsctl show | grep -A2 " + host
									+ " | grep remote_ip | cut -d '\"' -f 4` | grep ttl");

						stdout2 = new StreamGobbler(sess3.getStdout());
						br2 = new BufferedReader(new InputStreamReader(stdout2));
						if (br2.readLine() == null) {
							InterfaceStatus = "ORANGE";
							System.out.println(InterfaceStatus+ " " +serverMgmtIp);
							break;
						} else {
							InterfaceStatus = "GREEN";
							System.out.println(InterfaceStatus+ " " +serverMgmtIp);
						}
					}
				}
				br.close();
				sess.close();
				conn.close();
			} else {
				InterfaceStatus = "RED";
				sess.close();
				conn.close();
			}
		} catch (IOException e) {
			// System.out.println("[INFO][PING][UPDATE][Box : "+serverMgmtIp+" Failed");
			LOG.debug("[" + dateFormat.format(timestamp) + "][ERROR][PING][UPDATE][Box : " + serverMgmtIp + " Failed");
			e.printStackTrace(System.err);
		}

		return InterfaceStatus;
	}

	public void update_status() {
		timestamp = new Date();
		// pBoxList = db.getCollection(pboxMongoCollection).find(new Document("type",
		// BoxType).append("type", "C**"));
		/*pBoxList = mongoConnector.getDbConnection().getCollection(pboxMongoCollection)
				.find(new Document("$or", asList(new Document("type", BoxType[0]), new Document("type", BoxType[1]))));*/
		pBoxList = mongoConnector.getDbConnection().getCollection(pboxMongoCollection)
				.find(new Document("$or", asList(new Document("type", BoxType[0]), new Document("type", BoxType[1]))));

		pBoxList.forEach(new Block<Document>() {
			public void apply(final Document document) {
				boxName = (String) document.get("boxName");
				m_ip = (String) document.get("management_ip");
				d_ip = (String) document.get("data_ip");
				m_status = (String) document.get("management_ip_status");
				ovsVM1ip = (String) document.get("ovs_vm1");
				ovsVM2ip = (String) document.get("ovs_vm2");
				activeVM = (String) document.get("active_ovs_vm");
				boxtype = (String) document.get("boxType");
				
				// Get Management Plane Status & Update pBox Status Collection
				pBoxStatus = mongoConnector.getDataDB(pboxstatusMongoCollectionRT, "destination", m_ip);
				pBoxStatus.forEach(new Block<Document>() {
					public void apply(final Document document2) {
						m_status_new = document2.get("status").toString().toUpperCase();

						if (m_status_new.equalsIgnoreCase("UP")) {
							m_status_new = "GREEN";
							System.out.println(boxtype+" "+boxName);
							// Get Data Plane Status
							if (boxtype.equals("B")) {
								// Get Active OVS-VM
								//getActiveVM(m_ip, "virsh list | grep ovs-vm | grep running | awk '{print $2}'",
								//		SmartXBox_USER, SmartXBox_PASSWORD);
								activeVM="ovs-vm1";
								if (boxName.equals("SmartX-Box-MYREN") || boxName.equals("SmartX-Box-PH")) {
									d_status = getBoxStatus(activeVM.equals("ovs-vm1") ? ovsVM1ip : ovsVM2ip, d_ip,
										"ip r | grep " + d_ip, SmartXBox_USER,
										SmartXBox_PASSWORD, boxtype);
									System.out.println("Check 1"+boxName+" "+d_status);
								}
								else {
									d_status = getBoxStatus(activeVM.equals("ovs-vm1") ? ovsVM1ip : ovsVM2ip, d_ip,
											"ip r | grep " + d_ip, ovsVM_USER,
											ovsVM_PASSWORD, boxtype);
									System.out.println("Check 2"+boxName+" "+d_status);
								}
							}
							else{
								d_status = getBoxStatus(m_ip, d_ip,
										"ip r | grep " + d_ip, "visibility",
										SmartXBox_PASSWORD, boxtype);
								System.out.println("Check 3"+boxName+" "+d_status);
							}
						} else {
							m_status_new = "RED";
							d_status = "RED";
						}

						UpdateResult result = mongoConnector.getDbConnection().getCollection(pboxMongoCollection)
								.updateOne(new Document("management_ip", m_ip),
										new Document("$set", new Document("management_ip_status", m_status_new)
												.append("data_ip_status", d_status).append("active_ovs_vm", activeVM)));
						
						document.remove("_id");
						document.put("timestamp", new Date());
						mongoConnector.getDbConnection().getCollection("pbox-list-history").insertOne(document);

						LOG.debug("[" + dateFormat.format(timestamp) + "][INFO][PING][UPDATE][Box: " + m_ip
								+ " Management Status: " + m_status_new + " Data Status: " + d_status + " Active VM: "
								+ activeVM + " Records Updated :" + result.getModifiedCount() + "]");
						// System.out.println("["+dateFormat.format(timestamp)+"][INFO][PING][MVC][Box:
						// "+m_ip+" Management Status: "+m_status_new+" Data Status: "+d_status+" Active
						// VM: "+activeVM+" Records Updated :"+result.getModifiedCount()+"]");
						activeVM = null;
					}
				});
			}
		});
	}

	public void run() {
		while (true) {
			update_status();
			try {
				// Sleep For 30 Seconds
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void start() {
		System.out.println("Starting pBox Status Update Thread");
		if (thread == null) {
			thread = new Thread(this, ThreadName);
			thread.start();
		}

	}
}
