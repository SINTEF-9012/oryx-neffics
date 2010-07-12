package de.hpi.ibpmn.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.hpi.bpmn.ANDGateway;
import de.hpi.bpmn.Activity;
import de.hpi.bpmn.Container;
import de.hpi.bpmn.DataObject;
import de.hpi.bpmn.Edge;
import de.hpi.bpmn.EndPlainEvent;
import de.hpi.bpmn.Event;
import de.hpi.bpmn.Gateway;
import de.hpi.bpmn.IntermediateEvent;
import de.hpi.bpmn.IntermediateTimerEvent;
import de.hpi.bpmn.MessageFlow;
import de.hpi.bpmn.SequenceFlow;
import de.hpi.bpmn.StartPlainEvent;
import de.hpi.bpmn.StartTimerEvent;
import de.hpi.bpmn.SubProcess;
import de.hpi.bpmn.TextAnnotation;
import de.hpi.bpmn.XORDataBasedGateway;
import de.hpi.bpmn.XOREventBasedGateway;
import de.hpi.bpmn.Activity.LoopType;
import de.hpi.ibpmn.ComplexInteraction;
import de.hpi.ibpmn.IBPMNDiagram;
import de.hpi.ibpmn.IBPMNFactory;
import de.hpi.ibpmn.Interaction;
import de.hpi.ibpmn.IntermediateInteraction;
import de.hpi.ibpmn.OwnedNode;
import de.hpi.ibpmn.Pool;
import de.hpi.ibpmn.StartInteraction;

/**
 * main method: loadBPMN()
 * 
 * remark: bidirectional associations are interpreted as two separate associations
 * 
 * TODO handle decision ownership properly
 * 
 * @author gero.decker
 *
 */
public class IBPMNRDFImporter {
	
	protected Document doc;
	protected IBPMNFactory factory;
	
	protected class ImportContext {
		IBPMNDiagram diagram;
		Map<String,de.hpi.bpmn.DiagramObject> objects = new HashMap<String, de.hpi.bpmn.DiagramObject>(); // key = resource id, value = diagram object
		Map<String,de.hpi.bpmn.DiagramObject> connections = new HashMap<String, de.hpi.bpmn.DiagramObject>(); // key = to resource id, value = from node
		Map<de.hpi.bpmn.Node,String> parentRelationships = new HashMap<de.hpi.bpmn.Node, String>(); // key = child node, value = parent resource id
		Map<de.hpi.bpmn.DiagramObject,String[]> owningRolesMap = new HashMap<de.hpi.bpmn.DiagramObject, String[]>(); // key = node, value = list of roles
	}
	
	public IBPMNRDFImporter(Document doc) {
		this.doc = doc;
	}
	
