package de.hpi.cpn;

import static org.junit.Assert.*;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import de.hpi.cpn.converter.CPNConverter;
import de.hpi.cpn.elements.CPNPage;;

public class ArcRelationsTest 
{	
	@Test
	public void getArcRelation() throws JSONException
	{
		CPNPage testPage = new CPNPage();
		JSONObject tempjson = new JSONObject(String2());
		
		testPage.prepareArcRelations(tempjson);
		
		Hashtable<String, String> sourceTable = testPage.getArcRelation().getSourceTable();
		Hashtable<String, String> targetTable = testPage.getArcRelation().getTargetTable();
		
		JSONObject tempo = tempjson.optJSONArray("childShapes").getJSONObject(0);
		
		String resourceId = tempo.getString("resourceId");
		String outgoingresourceId1 = tempo.optJSONArray("outgoing").getJSONObject(0).getString("resourceId");
		String outgoingresourceId2 = tempo.optJSONArray("outgoing").getJSONObject(1).getString("resourceId");
		
		assertEquals("ResourceId der Stelle: ","oryx_46066F7D-EEBD-4EE7-986B-0EF5AF0E14DA",resourceId);
		assertEquals("erste Outgoing Node", "oryx_FCAEDDE2-84CD-49D6-93B4-9F31D8645C10", outgoingresourceId1);
		assertEquals("zweite Outgoing Node", "oryx_C0DE0E72-25D9-4EAF-AE6A-EDE49BAE9590", outgoingresourceId2);
		
		System.out.println(sourceTable.toString());
		System.out.println(targetTable.toString());
		
		testPage.getArcRelation().changePlaceId("oryx_46066F7D-EEBD-4EE7-986B-0EF5AF0E14DA", "1");
		
		String newvalue = (String) testPage.getArcRelation().getSourceTable().get("oryx_C0DE0E72-25D9-4EAF-AE6A-EDE49BAE9590");
		assertEquals("", "1", newvalue);
		
		newvalue = (String) testPage.getArcRelation().getSourceTable().get("oryx_FCAEDDE2-84CD-49D6-93B4-9F31D8645C10");
		assertEquals("", "1", newvalue);
		
		System.out.println(sourceTable.toString());
		System.out.println(targetTable.toString());

	}
	
	@Test
	public void testMapping() throws JSONException
	{
		String json = String2();	
		
//		CPNConverter trans = new CPNConverter();
		
		System.out.println(CPNConverter.convertToCPNFile(json));
	}
	
	

