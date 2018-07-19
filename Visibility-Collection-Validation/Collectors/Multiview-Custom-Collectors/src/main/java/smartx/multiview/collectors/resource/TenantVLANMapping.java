/**
 * @author Muhammad Usman
 * @version 0.1
 */

package smartx.multiview.collectors.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;

public class TenantVLANMapping implements Runnable {
	private Thread thread;
	private String ThreadName = "Tenant VLAN Mapping Status Thread";
	private String tenant_VLAN_MongoCollection;
	private String CTRL_Box_IP, CTRL_Box_USER, CTRL_Box_PASSWORD;

	private MongoClient mongoClient;
	private List<Document> documentsRT = new ArrayList<Document>();
	private MongoDatabase db;
	private Document documentRT;

	private Date timestamp;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Logger LOG = Logger.getLogger("novaUpdateFile");

	public TenantVLANMapping(String CTRL_IP, String CTRL_USER, String CTRL_PASSWORD, String dbHost, int dbPort,
			String dbName, String mongocollection) {
		mongoClient = new MongoClient(dbHost, dbPort);
		db = mongoClient.getDatabase(dbName);
		tenant_VLAN_MongoCollection = mongocollection;
		CTRL_Box_IP = CTRL_IP;
		CTRL_Box_USER = CTRL_USER;
		CTRL_Box_PASSWORD = CTRL_PASSWORD;
	}

	public void get_Teanant_VLAN_Mapping_List() {
		System.out.println("Running " + ThreadName);
		String tenantID, tenantName, userID, userName, vlanID;

		timestamp = new Date();
		try {
			Connection conn = new Connection(CTRL_Box_IP);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(CTRL_Box_USER, CTRL_Box_PASSWORD);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			ch.ethz.ssh2.Session sess = conn.openSession();
			sess.execCommand("cat /home/netcs/tenant.tags");

			InputStream stdout = new StreamGobbler(sess.getStdout());
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				if (line != null) {
					vlanID = line.substring(0, StringUtils.ordinalIndexOf(line, ",", 1)).trim();
					tenantID = line.substring(StringUtils.ordinalIndexOf(line, ",", 1) + 1,
							StringUtils.ordinalIndexOf(line, ",", 2)).trim();
					tenantName = line.substring(StringUtils.ordinalIndexOf(line, ",", 2) + 1,
							StringUtils.ordinalIndexOf(line, ",", 3)).trim();
					userID = "";
					userName = "";

					// Create MongoDB document fields for Bulk insert
					documentRT = new Document();
					documentRT.put("tenantid", tenantID);
					documentRT.put("tenantname", tenantName);
					documentRT.put("userid", userID);
					documentRT.put("username", tenantName);
					documentRT.put("vlan", vlanID);

					LOG.debug("[" + dateFormat.format(timestamp) + "][INFO][TenantVLANMapping][New Map is created]");

					documentsRT.add(documentRT);
				}
			}

			br.close();
			stdout.close();
			sess.close();
			conn.close();
		} catch (IOException e) {
			LOG.debug("[INFO][TenantVLANMapping][New Map is Failed]");
			System.out.println("[INFO][TenantVLANMapping][New Map is Failed]");
			e.printStackTrace(System.err);
		}
	}

	public void run() {
		while (true) {
			get_Teanant_VLAN_Mapping_List();

			// Delete Previous Documents from Real Time collection
			db.getCollection(tenant_VLAN_MongoCollection).deleteMany(new Document());

			// Insert New Documents for Near-Realtime Visualization
			if (documentsRT.isEmpty() == false) {
				db.getCollection(tenant_VLAN_MongoCollection).insertMany(documentsRT);
				documentsRT.clear();
			}

			try {
				// Sleep For 60 Seconds
				Thread.sleep(600000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}
