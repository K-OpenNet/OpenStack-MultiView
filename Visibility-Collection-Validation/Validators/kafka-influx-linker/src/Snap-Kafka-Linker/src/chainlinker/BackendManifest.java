package chainlinker;

import java.util.HashMap;

public class BackendManifest {
	private HashMap<String, Class<? extends Backend>> backendManifestMap = new HashMap<>();
	
	private BackendManifest() {
		backendManifestMap.put("influxdb", BackendInfluxDB.class);
	}
	
	public HashMap<String, Class<? extends Backend>> getBackendManifestMap() {
		return backendManifestMap;
	}
	
	// Singleton part for this class as this class does not need to exist in multitude.
	private static BackendManifest instance = new BackendManifest();
	public static BackendManifest getInstance () {
		return instance;
	}		
}
