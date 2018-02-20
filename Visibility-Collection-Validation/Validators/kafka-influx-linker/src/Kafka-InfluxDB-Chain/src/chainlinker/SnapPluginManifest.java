package chainlinker;

import java.util.HashMap;

/*
 * This class serve as a manifest for loading required SnapPluginParsers.
 * When a SnapPluginParser class is added to add another Snap plugin, then it must be registered here.
 */
public class SnapPluginManifest {
	private HashMap<String, Class<? extends SnapPluginParser>> pluginManifestMap = new HashMap<>();
	
	private SnapPluginManifest() {
		pluginManifestMap.put("snap-plugin-collector-psutil", SnapPSUtilParser.class);
		pluginManifestMap.put("snap-plugin-collector-cpu", SnapCPUParser.class);
		pluginManifestMap.put("snap-plugin-collector-meminfo", SnapMemInfoParser.class);
		pluginManifestMap.put("snap-plugin-collector-df", SnapDFParser.class);
	}
	
	public HashMap<String, Class<? extends SnapPluginParser>> getPluginManifestMap() {
		return pluginManifestMap;
	}
	
	// Singleton part for this class as this class does not need to exist in multitude.
	private static SnapPluginManifest instance = new SnapPluginManifest();
	public static SnapPluginManifest getInstance () {
		return instance;
	}		

}
