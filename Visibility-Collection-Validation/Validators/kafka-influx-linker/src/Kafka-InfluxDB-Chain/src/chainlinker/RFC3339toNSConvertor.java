package chainlinker;
import java.time.Instant;

public class RFC3339toNSConvertor {
	/*
		I don't know why there is no pre-existing library for this, but converting RFC3339 with zone
		offset AND WITH nanosecond digits does not exist. So here I wrote the code instead.
		
		For anyone who might come to repair this: If there are any standard library does this function,
		then replace this one with it from this entire code.
	 */
	
	/*
		Converting timestamp in RFC3339 standard into TimeUnit.NANOSECONDS form.
		String "2016-07-21T16:41:50.679207324+09:00" -> long 1469086910679207324L
	 */
	public static long ToNS(String timestamp) {
		int offset_pos = 23; // The position right after millisecond digit.
		
		// Catching the exact starting position of zone offset data
		long offset_millis = 0;
		while (offset_pos < timestamp.length()) {
			if (!Character.isDigit(timestamp.charAt(offset_pos))) {
				break;
			}
			offset_pos++;
		}
		
		char offset_ops = timestamp.charAt(offset_pos);
		if (offset_ops != 'Z') {
			// If the beginning char is not 'Z', it means the date is not UTC and must be re-adjusted
			// to UTC +00:00 (aka Z)
			String offset_string = timestamp.substring(offset_pos + 1);
			String[] values = offset_string.split(":");
			offset_millis = (Integer.parseInt(values[0]) * 3600 + Integer.parseInt(values[1]) * 60) * 1000;
			offset_millis *= offset_ops == '+' ? -1 : 1; // If offset is plus, then the value must be subtracted and vice versa.
		}
		// Using the existing timestamp parser with millisecond unit and concatenates the digits below
		// to it. 
		long time = (Instant.parse(timestamp.substring(0, 23) + "Z").toEpochMilli() + offset_millis)* 1000000 + Long.parseLong(timestamp.substring(23, offset_pos));
		
		return time;
	}
}