	public IBPMNDiagram loadIBPMN() {
		Node root = getRootNode(doc);
		if (root == null) return null;
		
		factory = new IBPMNFactory(); // for the moment: assume plain BPMN
		
		ImportContext c = new ImportContext();
		c.diagram = factory.createIBPMNDiagram();
		
		List<Node> edges = new ArrayList<Node>();
		
		// handle nodes
		for (Node node=root.getFirstChild(); node != null; node=node.getNextSibling()) {
			if (node instanceof Text) continue;
			
			String type = getType(node);
			if (type == null) continue;
			
			if (type.equals("IBPMNDiagram")) {
				handleDiagram(node, c);
			} else if (type.equals("Pool")) {
				addPool(node, c, false);
			} else if (type.equals("PoolSet")) { 
				addPool(node, c, true);
			} else if (type.equals("ComplexInteraction")) {
				addComplexInteraction(node, c);
			} else if (type.equals("ComplexInteractionCollapsed")) {
				addComplexInteraction(node, c);
				
			} else if (type.equals("StartEvent")) {
				addStartPlainEvent(node, c);
			} else if (type.equals("StartInteraction")) {
				addStartInteraction(node, c);
			} else if (type.equals("StartTimerEvent")) {
				addStartTimerEvent(node, c);
//			} else if (type.equals("StartRuleEvent")) {
//				addStartRuleEvent(node, c);
//			} else if (type.equals("StartLinkEvent")) {
//				addStartLinkEvent(node, c);
//			} else if (type.equals("StartMultipleEvent")) {
//				addStartMultipleEvent(node, c);
				
//			} else if (type.equals("IntermediateEvent")) {
//				addIntermediatePlainEvent(node, c);
			} else if (type.equals("IntermediateInteraction")) {
				addIntermediateInteraction(node, c);
//			} else if (type.equals("IntermediateErrorEvent")) {
//				addIntermediateErrorEvent(node, c);
			} else if (type.equals("IntermediateTimerEvent")) {
				addIntermediateTimerEvent(node, c);
//			} else if (type.equals("IntermediateCancelEvent")) {
//				addIntermediateCancelEvent(node, c);
//			} else if (type.equals("IntermediateCompensationEvent")) {
//				addIntermediateCompensationEvent(node, c);
//			} else if (type.equals("IntermediateRuleEvent")) {
//				addIntermediateRuleEvent(node, c);
//			} else if (type.equals("IntermediateLinkEvent")) {
//				addIntermediateLinkEvent(node, c);
//			} else if (type.equals("IntermediateMultipleEvent")) {
//				addIntermediateMultipleEvent(node, c);
				
			} else if (type.equals("EndEvent")) {
				addEndPlainEvent(node, c);
//			} else if (type.equals("EndCancelEvent")) {
//				addEndCancelEvent(node, c);
//			} else if (type.equals("EndLinkEvent")) {
//				addEndLinkEvent(node, c);
				
			} else if (type.equals("Exclusive_Databased_Gateway")) {
				addXORDataBasedGateway(node, c);
			} else if (type.equals("Exclusive_Eventbased_Gateway")) {
				addXOREventBasedGateway(node, c);
			} else if (type.equals("AND_Gateway")) {
				addANDGateway(node, c);
//			} else if (type.equals("Complex_Gateway")) {
//				addComplexGateway(node, c);
//			} else if (type.equals("OR_Gateway")) {
//				addORGateway(node, c);
				
//			} else if (type.equals("DataObject")) {
//				addDataObject(node, c);
//			} else if (type.equals("TextAnnotation")) {
//				addTextAnnotation(node, c);
				
			} else if (type.equals("SequenceFlow")) {
				edges.add(node);
			} else if (type.equals("MessageFlowFrom")) {
				edges.add(node);
			} else if (type.equals("MessageFlowTo")) {
				edges.add(node);
//			} else if (type.equals("Association_Unidirectional")) {
//				edges.add(node);
//			} else if (type.equals("Association_Bidirectional")) {
//				edges.add(node);
//			} else if (type.equals("Association_Undirected")) {
//				edges.add(node);
			}
		}
		
		// handle edges (except undirected associations)
		for (Node node: edges) {
			String type = getType(node);
			if (type.equals("SequenceFlow")) {
				addSequenceFlow(node, c);
			} else if (type.equals("MessageFlowFrom")) {
				addMessageFlowFrom(node, c);
			} else if (type.equals("MessageFlowTo")) {
				addMessageFlowTo(node, c);
//			} else if (type.equals("Association_Unidirectional")) {
//				addAssociation(node, c);
//			} else if (type.equals("Association_Bidirectional")) {
//				addBidirectionalAssociation(node, c);
			}
		}
		
		// handle undirected associations
//		for (Node node: edges) {
//			String type = getType(node);
//			if (type.equals("Association_Undirected")) {
//				addUndirectedAssociation(node, c);
//			}
//		}
		
		// handle decision / timer ownership
		for (Entry<de.hpi.bpmn.DiagramObject,String[]> entry: c.owningRolesMap.entrySet()) {
			if (entry.getKey() instanceof OwnedNode) {
				OwnedNode n = (OwnedNode)entry.getKey();
				for (String roleName: entry.getValue()) {
					Pool p = findPool(roleName, c);
					if (p != null)
						n.getOwners().add(p);
				}
			}
		}
		
		// handle intermediate events
		attachIntermediateEvents(c);
		
		// handle parent relationships
		setupParentRelationships(c);
		
		c.diagram.identifyProcesses();
		return c.diagram;
	}
	
