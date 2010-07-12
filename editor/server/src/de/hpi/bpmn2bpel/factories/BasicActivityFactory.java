package de.hpi.bpmn2bpel.factories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hpi.bpel4chor.model.activities.AssignTask;
import de.hpi.bpel4chor.model.activities.EmptyTask;
import de.hpi.bpel4chor.model.activities.Event;
import de.hpi.bpel4chor.model.activities.IntermediateEvent;
import de.hpi.bpel4chor.model.activities.NoneTask;
import de.hpi.bpel4chor.model.activities.ReceiveTask;
import de.hpi.bpel4chor.model.activities.SendTask;
import de.hpi.bpel4chor.model.activities.ServiceTask;
import de.hpi.bpel4chor.model.activities.Trigger;
import de.hpi.bpel4chor.model.activities.TriggerResultMessage;
import de.hpi.bpel4chor.model.activities.ValidateTask;
import de.hpi.bpel4chor.model.artifacts.VariableDataObject;
import de.hpi.bpel4chor.model.connections.MessageFlow;
import de.hpi.bpel4chor.util.Output;
import de.hpi.bpmn.BPMNDiagram;
import de.hpi.bpmn.DataObject;
import de.hpi.bpmn.StartEvent;
import de.hpi.bpmn.StartMessageEvent;
import de.hpi.bpmn.Task;
import de.hpi.bpmn2bpel.model.Container4BPEL;
import de.hpi.bpmn2bpel.model.supporting.Copy;
import de.hpi.bpmn2bpel.model.supporting.FromSpec;
import de.hpi.bpmn2bpel.model.supporting.ToSpec;
import de.hpi.bpmn2bpel.model.supporting.FromSpec.fromTypes;
import de.hpi.bpmn2bpel.model.supporting.ToSpec.toTypes;
import de.hpi.bpmn2bpel.util.BPELUtil;

/**
 * This factory transforms the basic BPEL4Chor activities assign, compensate, 
 * empty, invoke, opaque, receive, reply, throw, validate and wait from the diagram. 
 * Each instance of this factory can only be used for one diagram.
 */
public class BasicActivityFactory {
	
	private BPMNDiagram diagram = null;
	private Document document = null;
	private SupportingFactory supportingFactory = null;
	private StructuredElementsFactory structuredElementsFactory = null;
	private Output output;
	
	/**
	 * Constructor. Initializes the factory with the diagram and the 
	 * target document, the generated BPEL elements will be contained in. 
	 * 
	 * @param diagram  The diagram the activities are modeled in
	 * @param document The target document for the generated BPEL4Chor elements
	 * @param output   The Output to print errors to. 
	 */
	public BasicActivityFactory(BPMNDiagram diagram, Document document, Output output) {
		this.diagram = diagram;
		this.document = document;
		this.output = output;
		this.supportingFactory = new SupportingFactory(diagram, document, this.output);
		this.structuredElementsFactory = 
			new StructuredElementsFactory(diagram, document, this.output);
	}
	
	/**
	 * Constructor. Initializes the factory with the diagram and the 
	 * target document, the generated BPEL elements will be contained in. Also 
	 * passes the BPEL process element because of namespace issues.
	 * 
	 * @param diagram  The diagram the activities are modeled in
	 * @param document The target document for the generated BPEL4Chor elements
	 * @param output   The Output to print errors to.
	 * @param processElement
	 * 		The BPEL process XML element
	 */
	public BasicActivityFactory(BPMNDiagram diagram, Document document, Output output, Element processElement) {
		this.diagram = diagram;
		this.document = document;
		this.output = output;
		this.supportingFactory = new SupportingFactory(diagram, document, this.output, processElement);
		this.structuredElementsFactory = 
			new StructuredElementsFactory(diagram, document, this.output);
	}
	