	private static String String2()
	{
		String string = "{'resourceId':'oryx-canvas123','properties':{'title':'','engine':'false','version':'','author':'','language':'English','creationdate':'10/07/07','modificationdate':'10/07/07','documentation':'','declarations':''},'stencil':{'id':'Diagram'},'childShapes':[{'resourceId':'oryx_46066F7D-EEBD-4EE7-986B-0EF5AF0E14DA','properties':{'id':'','title':'Trans oben','firetype':'Automatic','href':'','omodel':'','oform':'','guard':''},'stencil':{'id':'Transition'},'childShapes':[],'outgoing':[{'resourceId':'oryx_FCAEDDE2-84CD-49D6-93B4-9F31D8645C10'},{'resourceId':'oryx_C0DE0E72-25D9-4EAF-AE6A-EDE49BAE9590'}],'bounds':{'lowerRight':{'x':355,'y':100},'upperLeft':{'x':315,'y':60}},'dockers':[]},{'resourceId':'oryx_60BE0D05-244D-44AD-96A1-73A9B1C7F5AE','properties':{'id':'','title':'1. Stelle','numberoftokens':'0','external':'false','exttype':'Push','href':'','locatornames':'','locatortypes':'','locatorexpr':'','colordefinition':''},'stencil':{'id':'Place'},'childShapes':[],'outgoing':[{'resourceId':'oryx_2799C23C-A34F-4DAB-A0BC-79EA42312861'},{'resourceId':'oryx_5D1EDAC4-9ABB-4E67-876F-5ED3B818B45A'}],'bounds':{'lowerRight':{'x':245,'y':174},'upperLeft':{'x':165,'y':90}},'dockers':[]},{'resourceId':'oryx_C47072FB-DF0B-4E96-A26D-D0E52A8A7951','properties':{'id':'','title':'Trans unten','firetype':'Automatic','href':'','omodel':'','oform':'','guard':''},'stencil':{'id':'Transition'},'childShapes':[],'outgoing':[{'resourceId':'oryx_CE546D9C-0B5D-48D9-8713-C39F20E10B0F'},{'resourceId':'oryx_FD944277-41C7-4ABA-B3B9-45204561A90C'}],'bounds':{'lowerRight':{'x':355,'y':205},'upperLeft':{'x':315,'y':165}},'dockers':[]},{'resourceId':'oryx_2799C23C-A34F-4DAB-A0BC-79EA42312861','properties':{'id':'','label':'','transformation':''},'stencil':{'id':'Arc'},'childShapes':[],'outgoing':[{'resourceId':'oryx_46066F7D-EEBD-4EE7-986B-0EF5AF0E14DA'}],'bounds':{'lowerRight':{'x':314.77464830911475,'y':118.62860932364589},'upperLeft':{'x':238.42847669088525,'y':88.09014067635411}},'dockers':[{'x':40,'y':42},{'x':20,'y':20}],'target':{'resourceId':'oryx_46066F7D-EEBD-4EE7-986B-0EF5AF0E14DA'}},{'resourceId':'oryx_5D1EDAC4-9ABB-4E67-876F-5ED3B818B45A','properties':{'id':'','label':'','transformation':''},'stencil':{'id':'Arc'},'childShapes':[],'outgoing':[{'resourceId':'oryx_C47072FB-DF0B-4E96-A26D-D0E52A8A7951'}],'bounds':{'lowerRight':{'x':314.77712484888116,'y':176.75528936146694},'upperLeft':{'x':237.41037515111884,'y':145.21346063853306}},'dockers':[{'x':40,'y':42},{'x':20,'y':20}],'target':{'resourceId':'oryx_C47072FB-DF0B-4E96-A26D-D0E52A8A7951'}},{'resourceId':'oryx_E4AE2B0F-B59F-4406-848C-4D3C63D8AA11','properties':{'id':'','title':'2. Stelle oben','numberoftokens':'0','external':'false','exttype':'Push','href':'','locatornames':'','locatortypes':'','locatorexpr':'','colordefinition':''},'stencil':{'id':'Place'},'childShapes':[],'outgoing':[],'bounds':{'lowerRight':{'x':468,'y':96},'upperLeft':{'x':436,'y':64}},'dockers':[]},{'resourceId':'oryx_315C05CC-D17C-4FCC-BC82-6F883B27599B','properties':{'id':'','title':'2. Stelle unten','numberoftokens':'0','external':'false','exttype':'Push','href':'','locatornames':'','locatortypes':'','locatorexpr':'','colordefinition':''},'stencil':{'id':'Place'},'childShapes':[],'outgoing':[],'bounds':{'lowerRight':{'x':468,'y':201},'upperLeft':{'x':436,'y':169}},'dockers':[]},{'resourceId':'oryx_CE546D9C-0B5D-48D9-8713-C39F20E10B0F','properties':{'id':'','label':'','transformation':''},'stencil':{'id':'Arc'},'childShapes':[],'outgoing':[{'resourceId':'oryx_315C05CC-D17C-4FCC-BC82-6F883B27599B'}],'bounds':{'lowerRight':{'x':435.4609375,'y':186},'upperLeft':{'x':355.1953125,'y':184}},'dockers':[{'x':20,'y':20},{'x':16,'y':16}],'target':{'resourceId':'oryx_315C05CC-D17C-4FCC-BC82-6F883B27599B'}},{'resourceId':'oryx_FD944277-41C7-4ABA-B3B9-45204561A90C','properties':{'id':'','label':'','transformation':''},'stencil':{'id':'Arc'},'childShapes':[],'outgoing':[{'resourceId':'oryx_E4AE2B0F-B59F-4406-848C-4D3C63D8AA11'}],'bounds':{'lowerRight':{'x':440.28700771630537,'y':166.69537150822273},'upperLeft':{'x':355.39658603369463,'y':90.51165974177725}},'dockers':[{'x':20,'y':20},{'x':16,'y':16}],'target':{'resourceId':'oryx_E4AE2B0F-B59F-4406-848C-4D3C63D8AA11'}},{'resourceId':'oryx_FCAEDDE2-84CD-49D6-93B4-9F31D8645C10','properties':{'id':'','label':'','transformation':''},'stencil':{'id':'Arc'},'childShapes':[],'outgoing':[{'resourceId':'oryx_315C05CC-D17C-4FCC-BC82-6F883B27599B'}],'bounds':{'lowerRight':{'x':439.37294521630537,'y':173.66802775822273},'upperLeft':{'x':355.39658603369463,'y':98.30462849177725}},'dockers':[{'x':20,'y':20},{'x':16,'y':16}],'target':{'resourceId':'oryx_315C05CC-D17C-4FCC-BC82-6F883B27599B'}},{'resourceId':'oryx_C0DE0E72-25D9-4EAF-AE6A-EDE49BAE9590','properties':{'id':'','label':'','transformation':''},'stencil':{'id':'Arc'},'childShapes':[],'outgoing':[{'resourceId':'oryx_E4AE2B0F-B59F-4406-848C-4D3C63D8AA11'}],'bounds':{'lowerRight':{'x':435.4609375,'y':80},'upperLeft':{'x':355.1953125,'y':80}},'dockers':[{'x':20,'y':20},{'x':16,'y':16}],'target':{'resourceId':'oryx_E4AE2B0F-B59F-4406-848C-4D3C63D8AA11'}}],'bounds':{'lowerRight':{'x':1485,'y':1050},'upperLeft':{'x':0,'y':0}},'stencilset':{'url':'/oryx//stencilsets/coloredpetrinets/coloredpetrinet.json','namespace':'http://b3mn.org/stencilset/coloredpetrinet#'},'ssextensions':[]}";
		
		return string;
	}

}
