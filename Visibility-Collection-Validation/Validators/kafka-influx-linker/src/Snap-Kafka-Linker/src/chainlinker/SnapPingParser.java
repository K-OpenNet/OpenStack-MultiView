package chainlinker;

/*
 * Corresponding class to snap-plugin-collector-iostat
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:ping:1
 */
public class SnapPingParser extends SnapPluginParser {

	public SnapPingParser() {
		// All these data forms must be updated when snap publisher's output format is changed.
		
		typeMap.put("/raintank/ping/avg", lfClass);
		typeMap.put("/raintank/ping/min", lfClass);
		typeMap.put("/raintank/ping/max", lfClass);
		typeMap.put("/raintank/ping/median", lfClass);
		typeMap.put("/raintank/ping/mdev", lfClass);
		typeMap.put("/raintank/ping/loss", lfClass);
	}

}
