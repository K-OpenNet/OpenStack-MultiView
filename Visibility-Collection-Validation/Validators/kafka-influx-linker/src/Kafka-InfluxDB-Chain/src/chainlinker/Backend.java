package chainlinker;

import java.io.InvalidClassException;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public abstract class Backend {

	public Backend() {
		super();
	}
	
	abstract public Object getConfig();
	abstract public void loadConfig(JSONObject config_json) throws ParseException;
	abstract public void processMessage(JSONArray msgValue, HashMap<String, SnapPluginParser> parserMap, LinkedList<SnapPluginParser> parserList);
	abstract public Object parse(JSONObject dataObj, HashMap<String, SnapPluginParser> parserMap, LinkedList<SnapPluginParser> parserList) throws NullPointerException, ClassNotFoundException, InvalidClassException;
	abstract public void addField(
			Object metricObject, 
			String dataTypeName, 
			Object data,
			SnapPluginParser parser
			) throws ClassNotFoundException, InvalidClassException;
	
	protected Object getSafe(JSONObject map, String key) throws NullPointerException {
		Object value = map.get(key);
		if (value == null) throw new NullPointerException();		
		return value;
	}		
}
