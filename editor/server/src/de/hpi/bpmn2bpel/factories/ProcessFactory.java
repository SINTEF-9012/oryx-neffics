package de.hpi.bpmn2bpel.factories;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hpi.bpel4chor.model.Swimlane;
import de.hpi.bpel4chor.util.Output;
import de.hpi.bpmn.Activity;
import de.hpi.bpmn.BPMNDiagram;
import de.hpi.bpmn.Container;
import de.hpi.bpmn.Pool;
import de.hpi.bpmn2bpel.model.Container4BPEL;

/**
 * This factory generates the abstract BPEL processes from the processes
 * contained in pools and pool sets. 
 * 
 * <p>An instance of this class can only be used for one diagram.</p>
 */
public class ProcessFactory {
	
	/* The default target namespace of the BPEL process */
	static final String targetNamespace = "http://sesastra.com/goldeneye/";
	
	BPMNDiagram diagram = null;
	
	/**
	 * Constructor. Initializes the process factory.
	 * 
	 * @param diagram The diagram that contains the processes to be generated.
	 */
	public ProcessFactory(BPMNDiagram diagram) {
		this.diagram = diagram;
	}
	
	/**
	 * Sets the attributes for the given process element. The attribute
	 * values are taken from the swimlane, the process is contained in.
	 * 
	 * @param processElement The process element to add the attributes to.
	 * @param pool       The {@link Pool} to take the attribute values from.
	 */
	private void setProcessAttributes(Element processElement, Pool pool) {
		String name = pool.getLabel().trim();
		
		processElement.setAttribute("name", name);
		processElement.setAttribute("targetNamespace", targetNamespace);
		processElement.setAttribute("id", "process");
		
//		if (pool.getProcess().getQueryLanguage() != null) {
//			processElement.setAttribute("queryLanguage", 
//					pool.getProcess().getQueryLanguage().toASCIIString());
//		} else if (this.diagram.getQueryLanguage() != null) {
//			processElement.setAttribute("queryLanguage", 
//					this.diagram.getQueryLanguage().toASCIIString());
//		}
//		
//		if (pool.getProcess().getExpressionLanguage() != null) {
//			processElement.setAttribute("expressionLanguage", 
//					pool.getProcess().getExpressionLanguage().toASCIIString());
//		} else if (this.diagram.getExpressionLanguage() != null) {
//			processElement.setAttribute("expressionLanguage", 
//					this.diagram.getExpressionLanguage().toASCIIString());
//		}
		
//		processElement.setAttribute("suppressJoinFailure", 
//				BPELUtil.booleanToYesNo(pool.getProcess().isSuppressJoinFailure()));
//		processElement.setAttribute("exitOnStandardFault", 
//				BPELUtil.booleanToYesNo(pool.getProcess().isExitOnStandardFault()));
		
		processElement.setAttribute("suppressJoinFailure", "yes");
		
		processElement.setAttribute("xmlns", "http://docs.oasis-open.org/wsbpel/2.0/process/executable");
//		processElement.setAttribute("abstractProcessProfile", "urn:HPI_IAAS:choreography:profile:2006/12");
//		processElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
//		processElement.setAttribute("xmlns:wsu","http://schemas.xmlsoap.org/ws/2002/07/utility/");
		processElement.setAttribute("xmlns:tns", targetNamespace);
				
//		if (pool.getImports() != null) {
//			for (Iterator<Import> it = pool.getImports().iterator(); it.hasNext();) {
//				Import imp = it.next();
//				processElement.setAttribute("xmlns:" + imp.getPrefix(), imp.getNamespace());
//			}
//		}
		
//		processElement.setAttribute("xsi:schemaLocation", 
//				"http://docs.oasis-open.org/wsbpel/2.0/process/abstract "+
//				"http://docs.oasis-open.org/wsbpel/2.0/OS/process/abstract/ws-bpel_abstract_common_base.xsd");
		
	}
	