	protected Pool findPool(String roleName, ImportContext c) {
		for (de.hpi.bpmn.Node n: c.diagram.getChildNodes()) {
			if (n instanceof Pool && n.getLabel().equals(roleName))
				return (Pool)n;
		}
		return null;
	}

	protected void attachIntermediateEvents(ImportContext c) {
		for (de.hpi.bpmn.Node node: c.diagram.getChildNodes()) {
			if (node instanceof IntermediateEvent) {
				de.hpi.bpmn.DiagramObject source = c.connections.get(node.getResourceId());
				if (source instanceof Activity)
					((IntermediateEvent)node).setActivity((Activity)source);
			}
		}
	}

	protected void setupParentRelationships(ImportContext c) {
		for (Entry<de.hpi.bpmn.Node,String> entry: c.parentRelationships.entrySet()) {
			de.hpi.bpmn.Node child = entry.getKey();
			de.hpi.bpmn.DiagramObject parent = c.objects.get(entry.getValue());
			if (parent instanceof Container)
				child.setParent((Container)parent);
		}
	}

	protected void handleDiagram(Node node, ImportContext c) {
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			// TODO: add further attributes...
			if (attribute.equals("title")) {
				c.diagram.setTitle(getContent(n));
//			} else {
//				handleStandardAttributes(attribute, n, pool, c, "Name");
			}
		}
	}

	protected void addPool(Node node, ImportContext c, boolean isSet) {
		Pool pool = factory.createPool();
		pool.setResourceId(getResourceId(node));
		pool.setParent(c.diagram);
		pool.setSet(isSet);
		c.objects.put(pool.getResourceId(), pool);
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			// TODO: add further attributes...
			if (attribute.equals("poolid")) {
				pool.setId(getContent(n));
			} else {
				handleStandardAttributes(attribute, n, pool, c, "name");
			}
		}
		if (pool.getId() == null)
			pool.setId(pool.getResourceId());
	}

	protected boolean handleStandardAttributes(String attribute, Node n, de.hpi.bpmn.DiagramObject node, ImportContext c, String label) {
		if (attribute.equals("id")) {
			node.setId(getContent(n));
		} else if (attribute.equals("outgoing")) {
			c.connections.put(getResourceId(getAttributeValue(n, "rdf:resource")), node);
		} else if (attribute.equals("owningroles") || attribute.equals("controllingroles")) {
			String roles = getContent(n);
			if (roles != null)
				c.owningRolesMap.put(node, roles.split(","));
		} else if (node instanceof de.hpi.bpmn.Node) {
			if (attribute.equals("parent")) {
				c.parentRelationships.put((de.hpi.bpmn.Node)node, getResourceId(getAttributeValue(n, "rdf:resource")));
			} else if (attribute.equals(label)) {
				((de.hpi.bpmn.Node)node).setLabel(getContent(n));
			}
		} else {
			return false;
		}
		return true;
	}

	protected void handleStandardActivityAttributes(String attribute, Node n, Activity activity, ImportContext c, String label) {
		if (attribute.equals("looptype")) {
			String looptypeValue = getContent(n);
			if (looptypeValue != null && looptypeValue.equals("Standard")) {
				activity.setLoopType(LoopType.Standard);
			} else if (looptypeValue != null && looptypeValue.equals("MultiInstance")) {
				activity.setLoopType(LoopType.Multiinstance);
			}
		} else if (attribute.equals("loopcondition")) {
			String loopconditionValue = getContent(n);
			if (loopconditionValue != null) {
				activity.setLoopCondition(loopconditionValue);
			}
		} else {
			handleStandardAttributes(attribute, n, activity, c, label);
		}
	}

	protected void addComplexInteraction(Node node, ImportContext c) {
		SubProcess sp = factory.createSubProcess();
		sp.setResourceId(getResourceId(node));
		sp.setParent(c.diagram);
		c.objects.put(sp.getResourceId(), sp);
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			// TODO: add further attributes...
//			if (attribute.equals("isadhoc")) {
//				sp.setAdhoc(getContent(n).equals("true"));
//			} else {
				handleStandardActivityAttributes(attribute, n, sp, c, "name");
//			}
		}
		if (sp.getId() == null)
			sp.setId(sp.getResourceId());
	}

	protected void addStartPlainEvent(Node node, ImportContext c) {
		StartPlainEvent event = factory.createStartPlainEvent();
		handleEvent(node, event, c, "documentation");
	}

	protected void addStartInteraction(Node node, ImportContext c) {
		StartInteraction event = factory.createStartInteraction();
		handleEvent(node, event, c, "message");
	}

	protected void addStartTimerEvent(Node node, ImportContext c) {
		StartTimerEvent event = factory.createStartTimerEvent();
		handleEvent(node, event, c, "timeDate");
	}

