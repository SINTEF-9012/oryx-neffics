package de.hpi.bpmn2xpdl;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmappr.Attribute;
import org.xmappr.RootElement;
import org.xmappr.Text;

@RootElement("TriggerResultSignal")
public class XPDLTriggerResultSignal  extends XMLConvertible {

	@Attribute("CatchThrow")
	protected String catchThrow;
	@Text
	protected String signal;
	
	public String getCatchThrow() {
		return catchThrow;
	}
	
	public String getSignal() {
		return signal;
	}
	
	public void readJSONsignalref(JSONObject modelElement) {
		setSignal(modelElement.optString("signalref"));
	}
	
	public void readJSONtriggerresultunknowns(JSONObject modelElement) {
		readUnknowns(modelElement, "triggerresultunknowns");
	}
	
	public void setCatchThrow(String catchThrow) {
		this.catchThrow = catchThrow;
	}
	
	public void setSignal(String signal) {
		this.signal = signal;
	}
	
	public void writeJSONsignalref(JSONObject modelElement) throws JSONException {
		putProperty(modelElement, "signalref", getSignal());
	}
	
	public void writeJSONtriggerresultunknowns(JSONObject modelElement) throws JSONException {
		writeUnknowns(modelElement, "triggerresultunknowns");
	}
	
	protected JSONObject getProperties(JSONObject modelElement) {
		return modelElement.optJSONObject("properties");
	}
	
	protected void initializeProperties(JSONObject modelElement) throws JSONException {
		JSONObject properties = modelElement.optJSONObject("properties");
		if (properties == null) {
			JSONObject newProperties = new JSONObject();
			modelElement.put("properties", newProperties);
			properties = newProperties;
		}
	}
	
	protected void putProperty(JSONObject modelElement, String key, String value) throws JSONException {
		initializeProperties(modelElement);
		
		getProperties(modelElement).put(key, value);
	}
}