	/**
	 * For expressing a fault handler on process level an additional scope has
	 * to be modeled the fault event is attached to. This scope can be removed
	 * under certain circumstances and the fault handler will be generated as
	 * fault handler of the process. The following requirements have to be
	 * fulfilled to transform the fault handlers of the scope as fault handler
	 * of the process.
	 * <ul>
	 * <li> Process activities contain only one start event
	 * <li> process activities contain only one end event
	 * <li> Process activities contain only one scope activity
	 * <li> process activities contain only one gateway activity (OR, XOR-Merge)
	 * <li> The scope is not a looping activity
	 * <li> The scope has attached fault events
	 * <li> The scope has no other event s attached
	 * <li> The scope is not isolated
	 * <li> The transitions from the start event leads to the scope
	 * <li> The transition from the exclusive or inclusive gateway after 
	 *      the scope and fault handler leads to the end event.
	 * </ul>
	 * 
	 * @param process The process that may contain a scope that was only
	 * modeled for expression process fault handlers.
	 * 
	 * @return The scope that was modeled for expressing the process
	 * fault handler. The result is null if such a scope does not exist. 
	 */
//	private Scope modeledScopeForHandlers(Process process) {
//		List<StartEvent> startEvents = process.getStartEvents();
//		List<EndEvent> endEvents = process.getEndEvents();
//		List<Scope> scopes = process.getScopes();
//		List<Gateway> gateways = process.getGateways();
//		
//		if ((startEvents.size() == 1) && (endEvents.size() == 1) && 
//				(scopes.size() == 1) && (gateways.size() == 1)) {
//			StartEvent startEvent = startEvents.get(0);
//			EndEvent endEvent = endEvents.get(0);
//			Gateway gateway = gateways.get(0);
//			Scope scope = scopes.get(0);
//			List<IntermediateEvent> faultEvents = 
//				scope.getAttachedEvents(IntermediateEvent.TRIGGER_ERROR);
//			
//			// gateway has to be exclusive or inclusive
//			if (!gateway.getGatewayType().equals(Gateway.TYPE_XOR) &&
//					!gateway.getGatewayType().equals(Gateway.TYPE_OR)) {
//				return null;
//			}
//			
//			if ((scope.getIsolated() != null) && scope.getIsolated().equals("yes")) {
//				return null;
//			}
//			
//			if (scope.getLoop() != null) {
//				return null;
//			}
//			
//			if (faultEvents.isEmpty()) {
//				return null;
//			}
//			
//			if ((!scope.getAttachedEvents(IntermediateEvent.TRIGGER_COMPENSATION).isEmpty()) ||
//					(!scope.getAttachedEvents(IntermediateEvent.TRIGGER_TERMINATION).isEmpty())) {
//				return null;
//			}
//			
//			// relation from start to scope
//			if (!startEvent.getSourceFor().isEmpty()) {
//				Transition trans = startEvent.getSourceFor().get(0);
//				if (!trans.getTarget().equals(scope)) {
//					return null;
//				}
//			}
//			
//			// relation from scope to gateway
//			if (!scope.getSourceFor().isEmpty()) {
//				Transition trans = scope.getSourceFor().get(0);
//				if (!trans.getTarget().equals(gateway)) {
//					return null;
//				}
//			}
//			
//			// fault events connected with fault handler
//			for (Iterator<IntermediateEvent> it = faultEvents.iterator(); it.hasNext();) {
//				Handler handler = it.next().getConnectedHandler();
//				if (handler == null || 
//						!handler.getHandlerType().equals(Handler.TYPE_FAULT) || 
//						handler.getSourceFor().isEmpty()) {
//					return null;
//				}
//				// relation from fault handlers to gateway
//				Transition trans = handler.getSourceFor().get(0);
//				if (!trans.getTarget().equals(gateway)) {
//					return null;
//				}
//			}
//			
//			// relation from gateway to end event
//			if (!gateway.getSourceFor().isEmpty()) {
//				Transition trans = gateway.getSourceFor().get(0);
//				if (!trans.getTarget().equals(endEvent)) {
//					return null;
//				}
//			}
//			
//			return scope;
//		}
//		return null;
//	}
	
	/**
	 * If a scope was modeled only for expressing a process fault handler
	 * (see {@link #modeledScopeForHandlers(Process)}) the elements
	 * contained in the scope should be moved to the process. This is done
	 * in this method. For this purpose the elements that were already present
	 * in the process will be removed
	 *  
	 * @param process The process to copy the elements to.
	 * @param scope   The scope to copy the elements from.
	 */
//	private void moveElementsToProcess(Process process, Scope scope) {
//		// remove start and end event from process
//		List<StartEvent> startEvents = process.getStartEvents();
//		process.getActivities().removeAll(startEvents);
//		
//		List<EndEvent> endEvents = process.getEndEvents();
//		process.getActivities().removeAll(endEvents);
//		
//		// remove transitions from process
//		process.getTransitions().clear();
//		
//		// remove all Activities except event handlers from process
//		List<Activity> toRemove = new ArrayList<Activity>();
//		for (Iterator<Activity> it = process.getActivities().iterator(); it.hasNext();) {
//			Activity act = it.next();
//			if (act instanceof Handler) {
//				Handler handler = (Handler)act;
//				if (handler.getHandlerType().equals(Handler.TYPE_MESSAGE) || 
//						(handler.getHandlerType().equals(Handler.TYPE_TIMER))) {
//					continue;
//				}
//			}
//			toRemove.add(act);
//		}
//		process.removeActivities(toRemove);
//		
//		// add scope activities to process
//		process.getActivities().addAll(scope.getSubProcess().getActivities());
//		
//		// add scope transitions to process
//		process.getTransitions().addAll(scope.getSubProcess().getTransitions());
//		
//		// remove scope from process
//		process.getActivities().remove(scope);
//	}
	
