package chainlinker;

public class SnapLibVirtParser extends SnapPluginParser {
	public SnapLibVirtParser() {
		super();
		// All these data forms must be updated when snap publisher's output format is changed.
		
		// Pattern: /intel/libvirt/(alphanumerical or _ or -)/disk/(alphanumerical(lowercase only) or _ or .)/(wrreq or rdreq or wrbytes or rdbytes)
		regexTypeMap.put("^\\/intel\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/disk\\/([0-9]|[a-z]|_|\\.)*\\/(wrreq|rdreq|wrbytes|rdbytes)$", lClass);
		// Pattern: /intel/libvirt/(alphanumerical or _ or -)/memory/(mem or swap_in or swap_out or major_fault or minor_fault or free or max)
		regexTypeMap.put("^\\/intel\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/memory\\/(mem|swap_in|swap_out|major_fault|minor_fault|free|max)$", lClass);
		// Pattern: /intel/libvirt/(alphanumerical or _ or -)/cpu/cputime
		regexTypeMap.put("^\\/intel\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/cpu\\/cputime$", lClass);
		// Pattern: /intel/libvirt/(alphanumerical or _ or -)/cpu/cputime/(numerical not starting with 0 or 0 itself.)
		regexTypeMap.put("^\\/intel\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/cpu\\/cputime\\/(0|[1-9][0-9]*)$", lClass);
		// Pattern: /intel/libvirt/(alphanumerical or _ or -)/network/(alphanumerical(lowercase only) or _ or . or -)/(rxbytes or rxpackets or rxerrs or rxdrop or txbytes or txpackets or txerrs or txdrop)
		regexTypeMap.put("^\\/intel\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/network\\/([0-9]|[a-z]|_|\\-|\\.)*\\/(rxbytes|rxpackets|rxerrs|rxdrop|txbytes|txpackets|txerrs|txdrop)$", lClass);
		
		// Obsolete types : Just commented for possible bug or later use.
		// Pattern: /intel/libvirt/(alphanumerical or _ or -)/cpu/vcpu/(numerical not starting with 0 or 0 itself.)/cputime
		// regexTypeMap.put("^\\/intel\\/libvirt\\/([0-9]|[a-z]|_|\\-)*\\/cpu\\/vcpu\\/(0|[1-9][0-9]*)\\/cputime$", lClass);
		
		regexSet = regexTypeMap.keySet();	
	}
}