//	protected void addStartRuleEvent(Node node, ImportContext c) {
//		StartConditionalEvent event = factory.createStartConditionalEvent();
//		handleEvent(node, event, c, "ruleName");
//	}

//	protected void addStartLinkEvent(Node node, ImportContext c) {
//		StartLinkEvent event = factory.createStartLinkEvent();
//		handleEvent(node, event, c, "linkId");
//	}

//	protected void addStartMultipleEvent(Node node, ImportContext c) {
//		StartMultipleEvent event = factory.createStartMultipleEvent();
//		handleEvent(node, event, c, "triggers");
//	}

//	protected void addIntermediatePlainEvent(Node node, ImportContext c) {
//		IntermediatePlainEvent event = factory.createIntermediatePlainEvent();
//		handleEvent(node, event, c, "documentation");
//	}

	protected void addIntermediateTimerEvent(Node node, ImportContext c) {
		IntermediateTimerEvent event = factory.createIntermediateTimerEvent();
		handleEvent(node, event, c, "timeCycle");
	}

//	protected void addIntermediateCancelEvent(Node node, ImportContext c) {
//		IntermediateCancelEvent event = factory.createIntermediateCancelEvent();
//		handleEvent(node, event, c, "target");
//	}

//	protected void addIntermediateCompensationEvent(Node node, ImportContext c) {
//		IntermediateCompensationEvent event = factory.createIntermediateCompensationEvent();
//		handleEvent(node, event, c, "target");
//	}

//	protected void addIntermediateRuleEvent(Node node, ImportContext c) {
//		IntermediateConditionalEvent event = factory.createIntermediateConditionalEvent();
//		handleEvent(node, event, c, "ruleName");
//	}

//	protected void addIntermediateLinkEvent(Node node, ImportContext c) {
//		IntermediateLinkEvent event = factory.createIntermediateLinkEvent();
//		handleEvent(node, event, c, "linkId");
//	}

//	protected void addIntermediateMultipleEvent(Node node, ImportContext c) {
//		IntermediateMultipleEvent event = factory.createIntermediateMultipleEvent();
//		handleEvent(node, event, c, "triggers");
//	}

	protected void addIntermediateInteraction(Node node, ImportContext c) {
		IntermediateInteraction event = factory.createIntermediateInteraction();
		handleEvent(node, event, c, "message");
	}

//	protected void addIntermediateErrorEvent(Node node, ImportContext c) {
//		IntermediateErrorEvent event = factory.createIntermediateErrorEvent();
//		handleEvent(node, event, c, "errorCode");
//	}

	protected void addEndPlainEvent(Node node, ImportContext c) {
		EndPlainEvent event = factory.createEndPlainEvent();
		handleEvent(node, event, c, "documentation");
	}
	
//	protected void addEndCancelEvent(Node node, ImportContext c) {
//		EndCancelEvent event = factory.createEndCancelEvent();
//		handleEvent(node, event, c, "documentation");
//	}

//	protected void addEndErrorEvent(Node node, ImportContext c) {
//		EndErrorEvent event = factory.createEndErrorEvent();
//		handleEvent(node, event, c, "errorCode");
//	}

//	protected void addEndCompensationEvent(Node node, ImportContext c) {
//		EndCompensationEvent event = factory.createEndCompensationEvent();
//		handleEvent(node, event, c, "activity");
//	}

//	protected void addEndTerminateEvent(Node node, ImportContext c) {
//		EndTerminateEvent event = factory.createEndTerminateEvent();
//		handleEvent(node, event, c, "documentation");
//	}

//	protected void addEndLinkEvent(Node node, ImportContext c) {
//		EndLinkEvent event = factory.createEndLinkEvent();
//		handleEvent(node, event, c, "linkId");
//	}

