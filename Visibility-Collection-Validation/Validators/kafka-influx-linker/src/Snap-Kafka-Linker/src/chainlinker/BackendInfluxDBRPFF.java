package chainlinker;

import java.io.InvalidClassException;

public class BackendInfluxDBRPFF extends ReflectivePointFieldFeeder {

	public BackendInfluxDBRPFF() {
		super();
	}
	
	@Override
	protected void addString(Object metricObject, String value) throws InvalidClassException {
		org.influxdb.dto.Point.Builder pointBuilder = (org.influxdb.dto.Point.Builder)metricObject; 		
		pointBuilder.addField("value", (String)value);
	}
	@Override
	protected void addLong(Object metricObject, long value) throws InvalidClassException {
		org.influxdb.dto.Point.Builder pointBuilder = (org.influxdb.dto.Point.Builder)metricObject; 		
		pointBuilder.addField("value", (long)value);
	}
	@Override
	protected void addDouble(Object metricObject, double value) throws InvalidClassException {
		org.influxdb.dto.Point.Builder pointBuilder = (org.influxdb.dto.Point.Builder)metricObject; 		
		pointBuilder.addField("value", (double)value);
	}
	@Override
	protected void addBoolean(Object metricObject, boolean value) throws InvalidClassException {
		org.influxdb.dto.Point.Builder pointBuilder = (org.influxdb.dto.Point.Builder)metricObject; 		
		pointBuilder.addField("value", (boolean)value);
	}	
}
