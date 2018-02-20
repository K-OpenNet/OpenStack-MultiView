package chainlinker;

import java.util.HashMap;

/*
 * Corresponding to snap-plugin-collector-cpu
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:cpu:2
 */
public class SnapCPUParser extends SnapPluginParser {
	// The regex must be updated when data type name changes.
	String regex = "^intel\\/procfs\\/cpu\\/(0|[1-9][0-9]*|all)\\/utilization_percentage$";
	
	public void loadMap(@SuppressWarnings("rawtypes") HashMap<String, Class> typeMap) {
		typeMap.put("intel/procfs/cpu/all/utilization_percentage", lfClass);
	}
	
	public void addField(
			org.influxdb.dto.Point.Builder pointBuilder, 
			String dataTypeName, 
			Object data
			) throws ClassNotFoundException {
		if (!isParsible(dataTypeName))  throw new ClassNotFoundException ();
		
		try {
			ReflectivePointFieldFeeder.addField(
					pointBuilder, lfClass, data);			
		} catch (ClassNotFoundException e) {
			throw new ClassNotFoundException ();
		}
	}
	
	public boolean isParsible(String dataTypeName) {
		if (dataTypeName.matches(regex)) {
			return true;			
		} else {
			return false;			
		}
	}	
}