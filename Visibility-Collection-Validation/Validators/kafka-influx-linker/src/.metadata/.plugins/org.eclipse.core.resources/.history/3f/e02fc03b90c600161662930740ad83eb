package chainlinker;

import java.io.InvalidClassException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class BackendInfluxDB extends Backend {
	private static final Logger logger = LogManager.getLogger(BackendInfluxDB.class);	
	
	private InfluxDBConfig influxDBConf;
	// Nested class to store and provice read-only access to InfluxDB-related setting values.
	public class InfluxDBConfig {
		private String address;
		private String id;
		private String password;
		private String db_name;
		private String retention_policy;
		private ConsistencyLevel consistency_level;

		public String getAddress() {
			return address;
		}
		public String getID() {
			return id;
		}
		public String getPassword() {
			return password;
		}
		public String getDBName() {
			return db_name;
		}
		public String getRetentionPolicy() {
			return retention_policy;
		}
		public ConsistencyLevel getConsistencyLevel() {
			return consistency_level;
		}
	}
	
	@Override
	public InfluxDBConfig getConfig() {
		return influxDBConf;
	}
	
	@Override
	public void loadConfig(JSONObject config_influx_json) throws ParseException {
		influxDBConf = new InfluxDBConfig();
		influxDBConf.address = (String)ConfigLoader.getValue(config_influx_json, "address");
		influxDBConf.id = (String)ConfigLoader.getValue(config_influx_json, "id");
		influxDBConf.password = (String)ConfigLoader.getValue(config_influx_json, "password");
		influxDBConf.db_name = (String)ConfigLoader.getValue(config_influx_json, "db_name");
		influxDBConf.retention_policy = (String)ConfigLoader.getValue(config_influx_json, "retention_policy");
		influxDBConf.consistency_level = getConsistencyLevel(config_influx_json);
	};	
	
	@Override
	public void processMessage(JSONArray msgValue, HashMap<String, SnapPluginParser> parserMap, LinkedList<SnapPluginParser> parserList) {
		// Creating InfluxDB instance to handle InfluxDB connection
		logger.trace("Opening a connection to InfluxDB server " + influxDBConf.getAddress() + " with ID " + influxDBConf.getID() + ".");
		InfluxDB influxDB = InfluxDBFactory.connect(influxDBConf.getAddress(), influxDBConf.getID(), influxDBConf.getPassword());
		// Reserving graceful shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				// When tested on Eclipse, this will not be executed.
				// But still on console this will work.
				influxDB.close();
				logger.debug("InfluxDB connection is closed.");
			}
		});
		
		String dbName = influxDBConf.getDBName();
		try {
			influxDB.createDatabase(dbName);
		} catch (RuntimeException e){
			// If the code reaches this block it means the DB already exists
		}

		// Only 1 query are sent to the DB per 1 Kafka message by batching points.
		logger.trace("Setting up a BatchPoints...");
		BatchPoints batchPoints = BatchPoints
				.database(dbName)
				.retentionPolicy(influxDBConf.getRetentionPolicy())
				.consistency(influxDBConf.getConsistencyLevel())
				.build();				

		@SuppressWarnings("unchecked")
		Iterator<JSONObject> msg_iterator = msgValue.iterator();
		logger.trace("Processing a message with " + msgValue.size() + " elements.");
		int count = 1;
		while (msg_iterator.hasNext()) { // For each data element
			logger.trace("Processing the element #" + count + ".");
			JSONObject dataObj = msg_iterator.next();
			try {
				batchPoints.point(parse(dataObj, parserMap, parserList));
			} catch (NullPointerException e) {
				e.printStackTrace();						
			} catch (ClassNotFoundException e) {
				logger.error("Failed to find data type info for given data with field name '" + e.getMessage() + "'", e);
			} catch (InvalidClassException e) {
				e.printStackTrace();						
			}
			count++;
		}

		influxDB.write(batchPoints);
	}
	
	// Gets each data JSONObject and return it as an Point to be fed into InfluxDB.
	@Override
	public Point parse(JSONObject dataObj, HashMap<String, SnapPluginParser> parserMap, LinkedList<SnapPluginParser> parserList) throws NullPointerException, ClassNotFoundException, InvalidClassException {
		logger.trace("Parsing...");
		
		/*
		 * InfluxDB Format
		 * 
		 * Measurement: name
		 * Tags: source, unit, time
		 * Field: value
		 */
		
		// Extraction of name.
		String name = (String)getSafe(dataObj, "namespace");
		
		// Extraction of tags.
		JSONObject tags_json = (JSONObject)getSafe(dataObj, "tags");

		// Extraction of unit.
		String unit = (String)getSafe(dataObj, "unit");

		// Extraction of time.
		String timestamp = (String)getSafe(dataObj, "timestamp");
		long time = RFC3339toNSConvertor.ToNS(timestamp); // TODO: Crash happens!

		logger.trace("Processed a data with time " + timestamp + " = " + time + " ns.");

		org.influxdb.dto.Point.Builder builder = Point.measurement(name)
				.time(time, TimeUnit.NANOSECONDS);
		
		for (Object key : tags_json.keySet()) {
			// All tags are given as String from Kafka.
			String tag_name = (String)key;
			builder.tag(tag_name, (String)tags_json.get(tag_name));
		}
		
		if (unit.length() > 0) {
			// This prevents 0 length String causing InfluxDB query parsing error.
			// If any other values make this error again, then more improvements may be required.
			builder.tag("unit", unit);
		}

		/*
		 * Not like previous tag data, the actual value 'data' can't be easily fed into the builder.
		 * First, there are data type problems. Some value may be float, others may be integer.
		 * 
		 * At first glance, field value's data type is never changed, so it would be easy to deal with.
		 * 
		 * But it isn't. Especially for float values. Sometimes a float type value would be with
		 * 0 below point. Then JSONParser will interpret this value as long type and when I try to
		 * convert it then it will throw type cast error.
		 * 
		 * To avert this, these specific values must be casted into long first and then into double.
		 * But this cannot be applied to all values; long type casting will remove digits below point.
		 * 
		 * So I made each parsers know their data type by their namespace in predefined map and
		 * made this class find appropriate parser for each data by their namespace.  
		 */
		
		// 1st pass : Fast searching for static names via HashMap
		SnapPluginParser parser = parserMap.get(name);
		if (parser == null) {
			// 2nd pass : Querying for parameterized names (ex. snap-plugin-collector-cpu)
			// Asking all registered parsers whether they can handle given data type
			for (SnapPluginParser parserIter : parserList) {
				if (parserIter.isParsible(name)) {
					parser = parserIter;
					break;					
				}
			}
		}
		
		if (parser == null) {
			logger.error("No matching parser found with data type '" + name + "'.");
			throw new ClassNotFoundException (name);
		}
		
		try {
			parser.addField(builder, name, getSafe(dataObj, "data"));
		} catch (ClassNotFoundException e) {
			logger.error("A parser '" + parser.getClass().getName() + "' corresponding to data type '" + name + "', but it does not know how to handle it.");
			throw new ClassNotFoundException (name);
		} catch (InvalidClassException e) {
			logger.error(e.getMessage());
			throw new InvalidClassException(null);
		}

		logger.trace("Parsing complete...");
		return builder.build();
	}	
	
	// This method is to describe how the parser will feed the given data into pointBuilder.
	ReflectivePointFieldFeeder rpff = new BackendInfluxDBRPFF();
	@Override
	public void addField(
			Object metricObject, 
			String dataTypeName, 
			Object data,
			SnapPluginParser parser
			) throws ClassNotFoundException, InvalidClassException {
		if (metricObject.getClass() != org.influxdb.dto.Point.Builder.class) {
			throw new InvalidClassException("Expecting '" + org.influxdb.dto.Point.Builder.class.getName() + "', but class '" + metricObject.getClass().getName() + "' is given!");
		}
		org.influxdb.dto.Point.Builder pointBuilder = (org.influxdb.dto.Point.Builder)metricObject; 
		
		if (!parser.isParsible(dataTypeName))  throw new ClassNotFoundException ();
		
		try {
			@SuppressWarnings("rawtypes")
			Class dataType = parser.typeMap.get(dataTypeName);
			if (dataType == null) {
				for (String regex : parser.regexSet) {
					if (dataTypeName.matches(regex)) {
						dataType = parser.regexTypeMap.get(regex);
					}
				}
			}
			if (dataType == null) {
				throw new ClassNotFoundException ();
			}
			
			rpff.addField(
					pointBuilder, dataType, data);
		} catch (ClassNotFoundException e) {
			logger.error("Given data type isn't supported by JSON format. Is it correct?");
			throw new ClassNotFoundException ();
		}
	}	
	
	// InfluxDB's ConsistencyLevel requires a different approach as it is not a String.
	protected ConsistencyLevel getConsistencyLevel(JSONObject json) throws ParseException {
		String lvl_str = ((String)ConfigLoader.getValue(json, "consistency_level")).toLowerCase();
		switch (lvl_str) {
		case "all" :
			return ConsistencyLevel.ALL;
		default:
			throw new ParseException(0);
		}
	}

}
