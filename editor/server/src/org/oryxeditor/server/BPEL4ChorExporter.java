
package org.oryxeditor.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * Copyright (c) 2008-2009 	Zhen Peng
 * 				 2010		Changhua Li
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class BPEL4ChorExporter extends HttpServlet {

	
	private static final long serialVersionUID = 3551528829474652693L;
	
	// use a hash map to record some node informations - with type "participantSet",
	// "associationEdge", "sendOrReceiveActivity", "messageLink" or "process", so that
	// we can easily get all necessary informations about a node
	//
	// in this hash map:
	// key : link ID - type String
	// value : {elementType, nodeElement}  - type Object[]
	private HashMap<String, Object[]> nodeMapForTopology = new HashMap<String, Object[]>();

	// use a hash map to record all link edge informations, so that we can
	// easily get all necessary informations about a link
	//
	// in this hash map:
	// key : link ID - type String
	// value : {senderActivityID, receiverActivityID, messageLinkElement}  
	//         - type Object[]
	private HashMap<String, Object[]> messageLinkMapForTopology = new HashMap<String, Object[]>();

	// use a array list to record all processes with process-paticipantRef relationships
	private ArrayList<String> nonSingleProcessSet = new ArrayList<String>();

	// extension by changhua Li 
	// define a Set to store process name (participant name)  
	private	Set<String> processNameSet = new HashSet<String>();
	
	// use a hash map to record processId 
	private HashMap<String, String> processIdMapForTopology = new HashMap<String, String>();
	
	// use a hash map to record crossPartnerScopes and elementNameList
	private HashMap<String, Set<String>> cps2elNameListMap = new HashMap<String, Set<String>>();
	
	// use a hash map to record elementName and elementId
	private HashMap<String, String> elName2elIdMap = new HashMap<String, String>();
	
	// use a set to store the crossPartnerScopes
	private Set<String> crossPartnerScopeSet = new HashSet<String>();
	
	// use a has map to record process and its children
	private HashMap<String, String>	child2processMap = new HashMap<String, String>();
	// extension end

	/**
     * The POST request.
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
    	
    	res.setContentType("application/json");
    	PrintWriter out = null;
    	try {
    	    out = res.getWriter();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	
    	out.print("{\"res\":[");
    	
    	String rdfString = req.getParameter("data");
    	
    	String contextPath = getServletContext().getRealPath("");
    	
    	transformTopology (rdfString, out, contextPath);

    	transformGrounding (rdfString, out, contextPath);
    	
    	transformProcesses (rdfString, out, contextPath);

    	out.print("]}");

    }
  
	private static String escapeJSON(String json) {
		// escape (some) JSON special characters
		String res = json.replace("\"", "\\\"");
		res = res.replace("\n","\\n");
		res = res.replace("\r","\\r");
		res = res.replace("\t","\\t");
		return res;
	}
	
	/**************************  topology ***********************************/
    private void transformTopology (String rdfString, PrintWriter out, String contextPath){
  	   
    	//System.out.println(rdfString);
	   	// XSLT source
    	final String xsltFilename = contextPath + "/xslt/RDF2BPEL4Chor_Topology.xslt";
//	   	final String xsltFilename = System.getProperty("catalina.home") 
//	   			+ "/webapps/oryx/xslt/RDF2BPEL4Chor_Topology.xslt";
	   	final File xsltFile = new File(xsltFilename);
	   	final Source xsltSource = new StreamSource(xsltFile);	
	   	
	   	// Transformer Factory
	   	final TransformerFactory transformerFactory = 
	   			TransformerFactory.newInstance();
	
	   	// Get the rdf source
	   	final Source rdfSource;
	   	InputStream rdf = new ByteArrayInputStream(rdfString.getBytes());
	   	rdfSource = new StreamSource(rdf);
	 
	   	// Get the result string
	   	String bufferResultString = null;
	   	try {
	   		Transformer transformer = transformerFactory
	   					.newTransformer(xsltSource);
	   		StringWriter writer = new StringWriter();
	   		transformer.transform(rdfSource, new StreamResult(writer));
	   		bufferResultString = writer.toString();
	   		String resultString = postprocessTopology(out, bufferResultString);
	   		printResponse (out, "topology", resultString);
	   	} catch (Exception e){
	   		handleException(out, "topology", e); 
	   	}
	   	
	   	out.print(',');

   }
    
    private String postprocessTopology (PrintWriter out, String oldString) throws Exception{
 	   
    	// initialize
    	nodeMapForTopology.clear();
    	messageLinkMapForTopology.clear();
    	
 	   	StringWriter stringOut = new StringWriter();
 	   	
		// transform string to document
		DocumentBuilderFactory factory = 
			DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream oldResultInputStream = new ByteArrayInputStream
							(oldString.getBytes());
		Document oldDocument = builder.parse(oldResultInputStream);
		
		// rearrange document
		Document newDocument = handleTopologyDocument(oldDocument);
		
		// transform document to string
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		DOMSource source = new DOMSource(newDocument);
		StreamResult result = new StreamResult(stringOut);
		transformer.transform(source, result);
		stringOut.flush();
 
 		return stringOut.toString();
    }
    
    private Document handleTopologyDocument(Document oldDocument) {
    	
		Element topology = getChildElementWithNodeName(oldDocument, 
				"topology", false);
		
		if (topology != null){
			
			// extended by Changhua Li
			// init the mapping and set for crossPartnerScope
			crossPartnerScopeSet.clear();
			cps2elNameListMap.clear();
			elName2elIdMap.clear();
			child2processMap.clear();
			// extended end
			
			// record necessary informations
			NodeList childrenList = topology.getChildNodes();
			for (int i = 0; i < childrenList.getLength(); i++){
				Node child = childrenList.item(i);
				if (child instanceof Element){
					Element childElement = (Element)child;
					
					if (childElement.getNodeName().equals("participants")
						|| childElement.getNodeName().equals("messageLinks")
						|| childElement.getNodeName().equals("nodeInfoSet")
						|| childElement.getNodeName().equals("associationEdgeInfoSet")){
						recordNodesInfo(childElement);
					}
				}
			}
			
			// handle each child elements
			childrenList = topology.getChildNodes();
			for (int i = 0; i < childrenList.getLength(); i++){
				Node child = childrenList.item(i);
				if (child instanceof Element){
					Element childElement = (Element)child;
					
					if (childElement.getNodeName().equals("participantTypes")){
						handleParticipantTypesElement(childElement);
					}
					
					if (childElement.getNodeName().equals("participants")){
						handleParticipantsElement(childElement);
					}
					
					if (childElement.getNodeName().equals("messageLinks")){
						handleMessageLinksElement(childElement);
					}
					
					// extended by Changhua Li
					// handle the crossPartnerScope elements
					if (childElement.getNodeName().equals("crossPartnerScopeInfo")){
						handleCrossPartnerScopeElement(childElement);
					}
					// extended end
				}
			}

			// delete all useless attributes and elements
			cleanUp(topology);
			
			// extension by changhua Li
			// set the xmlns:processes attribute of element topology
			if (!processNameSet.isEmpty()){
				for(String processName : processNameSet){
					topology.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + processName, 
							topology.getAttribute("targetNamespace") + ":" + processName);
				}
			}
			
			// create the <crossPartnerScope> element
			if (cps2elNameListMap != null && crossPartnerScopeSet != null) {
				Iterator<String> it = crossPartnerScopeSet.iterator();
				while (it.hasNext()) {
					String cpsName = (String)it.next();
					Element elCPS = oldDocument.createElement("crossPartnerScope");
					Element elActivities = oldDocument.createElement("activities");
					
					elCPS.setAttribute("name", cpsName);
					if (cps2elNameListMap.get(cpsName).size() > 0) {
						Set<String> valueList =  cps2elNameListMap.get(cpsName);
						Iterator<String> listIt = valueList.iterator();
						while (listIt.hasNext()) {
							String activityName = (String)listIt.next();
							String participantName = "";
							String elId = elName2elIdMap.get(activityName);
							if (child2processMap.containsKey(elId)){
								participantName = child2processMap.get(elId);
							}
							
							// create <activity> element
							Element elActivity = oldDocument.createElement("activity");
							elActivity.setAttribute("name", activityName);
							elActivity.setAttribute("participant", participantName);
							
							elActivities.appendChild(elActivity);
						}
					}
					
					// create <wsp:Policy> element
					Element elPolicy = oldDocument.createElementNS("uri:wsp", "wsp:Policy");
					Element elAll = oldDocument.createElementNS("uri:wsp", "wsp:All");
					Element elPolicyCPS = oldDocument.createElement("crossPartnerScope");
					elPolicyCPS.setAttribute("name", cpsName);
					
					elAll.appendChild(elPolicyCPS);
					elPolicy.appendChild(elAll);
					
					// append the elements on topology
					elCPS.appendChild(elActivities);
					elCPS.appendChild(elPolicy);
					topology.appendChild(elCPS);
				}
			}
			// extension end
		}
		return oldDocument;
	}


	private void recordNodesInfo(Element currentElement) {
		NodeList childrenList = currentElement.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element){
				Element childElement = (Element)child;
				
				String id = childElement.getAttribute("id");
				String name = childElement.getAttribute("name");
				
				if (childElement.getNodeName().equals("participantSet")
					|| childElement.getNodeName().equals("associationEdge")
					|| childElement.getNodeName().equals("sendOrReceiveActivity")
					|| childElement.getNodeName().equals("messageLink")
					|| childElement.getNodeName().equals("process")){
					
					nodeMapForTopology.put(id, new Object[]{childElement.getNodeName(), childElement});
				}
				// extended by Changhua Li
				// to store the process and its children
				if (childElement.getNodeName().equals("process")){
					processIdMapForTopology.put(id, "process");
					if (childElement.hasChildNodes()){
						NodeList childrenListOfProcess = childElement.getChildNodes();
						for (int j = 0; j < childrenListOfProcess.getLength(); j++) {
							Node childOfProcess = childrenListOfProcess.item(j);
							if (childOfProcess instanceof Element){
								Element childElementOfProcess = (Element)childOfProcess;
								String childId = childElementOfProcess.getAttribute("childId");
								child2processMap.put(childId, name);
							}
						}
					}
				}
				// extended end
				
				if (childElement.getNodeName().equals("sendOrReceiveActivity")){
					recordSendActivityOfMessageLink(childElement);
				}
				
				if (childElement.getNodeName().equals("messageLink")){
					recordReceiveActivityOfMessageLink(childElement);
				}
				
				if (childElement.getNodeName().equals("associationEdge")){
					recordTargetOfAssociationEdge(childElement);
				}
			}
		}
	}


	private void recordSendActivityOfMessageLink(Element currentElement) {
		String sendActivityID = currentElement.getAttribute("id");
		
		ArrayList<String> outgoingMessageLinkIDList = getAllOutgoingIDs(currentElement);
		
		Iterator<String> iter = outgoingMessageLinkIDList.iterator();
		while (iter.hasNext()){
			String messageLinkID = iter.next();
			
			String receiveActivityID;
			Element messageLinkElement;
			
			if (messageLinkMapForTopology.containsKey(messageLinkID)){
				receiveActivityID = getReceiveActivityIDInLinkMap(messageLinkID);
				messageLinkElement = getElementInLinkMap(messageLinkID);
			} else {
				receiveActivityID = null;
				messageLinkElement = null;
			}
			
			Object[] newItem = new Object[]{sendActivityID, receiveActivityID, messageLinkElement};
			
			messageLinkMapForTopology.put(messageLinkID, newItem);
		}
	}

	private void recordReceiveActivityOfMessageLink(Element currentElement) {
		String messageLinkID = currentElement.getAttribute("id");
		
		Element targetElement = getChildElementWithNodeName(currentElement, 
				"outgoingLink", false);
		if (targetElement == null) return;
		
		String receiveActivityID = targetElement.getAttribute("targetID");
		if (receiveActivityID == null) return;	
		
		String sendActivityID;
		
		if (messageLinkMapForTopology.containsKey(messageLinkID)){
			sendActivityID = getSendActivityIDInLinkMap(messageLinkID);
		} else {
			sendActivityID = null;
		}
		
		Object[] newItem = new Object[]{sendActivityID, receiveActivityID, currentElement};
		
		messageLinkMapForTopology.put(messageLinkID, newItem);
	}

	private void recordTargetOfAssociationEdge(Element currentElement) {
		
		Element targetElement = getChildElementWithNodeName(currentElement, 
				"outgoingLink", false);
		if (targetElement == null) return;
		
		String targetID = targetElement.getAttribute("targetID");
		if (targetID == null) return;
		
		if (!nonSingleProcessSet.contains(targetID)){
			nonSingleProcessSet.add(targetID);
		}
	}
	
	/**
	 * FIXME: This method is NOT namespace aware. Incoming element has NO namespace information
	 * @param participantTypes
	 */
	private void handleParticipantTypesElement(Element participantTypes) {
		
		ArrayList<String> typeRecorder = new ArrayList<String>();
		ArrayList<Element> uselessChildren = new ArrayList<Element>();
		
		NodeList childrenList = participantTypes.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element &&
					child.getNodeName().equals("participantType")){
				
				Element childElement = (Element)child;
				
				String pbd = childElement
					.getAttribute("participantBehaviorDescription");
				if (pbd == "") {
					uselessChildren.add(childElement);
				} else {
					// record type only if BPD is existed
					String type = childElement.getAttribute("name");
					typeRecorder.add(type);

					String namespace = childElement.getAttribute("processNamespace");
					String prefix = childElement.lookupPrefix(namespace);
					if (prefix == null) {
						//prefix = "ns" + pbd;
						prefix = pbd;
					}
					childElement.setAttribute("participantBehaviorDescription",
							prefix + ":" + pbd);
					childElement.setAttribute("xmlns:" + prefix, namespace);
				}
			}
		}
		
		Element movingChild;
		Iterator<Element> movingList = uselessChildren.iterator();
		while (movingList.hasNext()){
			movingChild = movingList.next();
			participantTypes.removeChild(movingChild);
		}
	}


	private void handleParticipantsElement(Element participants) {
		
		ArrayList<Element> deletingList = new ArrayList<Element>();
		
		// handle single process
		NodeList childrenList = participants.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element &&
					(child.getNodeName().equals("participant"))){
				
				// only process has an "id" attribute here
				String id = ((Element) child).getAttribute("id");
				if (id != null && nonSingleProcessSet.contains(id)){
					deletingList.add((Element)child);	
				}
			}
		}
		
		Iterator<Element> deletingListIter = deletingList.iterator();
		while (deletingListIter.hasNext()){
			Element deletingItem = deletingListIter.next();
			participants.removeChild(deletingItem);
		}
		
		
		
		ArrayList<Element[]> movingList = new ArrayList<Element[]>();
		
		childrenList = participants.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element &&
					(child.getNodeName().equals("participant")
							|| child.getNodeName().equals("participantSet"))){
				
				Element currentElement = (Element)child;
				ArrayList<String> outgoingEdges = getAllOutgoingIDs(currentElement);
				Iterator<String> iter = outgoingEdges.iterator();
				while (iter.hasNext()){
					String edgeID = iter.next();
				
					String connectedTargetID = getTargetIdOfAssociationEdge(edgeID);
					String connectedTargetType = getElementTypeInNodeMap(connectedTargetID);
					
					if (connectedTargetType.equals("participantSet")){
						currentElement.removeAttribute("type");
						Element connectedTarget = getElementInNodeMap(connectedTargetID);				
						
						Element[] newMovingItem = new Element[]{currentElement, connectedTarget};
						movingList.add(newMovingItem);
					}
					
				}
				
			}
		}
		
		Iterator<Element[]> movingListIter = movingList.iterator();
		while (movingListIter.hasNext()){
			Element[] movingItem = movingListIter.next();
			
			Element movingElement = movingItem[0];
			Element parentElement = movingItem[1];
			
			moveElementIntoElement(parentElement, movingElement);
		}
	}

	private void moveElementIntoElement(Element parentElement,
			Element movingElement) {
		// remove element from old parent
		Element oldParent = (Element)movingElement.getParentNode();
		oldParent.removeChild(movingElement);
		
		// append it into new parent
		parentElement.appendChild(movingElement);
	}

	private void handleMessageLinksElement(Element messageLinksElement) {
		
		ArrayList<Element> uselessLinks = new ArrayList<Element>();
	
		// in this hash map:
		// key :  link name - type String
		// value : messageLinkElement - type Element
		HashMap<String, Element> linkNameRecorder = new HashMap<String, Element>();
		linkNameRecorder.clear();
		
		Set<String> messageLinkIDSet = messageLinkMapForTopology.keySet();
		Iterator<String> IDSetIter = messageLinkIDSet.iterator();
		while (IDSetIter.hasNext()){
			String currentMessageLinkID = IDSetIter.next();
			
			Element currentMessageLink = getElementInLinkMap(currentMessageLinkID);
			String name = currentMessageLink.getAttribute("name");
			
			if (!linkNameRecorder.containsKey(name)){
				linkNameRecorder.put(name, currentMessageLink);
			} else {
				uselessLinks.add(currentMessageLink);
			}
			
			String sender =  currentMessageLink.getAttribute("senders");
			String receiver =  currentMessageLink.getAttribute("receivers");
			String sendActivityID = getSendActivityIDInLinkMap(currentMessageLinkID);
			String receiveActivityID = getReceiveActivityIDInLinkMap(currentMessageLinkID);

			
			Element messageLink = linkNameRecorder.get(name);

			// add send- and receiveActivity informations for messageLink
			Element sendElement = getElementInNodeMap(sendActivityID);
			String sendActivity =  sendElement.getAttribute("activityName");
			
			Element receiveElement = getElementInNodeMap(receiveActivityID);
			String receiveActivity =  receiveElement.getAttribute("activityName");
			
			
			String currentSenders = messageLink.getAttribute("senders");
			String currentSendActivities = messageLink.getAttribute("sendActivities");
			String currentReceivers = messageLink.getAttribute("receivers");
			String currentreceiveActivities = messageLink.getAttribute("receiveActivities");

			String newSenders = addItemInString(sender, currentSenders);
			String newSendActivities = addItemInString(sendActivity, currentSendActivities);
			String newReceivers = addItemInString(receiver, currentReceivers);
			String newreceiveActivities = addItemInString(receiveActivity, currentreceiveActivities);
			
			messageLink.setAttribute("senders", newSenders);
			messageLink.setAttribute("sendActivities", newSendActivities);
			messageLink.setAttribute("receivers", newReceivers);
			messageLink.setAttribute("receiveActivities", newreceiveActivities);
		}
		
		Iterator<Element> movingList = uselessLinks.iterator();
		while (movingList.hasNext()){
			Element movingLink = movingList.next();
			messageLinksElement.removeChild(movingLink);
		}
		
		NodeList childrenList = messageLinksElement.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element &&
					child.getNodeName().equals("messageLink")){
				
				Element messageLink = (Element)child;
				
				String currentSenders = messageLink.getAttribute("senders");
				String currentReceivers = messageLink.getAttribute("receivers");
				String currentSendActivities = messageLink.getAttribute("sendActivities");
				String currentreceiveActivities = messageLink.getAttribute("receiveActivities");
				
				if (currentSenders != null && !currentSenders.contains(" ")){
					messageLink.setAttribute("sender", currentSenders);
					messageLink.removeAttribute("senders");
				}
				
				if (currentReceivers != null && !currentReceivers.contains(" ")){
					messageLink.setAttribute("receiver", currentReceivers);
					messageLink.removeAttribute("receivers");
				}
				
				if (currentSendActivities != null && !currentSendActivities.contains(" ")){
					messageLink.setAttribute("sendActivity", currentSendActivities);
					messageLink.removeAttribute("sendActivities");
				}
				
				if (currentreceiveActivities != null && !currentreceiveActivities.contains(" ")){
					messageLink.setAttribute("receiveActivity", currentreceiveActivities);
					messageLink.removeAttribute("receiveActivities");
				}
			}
		}
	}

	private String addItemInString(String newItem, String oldString) {
		if (newItem == null){
			return oldString;
		}
		
		if (oldString.equals("")){
			return newItem;
		}
		
		String[] items = oldString.split(" ");
		for (int i = 0; i < items.length; i++){
			String currentItem = items[i];
			if (currentItem.equals(newItem)){
				return oldString;
			}
		}
		
		String result = oldString + " " + newItem; 
		return result;
	}

	// extended by Changhua Li
	// to handle the crossPartnerScope element
	private void handleCrossPartnerScopeElement(Element currentElement) {
		String nameOfCrossPartnerScope = "";
		String elementName = "";
		String elementId = "";

		if (currentElement.hasAttribute("elementName")){
			elementName = currentElement.getAttribute("elementName");
		}
		
		// create the mapping between cpsName and elementNames
		if (currentElement.hasAttribute("name")){
			nameOfCrossPartnerScope = currentElement.getAttribute("name");
			crossPartnerScopeSet.add(nameOfCrossPartnerScope);
			
			if (cps2elNameListMap.containsKey(nameOfCrossPartnerScope)){
				Set<String> valueElementNameSet = new HashSet<String>();
				valueElementNameSet = cps2elNameListMap.get(nameOfCrossPartnerScope);
				valueElementNameSet.add(elementName);
				cps2elNameListMap.put(nameOfCrossPartnerScope, valueElementNameSet);
			}
			else {
				Set<String> elNameSet = new HashSet<String>();
				elNameSet.add(elementName);
				cps2elNameListMap.put(nameOfCrossPartnerScope, elNameSet);
			}
		}
		
		// create the mapping between elementName and elementId
		if (currentElement.hasAttribute("elementId")){
			elementId = currentElement.getAttribute("elementId");
			elName2elIdMap.put(elementName, elementId);
		}
	}
	// extended end
	
	private void cleanUp(Node currentNode) {

		if (!(currentNode instanceof Element)) {
			return;
		};
		
		if (currentNode instanceof Element){
			((Element)currentNode).removeAttribute("id");
			((Element)currentNode).removeAttribute("processNamespace");	
		}
		
		NodeList childNodes = currentNode.getChildNodes();
		ArrayList<Node> uselessChildren = new ArrayList<Node>();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if (child instanceof Element){
				if (child.getNodeName().equals("outgoingLink")
						|| child.getNodeName().equals("nodeInfoSet")
						|| child.getNodeName().equals("associationEdgeInfoSet")
						|| child.getNodeName().equals("crossPartnerScopeInfo")){
					uselessChildren.add(child);
				} else {
					cleanUp(child);
				}
			}
		}	
		
		Iterator<Node> iter = uselessChildren.iterator();
		Node uselessChild;
		while (iter.hasNext()){
			uselessChild = iter.next();
			currentNode.removeChild(uselessChild);
		}
	}


	private ArrayList<String> getAllOutgoingIDs(Element currentElement) {
		ArrayList<String> result = new ArrayList<String>();
		
		NodeList childrenList = currentElement.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element &&
					child.getNodeName().equals("outgoingLink")){
				
				Element outgoingLink = (Element)child;
				String targetID = outgoingLink.getAttribute("targetID");
				result.add(targetID);
			}
		}
		
		return result;
	}

	
	private Element getChildElementWithNodeName(Node currentNode, 
			String childName, boolean ifNullBuildNewElement) {
		
		NodeList childrenList = currentNode.getChildNodes();
		for (int i = 0; i < childrenList.getLength(); i++){
			Node child = childrenList.item(i);
			if (child instanceof Element && child.getNodeName().equals(childName)){
				return (Element)child;
			}
		}
		
		// if no such child element can be found
		if (ifNullBuildNewElement){
			Element newNode = currentNode.getOwnerDocument()
								.createElement(childName);
			Node firstChild = currentNode.getFirstChild();
			currentNode.insertBefore(newNode, firstChild);
			return newNode;
		} else {
			return null;
		}
	}
	
	private String getTargetIdOfAssociationEdge(String associationEdgeID) {
		Element associationEdge  = getElementInNodeMap(associationEdgeID);
		
		if (associationEdge == null){
			return null;
		}
		
		Element target = getChildElementWithNodeName(associationEdge, 
								"outgoingLink", false);
		
		if (target == null){
			return null;
		} else {
			return target.getAttribute("targetID");
		}
	}

	private String getElementTypeInNodeMap(String nodeID) {
		Object[] infoSet = nodeMapForTopology.get(nodeID);
		return (String)infoSet[0];
	}

	private Element getElementInNodeMap(String nodeID) {
		Object[] infoSet = nodeMapForTopology.get(nodeID);
		return (Element)infoSet[1];
	}

	private String getSendActivityIDInLinkMap(String messageLinkID) {
		Object[] infoSet = messageLinkMapForTopology.get(messageLinkID);
		return (String)infoSet[0];
	}
	
	private String getReceiveActivityIDInLinkMap(String messageLinkID) {
		Object[] infoSet = messageLinkMapForTopology.get(messageLinkID);
		return (String)infoSet[1];
	}
	
	private Element getElementInLinkMap(String messageLinkID) {
		Object[] infoSet = messageLinkMapForTopology.get(messageLinkID);
		return (Element)infoSet[2];
	}
	
	/**************************  grounding *****************************/
    private void transformGrounding (String rdfString, PrintWriter out, String contextPath){
  	   
	   	// XSLT source
    	final String xsltFilename = contextPath + "/xslt/RDF2BPEL4Chor_Grounding.xslt";
//	   	final String xsltFilename = System.getProperty("catalina.home") 
//	   				+ "/webapps/oryx/xslt/RDF2BPEL4Chor_Grounding.xslt";
	   	final File xsltFile = new File(xsltFilename);
	   	final Source xsltSource = new StreamSource(xsltFile);	
	   	
	   	// Transformer Factory
	   	final TransformerFactory transformerFactory = 
	   			TransformerFactory.newInstance();
	
	   	// Get the rdf source
	   	final Source rdfSource;
	   	InputStream rdf = new ByteArrayInputStream(rdfString.getBytes());
	   	rdfSource = new StreamSource(rdf);
	 
	   	// Get the result string
	   	String resultString = null;
	   	try {
	   		Transformer transformer = transformerFactory
	   						.newTransformer(xsltSource);
	   		StringWriter writer = new StringWriter();
	   		transformer.transform(rdfSource, new StreamResult(writer));
	   		resultString = writer.toString();
	   		printResponse (out, "grounding", resultString); 		
	   	} catch (Exception e){
	   		handleException(out, "grounding", e);
	   	}
	   	
	   	out.print(',');
	   		

   }

    /****************************  processes  *******************************/
	private void transformProcesses (String rdfString, PrintWriter out, String contextPath){
	   
    	BPELExporter.transformProcesses (rdfString, out, contextPath);
   }
    
/***************************** print methods ********************************/
   private void printResponse(PrintWriter out, String type, String text){
		out.print("{\"type\":\"" + type + "\",");
		out.print("\"success\":true,");
		out.print("\"content\":\"");
		out.print(escapeJSON(text));
		out.print("\"}");
    }
    
    
//    private void printError(PrintWriter out, String type, String err){
//		out.print("{\"type\":\"" + type+ "\",");
//		out.print("\"success\":false,");
//		out.print("\"content\":\"");
//		out.print(escapeJSON(err));
//		out.print("\"}");
//    }
    
	private void handleException(PrintWriter out, String type, Exception e) {
		e.printStackTrace();
		out.print("{\"type\":\"" + type+ "\",");
		out.print("\"success\":false,");
		out.print("\"content\":\"");
		// TODO: check whether the exception has to be escaped. If yes, the exception should be output using a stringbuffer etc...
		e.printStackTrace(out);
		out.print(escapeJSON(e.getLocalizedMessage()));
		out.print("\"}");
	}
    
}