//	protected void addEndMultipleEvent(Node node, ImportContext c) {
//		EndMultipleEvent event = factory.createEndMultipleEvent();
//		handleEvent(node, event, c, "results");
//	}

	protected void handleEvent(Node node, Event event, ImportContext c, String label) {
		event.setResourceId(getResourceId(node));
		event.setParent(c.diagram);
		c.objects.put(event.getResourceId(), event);
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			handleStandardAttributes(attribute, n, event, c, label);
		}
		if (event.getId() == null)
			event.setId(event.getResourceId());
	}

	protected void addXORDataBasedGateway(Node node, ImportContext c) {
		XORDataBasedGateway gateway = factory.createXORDataBasedGateway();
		handleGateway(node, gateway, c);
	}

	protected void addXOREventBasedGateway(Node node, ImportContext c) {
		XOREventBasedGateway gateway = factory.createXOREventBasedGateway();
		handleGateway(node, gateway, c);
	}

	protected void addANDGateway(Node node, ImportContext c) {
		ANDGateway gateway = factory.createANDGateway();
		handleGateway(node, gateway, c);
	}

//	protected void addComplexGateway(Node node, ImportContext c) {
//		ComplexGateway gateway = factory.createComplexGateway();
//		handleGateway(node, gateway, c);
//	}

//	protected void addORGateway(Node node, ImportContext c) {
//		ORGateway gateway = factory.createORGateway();
//		handleGateway(node, gateway, c);
//	}

	protected void handleGateway(Node node, Gateway gateway, ImportContext c) {
		gateway.setResourceId(getResourceId(node));
		gateway.setParent(c.diagram);
		c.objects.put(gateway.getResourceId(), gateway);
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			handleStandardAttributes(attribute, n, gateway, c, "Documentation");
		}
		if (gateway.getId() == null)
			gateway.setId(gateway.getResourceId());
	}

	protected void addDataObject(Node node, ImportContext c) {
		DataObject obj = factory.createDataObject();
		obj.setResourceId(getResourceId(node));
		c.diagram.getDataObjects().add(obj);
		c.objects.put(obj.getResourceId(), obj);
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			// TODO: add further attributes...
			if (attribute.equals("state")) {
				obj.setState(getContent(n));
			} else {
				handleStandardAttributes(attribute, n, obj, c, "name");
			}
		}
		if (obj.getId() == null)
			obj.setId(obj.getResourceId());
	}

	protected void addTextAnnotation(Node node, ImportContext c) {
		TextAnnotation ta = factory.createTextAnnotation();
		ta.setResourceId(getResourceId(node));
		ta.setParent(c.diagram);
		c.objects.put(ta.getResourceId(), ta);
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			// TODO: add further attributes...
//			if (attribute.equals("state")) {
//				obj.setState(getContent(n));
//			} else {
				handleStandardAttributes(attribute, n, ta, c, "text");
//			}
		}
		if (ta.getId() == null)
			ta.setId(ta.getResourceId());
	}

	protected void addSequenceFlow(Node node, ImportContext c) {
		SequenceFlow flow = factory.createSequenceFlow();
		c.diagram.getEdges().add(flow);
		if (node.hasChildNodes()) {
			Node n = node.getFirstChild();
			while ((n = n.getNextSibling()) != null) {
				if (n instanceof Text)
					continue;
				String attribute = n.getNodeName().substring(
						n.getNodeName().indexOf(':') + 1);

				if (attribute.equals("conditiontype")) {
					String ctype = getContent(n);
					if (ctype.equals("Expression"))
						flow.setConditionType(SequenceFlow.ConditionType.EXPRESSION);
					else if (ctype.equals("Default"))
						flow.setConditionType(SequenceFlow.ConditionType.DEFAULT);
				} else if (attribute.equals("conditionexpression")) {
					String expression = getContent(n);
					if (expression != null)
						flow.setConditionExpression(expression);
				} else if (attribute.equals("name")) {
					String name = getContent(n);
					if (name != null)
						flow.setName(name);
				}
			}
		}
		setConnections(flow, node, c);
	}

	protected void addMessageFlowFrom(Node node, ImportContext c) {
		Edge temp = new MessageFlow(); // only created temporarily - will not appear in diagram
		setConnections(temp, node, c);
		
		if (temp.getTarget() instanceof Interaction) {
			Interaction i = (Interaction)temp.getTarget();
			if (i != null)
				i.setSenderRole((Pool)temp.getSource());
		} else if (temp.getTarget() instanceof ComplexInteraction) {
			((ComplexInteraction)temp.getTarget()).getOwners().add((Pool)temp.getSource());
		}
		
		temp.setSource(null); // clean-up
		temp.setTarget(null); // clean-up
	}

	protected void addMessageFlowTo(Node node, ImportContext c) {
		Edge temp = new MessageFlow(); // only created temporarily - will not appear in diagram
		setConnections(temp, node, c);
		
		Interaction i = (Interaction)temp.getSource();
		if (i != null)
			i.setReceiverRole((Pool)temp.getTarget());
		temp.setSource(null); // clean-up
		temp.setTarget(null); // clean-up
	}

