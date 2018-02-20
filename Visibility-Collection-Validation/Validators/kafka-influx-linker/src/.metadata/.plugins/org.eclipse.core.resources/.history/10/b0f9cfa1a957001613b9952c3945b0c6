package chainlinker;

public class SnapMemInfoParser extends SnapPluginParser {
	public SnapMemInfoParser() {
		super();
		// All these data forms must be updated when snap publisher's output format is changed.		
		typeMap.put("/intel/procfs/meminfo/mem_free", lClass);
		typeMap.put("/intel/procfs/meminfo/mem_available", lClass);
		typeMap.put("/intel/procfs/meminfo/mem_total", lClass);
		typeMap.put("/intel/procfs/meminfo/mem_used", lClass);		
	}	
}