	/**
	 * <p>Creates the BPEL4Chor "Copy" element from a Copy object.</p>
	 * 
	 * The attributes keepSrcElementName and ignoreMissingFromData 
	 * are taken from the Copy object. The fromSpec and toSpec elements
	 * are generated using the appropriate methods
	 * {@link SupportingFactory#createFromSpecElement(model.supporting.FromSpec)} and
	 * {@link SupportingFactory#createToSpecElement(model.supporting.ToSpec)}.
	 * 
	 * @param copy The copy object to create the element from
	 * @param task The assign task to generate the copy element for.
	 * 
	 * @return the generated BPEL4Chor "copy" element.
	 */
	private Element createCopyElement(Copy copy/*, AssignTask task*/) {
		Element result = this.document.createElement("copy");
		
		if (copy.isKeepSrcElementName() != null) {
			result.setAttribute("keepSrcElementName", 
					copy.isKeepSrcElementName());
		}
		
		if (copy.isIgnoreMissingFromData() != null) {
			result.setAttribute("ignoreMissingFromData", 
				copy.isIgnoreMissingFromData());
		}
		
		Element fromSpec = 
			this.supportingFactory.createFromSpecElement(copy.getFromSpec());
		if (fromSpec != null) {
			result.appendChild(fromSpec);
		} else {
//			this.output.addError("The assign task has a copy without a specified from spec element.", task.getId());
		}
		
		Element toSpec = 
			this.supportingFactory.createToSpecElement(copy.getToSpec());
		if (toSpec != null) {
			result.appendChild(toSpec);
		} else {
//			this.output.addError("The assign task has a copy without a specified to spec element.", task.getId());
		}
		
		return result;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "receive" element from an Event object.</p>
	 * 
	 * <p>The standard attributes are taken from the event. 
	 * The createInstance attribute is taken from the parameter.
	 * The messageExchange, correlation and fromParts attributes and elements 
	 * are taken from the trigger </p>
	 * 
	 * <p>
	 * The correlation and fromParts elements
	 * are generated using the appropriate methods
	 * {@link SupportingFactory#createCorrelationsElement(List)} and
	 * {@link SupportingFactory#createFromPartsElement(List)}.
	 * </p>
	 * 
	 * <p>
	 * The variable attribute will only be generated, 
	 * if no fromParts element was specified and if the trigger does not define
	 * an opaque variable. The variable attribute is determined from a standard
	 * variable data object, that is associated with the event. 
	 * </p>
	 * 
	 * @param event			 The event, to generate the receive element from. 
	 * @param eventTrigger   The trigger of the event (should be a TriggerResultMessage) 
	 * @param createInstance true, if the receive element has a createInstance attribute 
	 * set to "yes", false otherwise.
	 * 
	 * @return The generated BPEL4Chor "receive" element
	 */
	private Element createReceiveElement(Event event, 
			Trigger eventTrigger, boolean createInstance) {
		Element result = this.document.createElement("receive");
		
//		BPELUtil.setStandardAttributes(result, event);
		
		if (createInstance)
			result.setAttribute("createInstance", BPELUtil.booleanToYesNo(createInstance));
		
		// must be determined here, because there may be no trigger defined
		VariableDataObject variable = 
			this.diagram.getStandardVariable(event, true);
		
		if ((eventTrigger != null) && 
				(eventTrigger instanceof TriggerResultMessage)) {
			TriggerResultMessage trigger = (TriggerResultMessage)eventTrigger;
			
			if (trigger.getMessageExchange() != null) {
				result.setAttribute("messageExchange", trigger.getMessageExchange());
			}
			
			Element correlations = 
				this.supportingFactory.createCorrelationsElement(
						trigger.getCorrelations());
			if (correlations != null) {
				result.appendChild(correlations);
			}
			
			if(trigger.isOpaqueOutput()) {
				result.setAttribute("variable", "##opaque");
				// the output variable is opaque so omit from parts and variable data objects
				variable = null;
			} else if (variable == null) {
				// create from part only if no variable data object found
				Element fromParts = 
					this.supportingFactory.createFromPartsElement(
							trigger.getFromParts());
				if (fromParts != null) {
					result.appendChild(fromParts);
				} else {
					this.output.addError("The message event must define an output variable.", event.getId());
				}
			} 
		}
		
		if (variable != null) {
			result.setAttribute("variable", variable.getName());
		}
		
		return result;
	}
	
	
	/**
	 * <p>Generates a BPEL4Chor "receive" element from a start event.</p>
	 * 
	 * <p>For more detail see the documentation of 
	 * {@link #createReceiveElement(Event, Trigger, boolean)}</p>
	 * 
	 * @param event			 the start event to generate the receive element from
	 * @param createInstance true, if the receive element has a createInstance attribute 
	 * set to "yes", false otherwise.
	 * 
	 * @return  The generated BPEL4Chor "receive" element.
	 * 			The result is null if the trigger of the event is not a message trigger. 
	 */
	public Element createReceiveElement(StartEvent event, boolean createInstance) {
		//if (!event.getTriggerType().equals(IntermediateEvent.TRIGGER_MESSAGE)) {
		if (!(event instanceof StartMessageEvent)) {
			return null;
		}
		
		Element result = this.document.createElement("receive");
		
		/* Set necessary attributes to provide and start the process */
		result.setAttribute("partnerLink", "InvokeProcessPartnerLink");
		result.setAttribute("serviceName", "InvokeProcess_" 
								+ this.diagram.getId().replace(" ", "_"));
		result.setAttribute("operation", "process");
		result.setAttribute("portType", "tns:" + "InvokeProcess");
		result.setAttribute("variable", "input");
		
		BPELUtil.setStandardAttributes(result, event);
		
		if (createInstance)
			result.setAttribute("createInstance", BPELUtil.booleanToYesNo(createInstance));
		
		// must be determined here, because there may be no trigger defined
//		VariableDataObject variable = 
//			this.diagram.getStandardVariable(event, true);
		VariableDataObject variable = null;
		
//		if ((eventTrigger != null) && 
//				(eventTrigger instanceof TriggerResultMessage)) {
//			TriggerResultMessage trigger = (TriggerResultMessage)eventTrigger;
//			
//			if (trigger.getMessageExchange() != null) {
//				result.setAttribute("messageExchange", trigger.getMessageExchange());
//			}
//			
//			Element correlations = 
//				this.supportingFactory.createCorrelationsElement(
//						trigger.getCorrelations());
//			if (correlations != null) {
//				result.appendChild(correlations);
//			}
//			
//			if(trigger.isOpaqueOutput()) {
//				result.setAttribute("variable", "##opaque");
//				// the output variable is opaque so omit from parts and variable data objects
//				variable = null;
//			} else if (variable == null) {
//				// create from part only if no variable data object found
//				Element fromParts = 
//					this.supportingFactory.createFromPartsElement(
//							trigger.getFromParts());
//				if (fromParts != null) {
//					result.appendChild(fromParts);
//				} else {
//					this.output.addError("The message event must define an output variable.", event.getId());
//				}
//			} 
//		}
		
		if (variable != null) {
			result.setAttribute("variable", variable.getName());
		}
		
		return result;
		
		//return createReceiveElement(event, event.getTrigger(), createInstance);
		
	}
	
	/**
	 * <p>Generates a BPEL4Chor "receive" element from an intermediate event.</p>
	 * 
	 * <p>For more detail see the documentation of 
	 * {@link #createReceiveElement(Event, Trigger, boolean)}</p>
	 * 
	 * @param event			 the intermediate event to generate the receive element from
	 * 
	 * @return 	The generated BPEL4Chor "receive" element.
	 * 			The result is null if the trigger of the event is not a message trigger. 
	 */
	public Element createReceiveElement(IntermediateEvent event) {
		if (!event.getTriggerType().equals(IntermediateEvent.TRIGGER_MESSAGE)) {
			return null;
		}

		return createReceiveElement(event, event.getTrigger(), event.getCreateInstance());
	}
	
	/**
	 * <p>Creates the BPEL4Chor "receive" element from an receive task.</p>
	 * 
	 * <p>The standard, messageExchange, correlation and fromParts attributes 
	 * and elements are taken from the task. 
	 * There will be no createInstance attribute generated (only for receiving 
	 * start events).</p>
	 * 
	 * <p>
	 * The correlation and fromParts elements
	 * are generated using the appropriate methods
	 * {@link SupportingFactory#createCorrelationsElement(List)} and
	 * {@link SupportingFactory#createFromPartsElement(List)}.
	 * </p>
	 * 
	 * <p>
	 * The variable attribute will only be generated, 
	 * if no fromParts element was specified and if the task does not define
	 * an opaque variable. The variable attribute is determined from a standard
	 * variable data object, that is associated with the task. 
	 * </p>
	 * 
	 * @param task	The task, to generate the receive element from. 
	 * 
	 * @return 		The generated BPEL4Chor "receive" element
	 */
	public Element createReceiveElement(ReceiveTask task) {
		Element result = this.document.createElement("receive");
		
//		BPELUtil.setStandardAttributes(result, task);
		
		if (task.getMessageExchange() != null) {
			result.setAttribute("messageExchange", task.getMessageExchange());
		}
		
		result.setAttribute("createInstance", 
				BPELUtil.booleanToYesNo(task.isInstantiate()));
		
		Element correlations = 
			this.supportingFactory.createCorrelationsElement(
					task.getCorrelations());
		if (correlations != null) {
			result.appendChild(correlations);
		}
		
		Element fromParts = 
			this.supportingFactory.createFromPartsElement(task.getFromParts());
		VariableDataObject object = 
			this.diagram.getStandardVariable(task, true);
		
		if(task.isOpaqueOutput()) {
			result.setAttribute("variable", "##opaque");
		} else if (object != null) {
			result.setAttribute("variable", object.getName());
		} else if (fromParts != null) {
			result.appendChild(fromParts);
		} else {
			this.output.addError("The receive task must define an output variable.", task.getId());
		}
		
//		return createScopeForAttachedHandlers(result, task);
		return null;
	}
	
	/**
	 * Creates a raw BPEL invoke element from a {@link Task} in the first 
	 * implentation.
	 * 
	 * @param task
	 * 		The {@link Task} object representing the invoke
	 * @return
	 * 		The BPEL invoke element
	 */
	public Element createInvokeElement(Task task) {
		Element invoke = this.document.createElement("invoke");
		
		BPELUtil.setStandardAttributes(invoke, task);

		/* Set invoke element's attributes */
		invoke.setAttribute("group", task.getColor());
		invoke.setAttribute("serviceName", task.getServiceName());
		invoke.setAttribute("partnerLink", task.getServiceName() + "PartnerLink");
		invoke.setAttribute("operation", task.getOperation());
		invoke.setAttribute("inputVariable", task.getId());
		invoke.setAttribute("outputVariable", task.getId() + "Response");
		invoke.setAttribute("portType", 
				this.supportingFactory.getAndSetPrefixForNamespaceURI(
						task.getNamespace()) 
						+ ":" 
						+ task.getPortType());

		return invoke;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "invoke" element from an invoke task.</p>
	 * 
	 * <p>The standard, correlation and fromParts and toParts attributes 
	 * and elements are taken from the task.</p>
	 * 
	 * <p>
	 * The correlation, fromParts and toParts elements
	 * are generated using the appropriate methods
	 * {@link SupportingFactory#createCorrelationsElement(List)}, 
	 * {@link SupportingFactory#createFromPartsElement(List)} and 
	 * {@link SupportingFactory#createToPartsElement(List)}.
	 * </p>
	 * 
	 * <p>
	 * The inputVariable and outputVariable attribute will only be generated, 
	 * if no fromParts element was specified and if the task does not define
	 * an opaque input or output variable. 
	 * The variable attributes are determined from standard
	 * variable data objects, that are associated with the task. 
	 * </p>
	 * 
	 * <p>
	 * Fault and compensation Handlers that are present for the invoke task
	 * will be generated using the appropriate methods
	 * {@link StructuredElementsFactory#createFaultHandlerElements(model.activities.Activity)} and  
	 * {@link StructuredElementsFactory#createCompensationHandlerElement(model.activities.Activity)}. 
	 * </p>
	 * 
	 * @param task	The task, to generate the invoke element from. 
	 * 
	 * @return 		The generated BPEL4Chor "invoke" element
	 */
	public Element createInvokeElement(ServiceTask task) {
		Element invoke = this.document.createElement("invoke");
		
//		BPELUtil.setStandardAttributes(invoke, task);
		
		Element correlations = 
			this.supportingFactory.createCorrelationsElement(
					task.getCorrelations());
		if (correlations != null) {
			invoke.appendChild(correlations);
		}
		
		// create fault and compensation handlers		
		List<Element> faultHandlers = 
			this.structuredElementsFactory.createFaultHandlerElements(task);
		for (Iterator<Element> it = faultHandlers.iterator(); it.hasNext();) {
			invoke.appendChild(it.next());
		}
		
		Element compensationHandler = 
			this.structuredElementsFactory.createCompensationHandlerElement(task);
		if (compensationHandler != null) {
			invoke.appendChild(compensationHandler);
		}
		
		Element toParts = 
			this.supportingFactory.createToPartsElement(task.getToParts());
		VariableDataObject object = 
			this.diagram.getStandardVariable(task, false);
		if(task.isOpaqueInput()) {
			invoke.setAttribute("inputVariable", "##opaque");
		} else if (object != null) {
			invoke.setAttribute("inputVariable", object.getName());
		} else if (toParts != null) {
				invoke.appendChild(toParts);
		} else {
			this.output.addError("The service task must define an input variable.", task.getId());
		}
		
		Element fromParts = 
			this.supportingFactory.createFromPartsElement(task.getFromParts());
		object = this.diagram.getStandardVariable(task, true);
		if(task.isOpaqueOutput()) {
			invoke.setAttribute("outputVariable", "##opaque");
		} else if (object != null) {
			invoke.setAttribute("outputVariable", object.getName());
		} else if (fromParts != null) {
			invoke.appendChild(fromParts);
		} else {
			this.output.addError("The service task must define an output variable.", task.getId());
		}
		
		Element terminationHandler = 
			this.structuredElementsFactory.createTerminationHandlerElement(task);
		if (terminationHandler != null) {
			Element scope = this.document.createElement("scope");
			scope.appendChild(terminationHandler);
			scope.appendChild(invoke);
			return scope;
		}
		return invoke;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "invoke" element from a send task.</p>
	 * 
	 * <p>The standard, correlation and fromParts attributes 
	 * and elements are taken from the task.</p>
	 * 
	 * <p>
	 * The correlation and fromParts elements
	 * are generated using the appropriate methods
	 * {@link SupportingFactory#createCorrelationsElement(List)} and 
	 * {@link SupportingFactory#createFromPartsElement(List)}.
	 * </p>
	 * 
	 * <p>
	 * The inputVariable attribute will only be generated, 
	 * if no fromParts element was specified and if the task does not define
	 * an opaque input variable. 
	 * The variable attributes are determined from standard
	 * variable data objects, that are associated with the task. 
	 * </p>
	 * 
	 * <p>
	 * Fault and compensation Handlers that are present for the invoke task
	 * will be generated using the appropriate methods
	 * {@link StructuredElementsFactory#createFaultHandlerElements(model.activities.Activity)} and  
	 * {@link StructuredElementsFactory#createCompensationHandlerElement(model.activities.Activity)}. 
	 * </p>
	 * 
	 * @param task	The task, to generate the invoke element from. 
	 * 
	 * @return 		The generated BPEL4Chor "invoke" element
	 */
	public Element createInvokeElement(SendTask task) {
		Element invoke = this.document.createElement("invoke");
		
//		BPELUtil.setStandardAttributes(invoke, task);
		
		Element correlations = 
			this.supportingFactory.createCorrelationsElement(
					task.getCorrelations());
		if (correlations != null) {
			invoke.appendChild(correlations);
		}
		
		Element compensationHandler = 
			this.structuredElementsFactory.createCompensationHandlerElement(task);
		if (compensationHandler != null) {
			invoke.appendChild(compensationHandler);
		}
		
		// create fault termination handlers but they must be located in an additional scope 
		List<Element> faultHandlers = 
			this.structuredElementsFactory.createFaultHandlerElements(task);
		Element terminationHandler = 
			this.structuredElementsFactory.createTerminationHandlerElement(task);
		if (faultHandlers.size() > 0 || (terminationHandler != null)) {
			Element scope = this.document.createElement("scope");
			
			if (faultHandlers.size() > 0) {
				Element faultHandlersElement = 
					this.document.createElement("faultHandlers");
				for (Iterator<Element> it = faultHandlers.iterator(); it.hasNext();) {
					faultHandlersElement.appendChild(it.next());
				}
				scope.appendChild(faultHandlersElement);
			}
			
			if (terminationHandler != null) {
				scope.appendChild(terminationHandler);
			}
			scope.appendChild(invoke);
			return scope;
		}
		
		Element toParts = 
			this.supportingFactory.createToPartsElement(task.getToParts());
		VariableDataObject object = 
			this.diagram.getStandardVariable(task, false);
		if(task.isOpaqueInput()) {
			invoke.setAttribute("inputVariable", "##opaque");
		} else if (object != null) {
			invoke.setAttribute("inputVariable", object.getName());
		} else if (toParts != null) {
			invoke.appendChild(toParts);
		} else {
			this.output.addError("The send task must define an input variable.", task.getId());
		}
		
		return invoke;
	}
	
	/**
	 * Checks if all message flows emanating from the service task 
	 * lead to the swimlane the given send task is located in.
     *
	 * @param task    The send task to determine the target swimlane.
	 * @param service The service task to check the message flows for.
	 * 
	 * @return True, if all message flow lead to the swimlane of
	 * the send task. False otherwise.
	 */
	private boolean isReplyServiceTask(SendTask task, ServiceTask service) {
		List<MessageFlow> flows = 
			this.diagram.getMessageFlowsWithSource(service.getId());
		for (Iterator<MessageFlow> it = flows.iterator(); it.hasNext();) {
			MessageFlow flow = it.next();
			if (flow.getTarget().getParentSwimlane().getId() != 
				task.getParentSwimlane().getId()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Creates an invoke or a reply element for the given send task.
	 * 
	 * If the sending task sends a message to a service task that has sent a
	 * message to swimlane of the send task before, a reply element will be
	 * created (see {@link #createReplyElement(SendTask)}). 
	 * Otherwise an invoke element is created 
	 * (see {@link #createInvokeElement(SendTask)}).
	 * 
	 * @param task The send task to create an invoke or reply element for
	 * 
	 * @return The created invoke or reply element.
	 */
	public Element createSendingElement(SendTask task) {
		List<MessageFlow> flows = this.diagram.getMessageFlowsWithSource(task.getId());
		// all flows must lead to a service task to create a reply element from the send task
		boolean reply = false;
		for (Iterator<MessageFlow> it = flows.iterator(); it.hasNext();) {
			MessageFlow flow = it.next();
			if ((flow.getTarget() != null) && (flow.getTarget() instanceof ServiceTask)) {
				if (isReplyServiceTask(task, (ServiceTask)flow.getTarget())) {
					reply = true;
				}
			} 
		}
		
		if (reply) {
			return createReplyElement(task);
		}
		return createInvokeElement(task);
	}
	
//	/**
//	 * Creates an scope element containing the given content. Moreover,
//	 * fault handlers, a compensation handler and a termination handler
//	 * is created in this scope from handlers modeled for the given task.
//	 * 
//	 * If the task does not define any handlers, there is no need to 
//	 * create an additional scope. In this case the given content
//	 * is returned.
//	 * 
//	 * @param content The content enclosed by the scope.
//	 * @param task    The task that provides the handlers to be created
//	 *                in the scope.
//	 *                
//	 * @return The created scope enclosing the created handlers and the given
//	 * content. If a scope is not necessary, the given content is returned.
//	 */
//	private Element createScopeForAttachedHandlers(Element content, Task task) {
////		// create fault, compensation and termination handlers but they must 
////		// be located in an additional scope 
////		List<Element> faultHandlers = this.structuredElementsFactory.
////			createFaultHandlerElements(task);
////		Element compensationHandler = this.structuredElementsFactory.
////			createCompensationHandlerElement(task);
////		Element terminationHandler = 
////			this.structuredElementsFactory.createTerminationHandlerElement(task);
////		
////		if ((faultHandlers.size() > 0) || (compensationHandler != null) || 
////				(terminationHandler != null)) {
////			
////			Element scope = this.document.createElement("scope");
////			scope.setAttribute("name", BPELUtil.generateScopeName(task));
////			if (faultHandlers.size() > 0) {
////				Element faultHandlersElement = 
////					this.document.createElement("faultHandlers");
////				for (Iterator<Element> it = faultHandlers.iterator(); it.hasNext();) {
////					faultHandlersElement.appendChild(it.next());
////				}
////				scope.appendChild(faultHandlersElement);
////			}
////			
////			if (compensationHandler != null) {
////				scope.appendChild(compensationHandler);
////			}
////			
////			if (terminationHandler != null) {
////				scope.appendChild(terminationHandler);
////			}
////			scope.appendChild(content);
////			return scope;
////		}
////		return content;
//		return null;
//	}
	
	/**
	 * <p>Creates the BPEL4Chor "reply" element from an reply task.</p>
	 * 
	 * <p>The standard, messageExchange, faultName, correlation and ToParts
	 * attributes and elements are taken from the task.</p>
	 * 
	 * <p>
	 * The correlation and toParts elements
	 * are generated using the appropriate methods
	 * {@link SupportingFactory#createCorrelationsElement(List)} and 
	 * {@link SupportingFactory#createToPartsElement(List)}.
	 * </p>
	 * 
	 * <p>
	 * The variable attribute will only be generated, 
	 * if no toParts element was specified and if the task does not define
	 * an opaque variable. 
	 * The variable attributes are determined from standard
	 * variable data objects, that are associated with the task. 
	 * </p>
	 * 
	 * @param task	The task, to generate the reply element from. 
	 * 
	 * @return 		The generated BPEL4Chor "reply" element
	 */
	private Element createReplyElement(SendTask task) {
		Element reply = this.document.createElement("reply");
		
//		BPELUtil.setStandardAttributes(reply, task);
		
		if (task.getMessageExchange() != null) {
			reply.setAttribute("messageExchange", task.getMessageExchange());
		}
		
		if (task.getFaultName() != null) {
			reply.setAttribute("faultName", task.getFaultName());
		}
		
		Element correlations = 
			this.supportingFactory.createCorrelationsElement(
					task.getCorrelations());
		if (correlations != null) {
			reply.appendChild(correlations);
		}
		
		Element toParts = 
			this.supportingFactory.createToPartsElement(task.getToParts());
		VariableDataObject object = 
			this.diagram.getStandardVariable(task, false);
		if(task.isOpaqueInput()) {
			reply.setAttribute("variable", "##opaque");
		} else if (object != null) {
			reply.setAttribute("variable", object.getName());
		} else if (toParts != null) {
			reply.appendChild(toParts);
		} else {
			this.output.addError("The send task must define an input variable.", task.getId());
		}
		
//		return createScopeForAttachedHandlers(reply, task);
		return null;
	}
	
	/**
	 * Creates an assign task that copies token and reporting service URL into 
	 * the soap header of each web service method message. Both information are 
	 * necessary for the GoldenEye project.
	 *  
	 * @param process
	 * 		The business process
	 * @return
	 * 		The assign task element
	 */
	public Element createHeaderAssignElementForProcessInputRedirect(Container4BPEL process) {
		Element assign = this.document.createElement("assign");
		
		for (Task task : BPELUtil.getDistinctServiceList(process.getTasks())) {
			/* Copy token */
			Copy copy = prepareCopyForHeaderMetaData(task.getId(), "token");
			Element copyElement = createCopyElement(copy);
			assign.appendChild(copyElement);
			
			/* Copy reporting service URL */
			copy = prepareCopyForHeaderMetaData(task.getId(), "reportingService");
			copyElement = createCopyElement(copy);
			assign.appendChild(copyElement);
		}
		
		return assign;
	}
	
	
	/**
	 * Creates a copy object to copy token and reporting service URL to the 
	 * appropriate variable.
	 * 
	 * @param variableName
	 * 		The name of the target variable
	 * @param property
	 * 		The part of the header to copy
	 * @return
	 * 		The prepared copy object
	 */
	private Copy prepareCopyForHeaderMetaData(String variableName, String property) {
		Copy copy = new Copy();
		FromSpec from = new FromSpec();
		from.setType(fromTypes.VARIABLE);
		from.setPart("payload");
		from.setVariableName("input");
		from.setQueryLanguage("urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0");
		from.setQuery("<![CDATA[tns:" + property + "]]>");
		copy.setFromSpec(from);
		
		ToSpec to = new ToSpec();
		to.setType(toTypes.VARIABLE);
		to.setVariableName(variableName);
		to.setHeader(property);
		copy.setToSpec(to);
		
		return copy;
	}
	
	/**
	 * <p>Creates the BPEL "assign" element from an task.</p>
	 * 
	 * <p>The standard, validate and copy 
	 * attributes and elements are taken from the task.</p>
	 * 
	 * <p>
	 * The copy elements are generated using the appropriate method
	 * {@link #createCopyElement(Copy, AssignTask)}.
	 * </p>
	 * 
	 * @param task	The task, to generate the assign element from. 
	 * 
	 * @return 		The generated BPEL "assign" element
	 */
	public Element createAssignElement(Task task) {
		Element assign = this.document.createElement("assign");
		assign.setAttribute("validate", "no");
		
		
		Copy copy = prepareCopyObject(task);
		
		Element copyElement = createCopyElement(copy);
		if (copyElement != null) {
			assign.appendChild(copyElement);
		}
		
		/* Get all input data objects of the Task, they shell be handled copy
		 * elements. Input data object are used to describe the handover of 
		 * parameters */
		for (Copy handoverCopy : createCopyObjectForHandoverParameters(task)) {
			assign.appendChild(createCopyElement(handoverCopy));
		}
		
		/* Copy token and reporting service url */
//		for (Copy metaCopy : createCopyForMetaParameters(task.getId())) {
//			assign.appendChild(createCopyElement(metaCopy));
//		}
		
		/* Set header information for proxy usage */
		copy = createCopyObjectForHeader(task.getId(), task.getColor(), 
				task.getServiceName());
		copyElement = createCopyElement(copy);
		if (copyElement != null) {
			assign.appendChild(copyElement);
		}
		
		return assign;
	}
	
	/**
	 * Creates a copy object to describe the handover of an output parameter
	 * to an input parameter.
	 * 
	 * @param task
	 * 		The {@link Task} object.
	 * @return
	 * 		A list of the resulting {@link Copy} objects.
	 */
	private List<Copy> createCopyObjectForHandoverParameters(Task task) {
		ArrayList<Copy> handoverCopies = new ArrayList<Copy>();
		
		for (DataObject dataObject : task.getInputDataObjects()) {
			/* Get source parameter */
			Task sourceTask = dataObject.getFirstInputTask();
			if (sourceTask == null) {
				continue;
			}
			
			/* Copy parameter */
			Copy copy = new Copy();
			FromSpec from = new FromSpec();
			from.setType(fromTypes.EXPRESSION);
			from.setExpression("$" + sourceTask.getId() 
					+ "Response" + ".parameters/return");
			copy.setFromSpec(from);
			
			ToSpec to = new ToSpec();
			to.setType(toTypes.EXPRESSION);
			to.setExpression("$" + task.getId() 
					+ ".parameters/" + dataObject.getTargetOfCopy());
			copy.setToSpec(to);
			
			handoverCopies.add(copy);
		}
		
		return handoverCopies;
	}
	
//	/**
//	 * Creates the BPEL copy elements to pass the token and reporting service URL
//	 * as web service method parameters.
//	 * 
//	 * @return
//	 * 		The list of copy objects
//	 */
//	private List<Copy> createCopyForMetaParameters(String targetVariable) {
//		ArrayList<Copy> copies = new ArrayList<Copy>();
//		
//		/* Copy token */
//		Copy copy = new Copy();
//		FromSpec from = new FromSpec();
//		from.setType(fromTypes.EXPRESSION);
//		from.setExpression("$input.payload/tns:token");
//		copy.setFromSpec(from);
//		
//		ToSpec to = new ToSpec();
//		to.setType(toTypes.EXPRESSION);
//		to.setExpression("$" + targetVariable + ".parameters/token");
//		copy.setToSpec(to);
//		
//		copies.add(copy);
//		
//		/* Copy reporting service URL */
//		copy = new Copy();
//		from = new FromSpec();
//		from.setType(fromTypes.EXPRESSION);
//		from.setExpression("$input.payload/tns:reportingServiceUrl");
//		copy.setFromSpec(from);
//		
//		to = new ToSpec();
//		to.setType(toTypes.EXPRESSION);
//		to.setExpression("$" + targetVariable + ".parameters/reportingServiceUrl");
//		copy.setToSpec(to);
//		
//		copies.add(copy);
//		
//		return copies;
//	}

	/**
	 * Creates the assign element related to a {@link BPELDataObject} and its 
	 * connected {@link Task}. The basic steps are: 
	 * <ul>
	 * 	<li>
	 * 		Create a copy element to assign the web service method's parameters
	 * 		to the input variable.
	 *  </li>
	 *  <li>
	 *  	Create a copy element to add additional soap header information for
	 *  	the service proxy.
	 *  </li>
	 * </ul>
	 * 
	 * @param dataObject
	 * 		The source data object
	 * @param task
	 * 		The connected task
	 * @return
	 * 		The bpel assign element
	 */
//	public Element createAssignElement(BPELDataObject dataObject, Task task) {
//		Element assign = this.document.createElement("assign");
////		assign.setAttribute("validate", "no");
////		
////		/* Get all input data objects of the BPELDataObject */
////		List<BPELDataObject> bpelDataObjects = dataObject.getSourceBPELDataObjects();
////		
////		//TODO create copy element for each bpelDataObjects
////		Copy copy = prepareCopyObject(dataObject);
////		
////		Element copyElement = createCopyElement(copy);
////		if (copyElement != null) {
////			assign.appendChild(copyElement);
////		}
////		
////		copy = createCopyObjectForHeader(dataObject.getId(), task.getColor(), 
////				dataObject.getServiceName());
////		copyElement = createCopyElement(copy);
////		if (copyElement != null) {
////			assign.appendChild(copyElement);
////		}
//		
//		return assign;
//	}
	
	/**
	 * Creates the from and to specification of the copy object that assigns the
	 * target service URL to the appropriated soap header.
	 * 
	 * @param variableName
	 * 		The name of the variable to append the header
	 * @param group
	 * 		The group, to distinguish different ground stations (GoldenEye specific)
	 * @param serviceName
	 * 		The name of the related web service
	 * @return
	 * 		The filled copy object
	 */
	private Copy createCopyObjectForHeader(String variableName, String group,
			String serviceName) {
		Copy copy = new Copy();
		
		/* Create to spec part */
		ToSpec toSpec = new ToSpec();
		toSpec.setType(toTypes.VARIABLE);
		toSpec.setVariableName(variableName);
		toSpec.setHeader("serviceProxy");
		
		/* Create from spec part */
		FromSpec fromSpec = new FromSpec();
		fromSpec.setType(fromTypes.LITERAL);
		
		Element literal = this.document.createElement("literal");
		Element targetServiceURL = this.document.createElementNS(
				"http://goldeneye.org/header/", 
				"targetServiceURL");
		targetServiceURL.setTextContent("#" + group + "_" + serviceName + "#");
		literal.appendChild(targetServiceURL);
		
		fromSpec.setLiteral(literal);
		
		/* Add from and to spec to copy object */
		copy.setFromSpec(fromSpec);
		copy.setToSpec(toSpec);
		
		return copy;
	}

	/**
	 * Creates a {@link Copy} object based on the passed {@link Task}.
	 * 
	 * @param dataObject
	 * 		The source {@link Task}
	 * @return
	 * 		The resulting copy object
	 */
	private Copy prepareCopyObject(Task task) {
		Copy copyObj = new Copy(this.document);
		copyObj.setIgnoreMissingFromData("yes");
		
		try {
			copyObj.setFromSpecBasedOnTask(task);
		} catch (JSONException e) {
			return null;
		}
		
		copyObj.setToSpecBasedOnTask(task);
		
		return copyObj;
	}

	/**
	 * <p>Creates the BPEL4Chor "empty" element from an empty task.</p>
	 * 
	 * <p>The standard attributes are taken from the task.</p>
	 * 
	 * @param task	The task, to generate the empty element from. 
	 * 
	 * @return 		The generated BPEL4Chor "empty" element
	 */
	public Element createEmptyElement(EmptyTask task) {
//		Element empty = this.document.createElement("empty");
		
//		BPELUtil.setStandardAttributes(empty, task);
		
//		return createScopeForAttachedHandlers(empty, task);
		return null;
		
	}
	
	/**
	 * <p>Creates the BPEL4Chor "opaqueActivity" element from an opaque task.</p>
	 * 
	 * <p>The standard attributes are taken from the task.</p>
	 * 
	 * @param task	The task, to generate the "opaqueActivity" element from. 
	 * 
	 * @return 		The generated BPEL4Chor "opaqueActivity" element
	 */
	public Element createOpaqueElement(NoneTask task) {
//		Element opaque = this.document.createElement("opaqueActivity");
		
//		BPELUtil.setStandardAttributes(opaque, task);
		
//		return createScopeForAttachedHandlers(opaque, task);
		return null;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "compensate" or "compensateScope" element 
	 * from an intermediate event.</p>
	 * 
	 * <p>The standard attributes are taken from the event.
	 * If the trigger defines an activity to compensate, an "compensateScope" 
	 * element will be generated with the target attribute set to this activity.</p>
	 * 
	 * @param event The event, to generate the compensate of compensateScope element from. 
	 * 
	 * @return 		The generated BPEL4Chor "compensate" or "compensateScope" element
	 */
	public Element createCompensateElement(IntermediateEvent event) {
//		Element result = null;
//		if ((event.getTrigger() != null) && (event.getTrigger() instanceof ResultCompensation)) {
//			ResultCompensation trigger = (ResultCompensation)event.getTrigger();
//			if (trigger.getActivity() != null) {
//				Activity act = trigger.getActivity();
//				String name = null;
//				if (act instanceof Task) {
//					if (act instanceof ServiceTask) {
//						name = act.getName();
//					} else {
//						// task must have an attached compensation event
//						if (act.getAttachedEvents(
//							IntermediateEvent.TRIGGER_COMPENSATION).isEmpty()) {
//							
//							this.output.addError("The task must have an attached compensation event to be compensated.", act.getId());
//							return null;
//						}
//						// scope created around task is compensated
//						name = BPELUtil.generateScopeName(act);
//					}
//				} else {
//					name = act.getName();
//				}
//				result = this.document.createElement("compensateScope");
//				result.setAttribute("target", name);
//				return result;
//			}
//		} 
//		result = this.document.createElement("compensate");
//		BPELUtil.setStandardAttributes(result, event);
//		return result;
		return null;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "throw" or "rethrow" element 
	 * from an intermediate event.</p>
	 * 
	 * <p>The standard attributes are taken from the event.
	 * If the trigger defines an error code a "throw" 
	 * element will be generated with the faultName attribute set 
	 * to the defined error code. In this case also the faultVariable 
	 * attribute may be generated if a standard variable is associated with the event. 
	 * </p>
	 * 
	 * @param event 			The event, to generate the throw of rethrow 
	 * 							element from. 
	 * @param rethrowAllowed 	True, if a rethrow element is allowed in the 
	 * 							context of the event, false otherwise.
	 * 
	 * @return 		The generated BPEL4Chor "throw" or "rethrow" element. 
	 * 				The result is null, if the trigger type of the event is not
	 * 				an error trigger or if a rethrow should be generated 
	 * 				although it is not allowed in this context.
	 */
	public Element createThrowElement(IntermediateEvent event, boolean rethrowAllowed) {
		Element result = null;
//		if (!event.getTriggerType().equals(IntermediateEvent.TRIGGER_ERROR)) {
//			return result;
//		}
//		if (event.getTrigger() == null) {
//			if (rethrowAllowed) {
//				result = this.document.createElement("rethrow");
//				BPELUtil.setStandardAttributes(result, event);
//			} else {
//				this.output.addError("The activity must define an error code, because a rethrow is not allowed in this context.", event.getId());
//				return null;
//			}
//		} else if (event.getTrigger() instanceof ResultError) {
//			ResultError trigger = (ResultError)event.getTrigger();
//			if (trigger.getErrorCode() == null || trigger.getErrorCode().equals("")) {
//				if (rethrowAllowed) {
//					result = this.document.createElement("rethrow");
//					BPELUtil.setStandardAttributes(result, event);
//				} else {
//					this.output.addError("The activity must define an error code because a rethrow is not allowed in this context.", event.getId());
//					return null;
//				}
//			} else {
//				result = this.document.createElement("throw");
//				BPELUtil.setStandardAttributes(result, event);
//				result.setAttribute("faultName", trigger.getErrorCode());
//				VariableDataObject faultVariable = 
//					this.diagram.getStandardVariable(event, false);
//				if (faultVariable != null) {
//					result.setAttribute("faultVariable", faultVariable.getName());
//				}
//			}
//		}
		return result;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "wait" element from an intermediate event.</p>
	 * 
	 * <p>The standard attributes are taken from the event.</p>
	 * 
	 * <p>The triger of the event must either define a time deadline of a time duration.
	 * The until of for attributes will be generated from this using the method 
	 * {@link SupportingFactory#createExpressionElement(String, model.supporting.Expression)}.</p>
	 * 
	 * @param event 			The event, to generate the wait element from. 
	 * 
	 * @return 		The generated BPEL4Chor wait element. 
	 * 				The result is null, if the trigger type of the event is not
	 * 				a time trigger or if the event trigger does not define 
	 * 				a time duration or a time deadline.
	 */
	public Element createWaitElement(IntermediateEvent event) {
		Element result = null;
//		if ((event.getTrigger() != null) && 
//				(event.getTrigger() instanceof TriggerTimer)) {
//			
//			TriggerTimer trigger = (TriggerTimer)event.getTrigger();
//			result = this.document.createElement("wait");
//			BPELUtil.setStandardAttributes(result, event);
//			
//			if (trigger.getTimeDeadlineExpression() != null) {
//				result.appendChild(this.supportingFactory.createExpressionElement(
//						"until", trigger.getTimeDeadlineExpression()));
//			} else if (trigger.getTimeDurationExpression() != null) {
//				result.appendChild(this.supportingFactory.createExpressionElement(
//						"for", trigger.getTimeDurationExpression()));
//			} else {
//				this.output.addError("The duration or deadline expression of the wait activity could not be generated.", event.getId());
//				return null;
//			}
//		} else {
//			this.output.addError("The event must define a timer trigger element.", event.getId());
//			return null;
//		}
		return result;
	}
	
	/**
	 * <p>Creates the BPEL4Chor "validate" element from a validate task.</p>
	 * 
	 * <p>The standard attributes are taken from the task.</p>
	 * 
	 * <p>The variables to validate are generated from the standard variable 
	 * data objects associated with the validate element.</p>
	 * 
	 * @param task The task, to generate the validate element from. 
	 * 
	 * @return 		The generated BPEL4Chor validate element. 				
	 */
	public Element createValidateElement(ValidateTask task) {
		Element result = this.document.createElement("validate");
//		BPELUtil.setStandardAttributes(result, task);
		
		List<VariableDataObject> variables = 
			this.diagram.getStandardVariables(task, false);
		String variablesStr = "";
		for (Iterator<VariableDataObject> it = variables.iterator(); it.hasNext();) {
			if (variablesStr.equals("")) {
				variablesStr += it.next().getName();
			} else {
				variablesStr += " " + it.next().getName();
			}
		}
		
		if (variablesStr.equals("")) {
			this.output.addError("The variables for this validate task could not be determined.", task.getId() );
		} else {
			result.setAttribute("variables", variablesStr);
		}
		
//		return createScopeForAttachedHandlers(result, task);
		return null;
	}
}
