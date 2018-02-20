package chainlinker;

/*
 * Corresponding to snap-plugin-collector-cpu
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:cpu:2
 */
public class SnapCPUParser extends SnapPluginParser {
	public SnapCPUParser() {
		super();
		// All these data forms must be updated when snap publisher's output format is changed.
		
		typeMap.put("/intel/procfs/cpu/all/utilization_percentage", lfClass);
		
		// The regex must be updated when data type name changes.
		
		// Pattern: /intel/procfs/cpu/(0 or numbers not starting with 0)/utilization_percentage
		regexTypeMap.put("^\\/intel\\/procfs\\/cpu\\/(0|[1-9][0-9]*|all)\\/utilization_percentage$", lfClass);
		
		regexSet = regexTypeMap.keySet();
	}			
	
}
