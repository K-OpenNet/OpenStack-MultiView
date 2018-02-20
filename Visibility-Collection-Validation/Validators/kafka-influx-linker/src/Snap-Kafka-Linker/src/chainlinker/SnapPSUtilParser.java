package chainlinker;

/*
 * Corresponding to snap-plugin-collector-psutil
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:psutil:6
 */
public class SnapPSUtilParser extends SnapPluginParser {
	public SnapPSUtilParser() {
		super();
		// All these data forms must be updated when snap publisher's output format is changed.
		
		typeMap.put("/intel/psutil/load/load1", lfClass);
		typeMap.put("/intel/psutil/load/load5", lfClass);
		typeMap.put("/intel/psutil/load/load15", lfClass);
		typeMap.put("/intel/psutil/vm/active", lClass);
		typeMap.put("/intel/psutil/vm/available", lClass);
		typeMap.put("/intel/psutil/vm/buffers", lClass);
		typeMap.put("/intel/psutil/vm/cached", lClass);
		typeMap.put("/intel/psutil/vm/free", lClass);
		typeMap.put("/intel/psutil/vm/inactive", lClass);
		typeMap.put("/intel/psutil/vm/total", lClass);
		typeMap.put("/intel/psutil/vm/used", lClass);
		typeMap.put("/intel/psutil/vm/used_percent", lfClass);
		typeMap.put("/intel/psutil/vm/wired", lClass);
		// typeMap.put("/intel/psutil/vm/shared", lClass); // Currently the snap plugin is broken.
		typeMap.put("/intel/psutil/net/all/bytes_recv", lClass);
		typeMap.put("/intel/psutil/net/all/bytes_sent", lClass);
		typeMap.put("/intel/psutil/net/all/dropin", lClass);
		typeMap.put("/intel/psutil/net/all/dropout", lClass);
		typeMap.put("/intel/psutil/net/all/errin", lClass);
		typeMap.put("/intel/psutil/net/all/errout", lClass);
		typeMap.put("/intel/psutil/net/all/packets_recv", lClass);
		typeMap.put("/intel/psutil/net/all/packets_sent", lClass);
		
		// Pattern: /intel/psutil/net/(alphanumerical(lowercase only) or _ or . or -)/(bytes_recv or bytes_sent or packets_recv or packets_sent)
		regexTypeMap.put("^\\/intel\\/psutil\\/net\\/([0-9]|[a-z]|_|\\.|-)*\\/(bytes_recv|bytes_sent|dropin|dropout|errin|errout|packets_recv|packets_sent)$", lClass);
		// Pattern: /intel/psutil/cpu/cpu(numerical not starting with 0 or 0 itself.)/(bytes_recv or bytes_sent or packets_recv or packets_sent)
		// regexTypeMap.put("^\\/intel\\/psutil\\/cpu\\/cpu(0|[1-9][0-9]*)\\/(guest|guest_nice|idle|iowait|irq|nice|softirq|steal|stolen|system|user)$", lfClass);
		// Currently "idle" value is given as null. Snap plugin is broken.
		regexTypeMap.put("^\\/intel\\/psutil\\/cpu\\/cpu(0|[1-9][0-9]*)\\/(guest|guest_nice|iowait|irq|nice|softirq|steal|stolen|system|user)$", lfClass);
		
		regexSet = regexTypeMap.keySet();
	}
}