//	protected void addAssociation(Node node, ImportContext c) {
//		Association ass = factory.createAssociation();
//		c.diagram.getEdges().add(ass);
//		setConnections(ass, node, c);
//	}

	// introduce 2 associations
//	protected void addBidirectionalAssociation(Node node, ImportContext c) {
//		Association ass = factory.createAssociation();
//		c.diagram.getEdges().add(ass);
//		setConnections(ass, node, c);
//		
//		Association ass2 = factory.createAssociation();
//		c.diagram.getEdges().add(ass);
//		ass2.setId(ass.getId());
//		ass2.setResourceId(ass.getResourceId());
//		ass2.setSource(ass.getTarget());
//		ass2.setTarget(ass.getSource());
//	}

//	protected void addUndirectedAssociation(Node node, ImportContext c) {
//		UndirectedAssociation ass = factory.createUndirectedAssociation();
//		c.diagram.getEdges().add(ass);
//		setConnections(ass, node, c);
//	}

	protected void setConnections(Edge edge, Node node, ImportContext c) {
		edge.setResourceId(getResourceId(node));
		c.objects.put(edge.getResourceId(), edge);
		edge.setSource(c.connections.get(edge.getResourceId()));
		
		for (Node n=node.getFirstChild(); n != null; n=n.getNextSibling()) {
			if (n instanceof Text) continue;
			String attribute = n.getNodeName().substring(n.getNodeName().indexOf(':')+1);
			
			if (attribute.equals("id")) {
				edge.setId(getContent(n));
			} else if (attribute.equals("outgoing")) {
				if (edge.getTarget() == null)
					edge.setTarget(c.objects.get(getResourceId(getAttributeValue(n, "rdf:resource"))));
				else
					c.connections.put(getResourceId(getAttributeValue(n, "rdf:resource")), edge);
			}
		}
		if (edge.getId() == null)
			edge.setId(edge.getResourceId());
	}

	

	protected String getContent(Node node) {
		if (node != null && node.hasChildNodes())
			return node.getFirstChild().getNodeValue();
		return null;
	}
	
	private String getAttributeValue(Node node, String attribute) {
		Node item = node.getAttributes().getNamedItem(attribute);
		if (item != null)
			return item.getNodeValue();
		else
			return null;
	}

	protected String getType(Node node) {
		String type = getContent(getChild(node, "type"));
		if (type != null)
			return type.substring(type.indexOf('#')+1);
		else
			return null;
	}
	
	protected String getResourceId(Node node) {
		Node item = node.getAttributes().getNamedItem("rdf:about");
		if (item != null)
			return getResourceId(item.getNodeValue());
		else
			return null;
	}
	
	protected String getResourceId(String id) {
		return id.substring(id.indexOf('#')+1);
	}

	protected Node getChild(Node n, String name) {
		if (n == null)
			return null;
		for (Node node=n.getFirstChild(); node != null; node=node.getNextSibling())
			if (node.getNodeName().indexOf(name) >= 0) 
				return node;
		return null;
	}

	protected Node getRootNode(Document doc) {
		Node node = doc.getDocumentElement();
		if (node == null || !node.getNodeName().equals("rdf:RDF"))
			return null;
		return node;
	}

}
