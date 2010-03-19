package de.hpi.bpmn2xpdl;

import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmappr.Xmappr;

public class XPDLTaskReferenceTest extends TestCase {
	
	private String jsonParse = "{" +
		"\"taskref\":\"ABC\"" +
		"}";
	private String jsonWrite = "{" +
		"\"tasktype\":\"Reference\"," +
		"\"taskref\":\"ABC\"" +
		"}";
	private String xpdl = "<TaskReference TaskRef=\"ABC\" />";

	public void testParse() throws JSONException {
		XPDLTaskReference task = new XPDLTaskReference();
		task.parse(new JSONObject(jsonParse));

		StringWriter writer = new StringWriter();

		Xmappr xmappr = new Xmappr(XPDLTaskReference.class);
		xmappr.setPrettyPrint(false);
		xmappr.toXML(task, writer);

		assertEquals(xpdl, writer.toString());
	}

	public void testWrite() {		
		StringReader reader = new StringReader(xpdl);

		Xmappr xmappr = new Xmappr(XPDLTaskReference.class);
		XPDLTaskReference task = (XPDLTaskReference) xmappr.fromXML(reader);

		JSONObject importObject = new JSONObject();
		task.write(importObject);

		assertEquals(jsonWrite, importObject.toString());
	}
}