	/**
	 * Creates a BPEL process element. For this purpose the process
	 * attributes and child elements are created
	 * (see {@link SupportingFactory#createImportElements(Swimlane)},
	 * {@link SupportingFactory#createMessageExchangesElement(Process)},
	 * {@link SupportingFactory#createVariablesElement(Swimlane, model.Container)},
	 * {@link SupportingFactory#createCorrelationSetsElement(Process)},
	 * {@link StructuredElementsFactory#createFaultHandlersElement(Activity)},
	 * {@link StructuredElementsFactory#createEventHandlersElement(model.Container)})
	 * 
	 * <p>In addition to this the sequence flow of the process is transformed
	 * using the {@link SequenceFlowFactory}.<p>
	 * 
	 * @param document The document to create the process elements in.
	 * @param swimlane The swimlane containing the process.
	 * @param output   The Output to print errors to.
	 * 
	 * @return The created process element.
	 */
	private Element createProcessElement(Document document, Pool pool, Output output) {
		Element processElement = document.createElement("process");
		setProcessAttributes(processElement, pool);
		
		Container container = pool.getProcess();
		Container4BPEL process = null;
		if (container instanceof Container4BPEL) {
			process = (Container4BPEL) container;
		} else {
			output.addError(
					"de.hpi.bpmn.Pool.getProcess() returned an unexpected type.",
					""
			);
		}
		
		SupportingFactory supportingFactory = 
			new SupportingFactory(this.diagram, document, output, processElement);
		
		// imports
		List<Element> importElements = 
			supportingFactory.createImportElements(process);
		for (Element importElement : importElements) {
			processElement.appendChild(importElement);
		}
		
//		// messageExchanges
//		Element messageExchangesElement = 
//			supportingFactory.createMessageExchangesElement(process);
//		if (messageExchangesElement != null) {
//			processElement.appendChild(messageExchangesElement);
//		}
		
		/* Create partner links of the BPEL process */
		Element partnerLinksElement = supportingFactory.
										createPartnerLinksElement(process);
		if (partnerLinksElement != null) {
			processElement.appendChild(partnerLinksElement);
		}
		
		/* Create variables of the BPEL process */
		Element variablesElement = 
			supportingFactory.createVariablesElement(null, process);
		if (variablesElement != null) {
			processElement.appendChild(variablesElement);
		}
		
//		// correlations
//		Element correlationsElement = 
//			supportingFactory.createCorrelationSetsElement(process);
//		if (correlationsElement != null) {
//			processElement.appendChild(correlationsElement);
//		}
//		
//		// fault handlers of process must be modeled with additional scope
//		Scope scope = modeledScopeForHandlers(process);
//		if (scope != null) {
//			
//			Element element = new StructuredElementsFactory(
//					this.diagram, document, output).createFaultHandlersElement(scope);
//			if (element != null) {
//				processElement.appendChild(element);
//			}
//			moveElementsToProcess(process, scope);
//		}
//		
//		// event handlers
//		Element eventHandlersElement = 
//			new StructuredElementsFactory(this.diagram, document, output).
//				createEventHandlersElement(process);
//		if (eventHandlersElement != null) {
//			processElement.appendChild(eventHandlersElement);
//		}
		
		// sequence flow
		Element sequenceFlow = 
			new SequenceFlowFactory(this.diagram, document, process, output, processElement)
				.transformSequenceFlow();
		if (sequenceFlow != null) {
			processElement.appendChild(sequenceFlow);
		}
		
		return processElement;
	}
	
	/**
	 * Transforms the process contained in the given {@link Pool} to a BPEL
	 * abstract process.
	 * 
	 * @param pool The {@pool} containing the process to be created.
	 * @param output   The Output to print errors to.
	 * 
	 * @return The created process. The result is null if a 
	 * {@link ParserConfigurationException} occurred.
	 */
	public Document transformProcess(Pool pool, Output output) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setValidating(false);
		
		DocumentBuilder builder;
		try {
			builder = documentBuilderFactory.newDocumentBuilder();
			
			Document process = builder.newDocument();
			
			Element processElement = createProcessElement(process, pool, output);
			
			process.appendChild(processElement);
			return process;
		} catch (ParserConfigurationException e) {
			output.addError(e);
		}
		return null;
	}
}
