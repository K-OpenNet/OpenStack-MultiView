package chainlinker;

/*
 * Corresponding to snap-plugin-collector-df
 * 
 * CAUTION: This setting may fail in case if the plugins' version mismatch with the below.
 * - collector:df:2
 */
public class SnapDFParser extends SnapPluginParser {
	public SnapDFParser() {
		super();		
		// All these data forms must be updated when snap publisher's output format is changed.
		
		// Pattern: /intel/procfs/filesystem/(Any strings without /)/(space_percent_free or space_percent_reserved or space_percent_used or inodes_percent_free or inodes_percent_reserved or inodes_percent_used)
		regexTypeMap.put("^\\/intel\\/procfs\\/filesystem\\/((?!\\/).)*\\/(space_percent_free|space_percent_reserved|space_percent_used|inodes_percent_free|inodes_percent_reserved|inodes_percent_used)$", lfClass);
		// Pattern: /intel/procfs/filesystem/(Any strings without /)/(space_free or space_reserved or space_used or inodes_free or inodes_reserved or inodes_used)
		regexTypeMap.put("^\\/intel\\/procfs\\/filesystem\\/((?!\\/).)*\\/(space_free|space_reserved|space_used|inodes_free|inodes_reserved|inodes_used)$", lClass);
		// Pattern: /intel/procfs/filesystem/(Any strings without /)/(device_name or device_type)
		regexTypeMap.put("^\\/intel\\/procfs\\/filesystem\\/((?!\\/).)*\\/(device_name|device_type)$", sClass);
		
		regexSet = regexTypeMap.keySet();
	}			
	
}
