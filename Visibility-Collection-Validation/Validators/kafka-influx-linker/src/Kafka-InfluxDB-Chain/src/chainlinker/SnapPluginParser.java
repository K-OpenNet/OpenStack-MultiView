package chainlinker;

import java.util.HashMap;

/*
 * This class serve as a template for all Snap plugin parsers.
 */
public abstract class SnapPluginParser {
	Long lValue = 0L;
	Double lfValue = 0.0;
	@SuppressWarnings("rawtypes")
	Class lClass = lValue.getClass();
	@SuppressWarnings("rawtypes")
	Class lfClass = lfValue.getClass();	
	
	@SuppressWarnings("rawtypes")
	HashMap<String, Class> typeMap = new HashMap<>(); 
	
	public void loadMap(@SuppressWarnings("rawtypes") HashMap<String, Class> map) {
		map.putAll(typeMap);
	}
	
	public void loadParserMap(HashMap<String, SnapPluginParser> map) {
		for (String dataName : typeMap.keySet()) {
			map.put(dataName, this);
		}
	}
	
	public abstract void addField(
			org.influxdb.dto.Point.Builder pointBuilder, 
			String dataTypeName, 
			Object data
			) throws ClassNotFoundException;
	
	public abstract boolean isParsible(String dataTypeName);
}