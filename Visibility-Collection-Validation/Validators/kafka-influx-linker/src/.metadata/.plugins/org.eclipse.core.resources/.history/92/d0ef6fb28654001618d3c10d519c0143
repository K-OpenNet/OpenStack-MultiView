package chainlinker;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.Point;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 * Corresponding to snap-plugin-collector-psutil
 * 
 * CAUTION: This parser may fail in case if the plugins' version mismatch with the below.
 * - collector:psutil:6
 * - publisher:kafka:7
 * 
 */
public class SnapPSUtilParser extends SnapPluginParser {
	private static final Logger logger = LogManager.getLogger(SnapPSUtilParser.class);
	
	public Point parse(JSONObject dataObj) throws NullPointerException, ClassNotFoundException {
		logger.trace("Parsing...");
		
		// name, source, unit, time, value

		// Extraction of name. String name will be the measurement in influxDB.
		JSONArray namespace = (JSONArray)getSafe(dataObj, "namespace");
		StringJoiner nameSJ = new StringJoiner("/", "", "");
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> namespace_iterator = namespace.iterator();
		while (namespace_iterator.hasNext()) {
			nameSJ.add(getSafe(namespace_iterator.next(),"Value").toString());
		}
		String name = nameSJ.toString();

		// Extraction of source.
		String source = (String)((JSONObject)getSafe(dataObj, "tags")).get("plugin_running_on");

		// Extraction of unit.
//		String unit = (String)getSafe(dataObj, "Unit_");
		String unit = (String)getSafe(dataObj, "Unit_");

		// Extraction of time.
		String timestamp = (String)getSafe(dataObj, "timestamp");
		long time = RFC3339toNSConvertor.ToNS(timestamp);

		logger.trace("Processed a data. (Time " + timestamp + " = " + time + " ns)");

		org.influxdb.dto.Point.Builder builder = Point.measurement(name)
				.time(time, TimeUnit.NANOSECONDS)
				.tag("source", source)
				.tag("unit", unit);

		PSUtilParserPref parserPref = PSUtilParserPref.getInstance();

		try {
			ReflectivePointFieldFeeder.addField(
					builder, parserPref.TypeMap.get(name), getSafe(dataObj, "data"));
		} catch (ClassNotFoundException e) {
			throw new ClassNotFoundException (name);
		}

		logger.trace("Parsing complete...");
		return builder.build();
	}

}
