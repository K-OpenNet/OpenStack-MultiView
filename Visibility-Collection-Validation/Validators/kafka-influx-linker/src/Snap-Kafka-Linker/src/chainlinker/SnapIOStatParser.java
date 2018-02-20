package chainlinker;

/*
 * Corresponding class to snap-plugin-collector-iostat
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:iostat:4
 */
public class SnapIOStatParser extends SnapPluginParser {

	public SnapIOStatParser() {
		// All these data forms must be updated when snap publisher's output format is changed.
		
		typeMap.put("/intel/linux/iostat/avg-cpu/%user", lfClass);
		typeMap.put("/intel/linux/iostat/avg-cpu/%nice", lfClass);
		typeMap.put("/intel/linux/iostat/avg-cpu/%system", lfClass);
		typeMap.put("/intel/linux/iostat/avg-cpu/%iowait", lfClass);
		typeMap.put("/intel/linux/iostat/avg-cpu/%steal", lfClass);
		typeMap.put("/intel/linux/iostat/avg-cpu/%idle", lfClass);
		
		// Pattern: /intel/linux/iostat/device/(alphanumerical(lowercase only) or - or just ALL)/(rrqm_per_sec or wrqm_per_sec or r_per_sec or w_per_sec or rkB_per_sec or wkB_per_sec)
		regexTypeMap.put("^\\/intel\\/linux\\/iostat\\/device\\/(([0-9]|[a-z]|\\-)*|ALL)\\/(rrqm_per_sec|wrqm_per_sec|r_per_sec|w_per_sec|rkB_per_sec|wkB_per_sec)$", lfClass);
		// Pattern: /intel/linux/iostat/device/(alphanumerical(lowercase only) or - or just ALL)/(avgrq-sz or avgqu-sz or await or r_await or w_await or svctm or %util)
		regexTypeMap.put("^\\/intel\\/linux\\/iostat\\/device\\/(([0-9]|[a-z]|\\-)*|ALL)\\/(avgrq-sz|avgqu-sz|await|r_await|w_await|svctm|%util)$", lfClass);
		
		regexSet = regexTypeMap.keySet();
	}

}
