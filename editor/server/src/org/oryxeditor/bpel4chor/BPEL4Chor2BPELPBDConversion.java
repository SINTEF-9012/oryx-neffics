package org.oryxeditor.bpel4chor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Copyright (c) 2009-2010 
 * 
 * Changhua Li
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


/**
 * !!!!!!Attention!!!!!!
 * Now this files works isolated from the other files, which outside of this directory.
 * But it should be added into oryx as a plugin in the further.
 * 
 * It will be used for the Transformation of the BPEL4Chor to BPEL.
 * 
 * It was designed for the Diplom Arbeit of Changhua Li(student of uni. stuttgart), 
 * It is the procedure of conversion for an PBD , which was designed in the Studien Arbeit
 * of Peter Reimann(2008)
 */
public class BPEL4Chor2BPELPBDConversion {//extends FunctionsOfBPEL4Chor2BPEL {
	
	private Document currentDocument;								// the current document to handle
	private static String process_nsprefix = "";					// the name space prefix of the target name space of the current PBD
	private static Set<String> paSetList = new HashSet<String>();	// a list of sets of participant references over which the 
																	// set-based <forEach> activities in this process iterate
																	// they are used as names of variables containing an array of
																	// endpoint references
	// attention: global name space prefix set here is namespacePrefixSet !!

	final static String EMPTY = "";
	public Set<String> messageLinkSet = new HashSet<String>();
	// 3.20: fportTypeMC()
	public HashMap<String, String> ml2ptMap = new HashMap<String, String>();

	// 3.21: foperationMC()
	public HashMap<String, String> ml2opMap = new HashMap<String, String>();

	// 3.22: partnerLink Set
	public Set<String> plSet = new HashSet<String>();

	// 3.23: messageConstruct --> partnerLink Mapping
	public HashMap<String, PartnerLink> mc2plMap = new HashMap<String, PartnerLink>();

	// 3.24: scope --> partnerLinkSet Mapping
	public HashMap<String, Set<PartnerLink>> sc2plMap = new HashMap<String, Set<PartnerLink>>();

	// 3.26: partnerLink --> partnerLinkType
	public HashMap<String, String> pl2plTypeMap = new HashMap<String, String>();

	// 3.30: partnerLink --> myRole 
	public HashMap<String, String> pl2myRoleMap = new HashMap<String, String>();

	// 3.31: partnerLink --> partnerRole
	public HashMap<String, String> pl2partnerRoleMap = new HashMap<String, String>();

	// for function 3.11 fscopePa
	public HashMap<String, Object> pa2scopeMap = new HashMap<String, Object>();

	// used by 3.34
	public HashMap<String, String> corrPropName2propertyMap = new HashMap<String, String>();

	// used by 3.35
	public HashMap<String, String> property2nsprefixOfPropMap = new HashMap<String, String>();

	// 3.2: record all name space prefixes of QName
	public Set<String> namespacePrefixSet = new HashSet<String>();
	public HashMap<String, String> ns2prefixMap = new HashMap<String, String>();
	public String topologyNS;					// it will be used in conversion of PBD
	public HashMap<String, String> forEach2setMap = new HashMap<String, String>();

	public HashMap<String, Object> ml2mcMap = new HashMap<String, Object>();


	// 3.5: process set
	public Set<String> processSet = new HashSet<String>();
	//private HashMap<String, String> paType2PBDMap = new HashMap<String, String>();
	
	// 3.8: participant set
	public Set<String> paSet = new HashSet<String>();

	// 3.10: scopes set
	public Set<String> scopeSet = new HashSet<String>();
	public Set<String> messageConstructsSet;


	/**
	 * Algorithm 3.5 and Algorithm 3.17 Conversion of one PBD into BPEL
	 * 
	 * @param {Element} currentElement     The current element
	 */
	public void convertPBD(Document docPBD){
		Element currentElement = (Element)docPBD.getFirstChild();
		if(!(currentElement.getNodeName().equals("process"))){
			return;
		}
															// the input process points on the <process> activity of current PBD
		//String process_nsprefix;							// the name space prefix of the target name space of the current PBD
		Set<String> nsprefixSet = this.namespacePrefixSet;	// a list of name space prefixes referencing to the name
															// spaces of the WSDL definitions of port types used in
															// this process
		//Set<String> paSetList = paSetList;				// a list of sets of participant references over which the 
															// set-based <forEach> activities in this process iterate
															// they are used as names of variables containing an array of
															// endpoint references
		//System.out.println("scope set is: " + scopeSet);
		//System.out.println("pa2scopeMap is: " + pa2scopeMap);
		//System.out.println("process set is: " + processSet);
		//System.out.println("partnerLink set is: " + plSet);
		//System.out.println("sc2plMap is: " + sc2plMap);
		//System.out.println("messageConstructsSet is: " + messageConstructsSet);
		//System.out.println("messageLinkSet is: " + messageLinkSet);
		//System.out.println("ml2ptMap is: " + ml2ptMap);
		//System.out.println("ml2mcMap is: " + ml2mcMap);
		//System.out.println("nsprefixSet is: " + nsprefixSet);
		//System.out.println("ns2prefixMap is: " + ns2prefixMap);
		//System.out.println("--------------------------------------------------");
		
		if(!(process_nsprefix.isEmpty())){
			process_nsprefix = "";
		}
		process_nsprefix = fprefixNSofPBD(currentElement);
		//System.out.println("process_nsprefix of PBD is: " + fprefixNSofPBD(currentElement));

		// add the declaration of the name space of the partner link type definitions
		currentElement.setAttribute("xmlns:plt", topologyNS + "/partnerLinkTypes");
		//System.out.println("xmlns:plt=" + currentElement.getAttribute("xmlns:plt"));
	
		// start depth-first search
		executeDepthFirstSearch(currentElement);							// algorithm 3.7

		// add partner link declarations to the process
		String localName = currentElement.getAttribute("name");				// localName is value of "name" of the PBD
		//System.out.println("#########" + localName);
		declarePartnerLinks(currentElement, localName);						// algorithm 3.6
		
		// add the declarations of the name spaces of the port type definitions
		declareNameSpaces(currentElement, nsprefixSet);						// algorithm 3.8
		
		// declare the variables containing an array of endpoint references
		declareSrefVariables(currentElement, paSetList);					// algorithm 3.18
		if(!(paSetList.isEmpty())){
			paSetList.clear();
		}
	}
	
	/**
	 * Algorithm 3.6 declarepartnerLinks
	 * 
	 * @param {Element} currentElement     current element (process or scope)
	 * @param {String}  localName          value of name attribute
	 */
	private void declarePartnerLinks(Element currentElement, String localName){
		String sc;											// an element of (scopeSet U processSet), it is a QName
		Set<PartnerLink> partnerLinkSet;					// plSet for declarePartnerLinks is ready
		PartnerLink pl = null;								// a single partner link declaration
		Element partnerLinks, partnerLink;					// single BPEL constructs
		String role;										// a participant reference, it is a participant
		String s;											// NCName
		
		sc = buildQName(process_nsprefix, localName);		// the global name of the current scope or process and the corresponding 
															// element of (scopeSet U processSet)
		if (scopeSet.contains(sc) || processSet.contains(sc)){
			// sc is a process or a single participant reference is limited to the scope sc
			// thus the function fpartnerLinksScope can be used on sc
			// determine the set of partner link declarations that need to be declared
			partnerLinkSet = (Set<PartnerLink>)fpartnerLinksScope(sc);
			//System.out.println("sc2plMap: " + sc2plMap);
			if(partnerLinkSet != null){
				// There are partner links to be declared
				//System.out.println(partnerLinkSet.iterator().next().getName());
				partnerLinks = currentDocument.createElement("partnerLinks");
				currentElement.appendChild(partnerLinks);					// adding a <partnerLinks> declaration
				//System.out.println("partnerLinks is: " + partnerLinks.getTagName());
				Iterator<PartnerLink> it = partnerLinkSet.iterator();
				while(it.hasNext()){
					pl = (PartnerLink)it.next();
					// create a new partner link declaration for pl
					partnerLink = currentDocument.createElement("partnerLink");
					partnerLink.setAttribute("name", pl.getName());
					s = ftypePL(pl);
					partnerLink.setAttribute("partnerLinkType", "plt:" + s);	// "plt" is the name space prefix of the name space of
																				// the partner link type declarations
					role = fmyRolePL(pl);										// the myRole of pl
					if (!role.isEmpty()){
						partnerLink.setAttribute("myRole", role);
					}
					role = fpartnerRolePL(pl);									// the partnerRole of pl
					if (!role.isEmpty()){
						partnerLink.setAttribute("partnerRole", role);
					}
					partnerLinks.appendChild(partnerLink);						// add the new partner link declaration to the 
																				// <partnerLinks> declaration
				}
			}
		}
	}

	/**
	 * Algorithm 3.7 executeDepth-firstSearch
	 * 
	 * @param {Element} currentConstruct     The current BPEL construct
	 */
	private void executeDepthFirstSearch(Element currentConstruct){
		// the input currentConstruct points on the tag of the current BPEL construct
		NodeList constructList;													// a list of BPEL constructs
		Node construct;															// a single BPEL construct
		constructList = currentConstruct.getChildNodes();
		for(int i=0; i<=constructList.getLength(); i++){
			construct = constructList.item(i);
			if(construct instanceof Element){
				//System.out.println("!!!!!!!!!!!!!!!!!current construct is: " + ((Element)construct).getTagName());
				modifyConstruct((Element)construct);
			}
		}
	}

	/**
	 * Algorithm 3.8 declareNameSpaces
	 * 
	 * @param {Element} construct     The tag of the current BPEL construct
	 * @param {Set}     nsprefixSet   includes the name space prefixes referencing to the name spaces 
	 *                                which need to be declared within construct
	 */
	protected void declareNameSpaces(Element construct, Set<String> nsprefixSet){
		// the input construct points on the tag of the current BPEL construct, and nsprefixSet includes the name space prefixes
		// referencing to the name spaces which need to be declared within construct
		String nsprefix = EMPTY;												// a single name space prefix
		// add each name space declaration to the construct
		Iterator<String> it = namespacePrefixSet.iterator();
		while(it.hasNext()){
			nsprefix = (String)it.next();
			if (!nsprefix.equals("targetNamespace")){
				construct.setAttribute("xmlns:" + nsprefix, ns2prefixMap.get(nsprefix));
			}
		}
	}
	
	/**
	 * Algorithm 3.9,3.12 and 3.14 (refined)modifyConstruct
	 * 
	 * @param {Element} construct     The current BPEL construct
	 */
	private void modifyConstruct(Element construct){
		// the input construct points on the tag of the current BPEL construct
		String constructName = construct.getTagName();							// the name of the current BPEL construct
		Element fEScope;														// a single BPEL construct
		
		if(constructName.equals("invoke") || constructName.equals("onMessage") 
				|| constructName.equals("onEvent")){
			// construct is an <invoke> activity or an <onMessage> or <onEvent> construct
			modifyMessageConstruct(construct, constructName);					// algorithm 3.10
			executeDepthFirstSearch((Element)construct);						// continue depth-first search
		}
		else if(constructName.equals("receive") || constructName.equals("reply")){
			// construct is a <receive> or <reply> activity
			modifyMessageConstruct(construct, constructName);					// algorithm 3.10
		}
		else if(constructName.equals("scope") && construct.hasAttribute("wsu:id")){
			// construct is a <scope> activity having a wsu:id attribute assigned
			executeDepthFirstSearch((Element)construct);						// continue depth-first search
			declarePartnerLinks(construct, construct.getAttribute("wsu:id"));	// algorithm 3.10
			String id = construct.getAttribute("wsu:id");
			construct.removeAttribute("wsu:id");
			construct.setAttribute("name",id);
		}
		else if(constructName.equals("forEach")){
			// construct is a <forEach> activity
			NodeList nl = construct.getElementsByTagName("scope");
			for (int i=0; i<nl.getLength(); i++){
				fEScope = (Element)nl.item(i);									// fEScope points on the <scope> activity nested
				//System.out.println("feScope: " + fEScope.getNodeName());		// in the <forEach> activity
				// continue depth-first search on the scope
				modifyConstruct(fEScope);										// recursion
				if(construct.hasAttribute("wsu:id")){
					String id = construct.getAttribute("wsu:id");				// the value of the wsu:id attribute is stored in id since it is
																				// used twice
					// modify the set-based <forEach> activity
					modifyForEach(construct, fEScope, id);						// algorithm 3.15
					
					// add partner link declarations to the scope
					declarePartnerLinks(fEScope, id);
					construct.removeAttribute("wsu:id");
					construct.setAttribute("name",id);							
				}
			}
		}
		else if(constructName.equals("correlationSets")){
			// construct is a <correlationSets> construct
			// it includes one or more <correlationSet> constructs (and nothing else)
			Element corrSet;													// a single BPEL construct
			NodeList corrList = construct.getElementsByTagName("correlationSet");// corrList becomes the list of <correlationSet> consturcts nested
																				// in the current <correlationSets> construct
			// modify each of these <correlationSet> constructs
			for(int i=0; i<corrList.getLength(); i++){
				corrSet = (Element)corrList.item(i);
				modifyCorrelationSet(corrSet);									// algorithm 3.13
			}
		}
		else{
			// construct may be any BPEL construct, e.g. a structured activity
			// continue depth-first search
			executeDepthFirstSearch(construct);
		}
	}
	
	/**
	 * Algorithm 3.10 modifyMessageConstruct
	 * 
	 * @param {Element} construct     The current BPEL construct
	 * @param {String}  constructName The local name of this construct
	 */
	private void modifyMessageConstruct(Element construct, String constructName){
		// the input construct points on the tag of the current message construct, and constructName is the local name of it
		String mc = EMPTY;														// the current element of MC(messageConstructsSet)
		String constructID = EMPTY;												// NCName
		PartnerLink pl = null;													// a single partner link declaration
		String pt = EMPTY;														// a single port type
		String op = EMPTY;														// a single operation
		String pt_nsprefix = EMPTY;												// the name space prefix of the port type pt
		
		// change the wus:id attribute to a name attribute if possible
		if (construct.hasAttribute("wsu:id")){
			constructID = construct.getAttribute("wsu:id");
		}
		if (!constructName.equals("onMessage") && !constructName.equals("onEvent")){
			// <onMessage> and <onEvent> constructs are not allowed to have a name attribute assigned
			construct.removeAttribute("wsu:id");
			construct.setAttribute("name", constructID);
		}
		mc = buildQName(process_nsprefix, constructID);							// mc becomes the global name of the current message
																				// construct and the corresponding element of MC
		// add a partnerLink attribute to the message construct
		pl = fpartnerLinkMC(mc);												// pl becomes the partner link declaration used by 
																				// the current message construct
		if (pl != null){
			construct.setAttribute("partnerLink", pl.getName());
			//System.out.println("mc mapped to pl: " + pl.getName());
		}
		
		// add a portType attribute to the message construct
		//System.out.println(mc);
		pt = fportTypeMC(mc);
		if(pt != null){															// when no mapping between mc and pt then null
			construct.setAttribute("portType", pt);
			
			// add the name space prefix of pt to the global list nsprefixList if it has not been added before
			pt_nsprefix = fnsprefixPT(pt);
			if (!namespacePrefixSet.contains(pt_nsprefix) && !pt_nsprefix.equals(EMPTY)){
				namespacePrefixSet.add(pt);
			}
		}
		
		// add an operation attribute to the message construct
		op = foperationMC(mc);
		if(op != null){															// when there is no mapping between mc an op then null
			construct.setAttribute("operation", fremoveNSPrefix(op));			// the operation attribute needs to be associated with
																				// the local name of the operation
		}
	}

	/**
	 * Algorithm 3.13 modifyCorrelationSet
	 * 
	 * @param {Element} correlationSet     The tag of the current <correlationSet> construct
	 */
	private void modifyCorrelationSet(Element correlationSet){
		// the input correlationSet points on the tag of the current <correlationSet> construct
		Set<String> propNameSet = new HashSet<String>();						// a list of property names
		String propName;														// a single property name
		Set<String> propertySet = new HashSet<String>();						// a list of WSDL properties
																				// initially the empty list
		String property;														// a single WSDL property
		String nsprefix;														// a single name space prefix
		
		// get the list of property names of the current correlation set
		propNameSet = getAttributeValueAsList(correlationSet, "properties");
		if (!propNameSet.isEmpty()){
			// use the function fpropertyCorrPropName to get corresponding list of WSDL properties
			Iterator<String> it = propNameSet.iterator();
			while(it.hasNext()){
				propName = (String)it.next();
				property = fpropertyCorrPropName(propName);
				propertySet.add(property);
				// add the name space prefix of the current WSDL property to the global list nsprefixList of name space
				// prefixes if it has not been added before
				// the corresponding name space declaration is added to the <process> activity after having finished the
				// depth-first search
				nsprefix = fnsprefixProperty(property);
				if (!namespacePrefixSet.contains(nsprefix)){
					namespacePrefixSet.add(nsprefix);
				}
			}
		}
		// change the property names to the corresponding references to WSDL properties
		addAttributeList(correlationSet, "properties", propertySet);
	}
	
	/**
	 * Algorithm 3.15 modifyForEach
	 * 
	 * @param {Element} forEach     The tag <forEach>
	 * @param {Element} fEScope     a BPEL construct
	 * @param {String}  id          NCName
	 */
	private void modifyForEach(Element forEach, Element fEScope, String id){
		// the input forEach points on the tag of the current <forEach> activity, the input fEScope on the tag of the
		// <scope> activity nested in the <forEach> activity, and the input id is its wsu:id
		String fe;																// name of <Scope> inherits of QName
		String set;																// name of <Participant> inherits of NCName
		Element startCounter = currentDocument.createElement("startCounterValue");	// the <startCounterValue>
		Element finalCounter = currentDocument.createElement("finalCounterValue");  // the <finalCounterValue>
		Element completionCondition = getChildElement(forEach, "completionCondition");// the optional <completionCondition>
																					// construct of the current <forEach>
		fe = buildQName(process_nsprefix, id);
		//System.out.println("forEach of <forEach> is: " + fe);
		//System.out.println("scopeSet is: " + scopeSet);
		// the global name of the current <forEach> activity and the corresponding element of scopeSet
		if (scopeSet.contains(fe)){
			// a set of participant references is associated with fe by a forEach attribute, thus it is a set-based
			// <forEach> activity.
			// add the counterName attribute
			forEach.setAttribute("counterName", "i_" + id);
			// add the contents to the <startCounterValue> and the <finalCounterValue>
			startCounter.setTextContent("0");
			set = fsetForEach(fe);
			finalCounter.setTextContent("count($" + set + "/)-1");
			// fsetForEach(fe) is the name of the set of participant references over which the current <forEach> activity
			// iterates
			// Add this name to the global list paSet if it has not been added before
			//System.out.println("paSetList is: " + paSetList);
			if(!paSetList.contains(set)){ 							
				paSetList.add(set);
			}
			// enclose the <startCounterValue> and the <finalCounterValue> constructs before the <completionCondition>
			// construct and the <scope> activity of the <forEach> activity
			if(completionCondition != null){
				forEach.removeChild(completionCondition);
			}
			if(fEScope != null){
				forEach.removeChild(fEScope);
			}
			forEach.appendChild(startCounter);
			forEach.appendChild(finalCounter);
			if(completionCondition != null){
				forEach.appendChild(completionCondition);
			}
			if(fEScope != null){
				forEach.appendChild(fEScope);
			}
			// add the <assign> activities to copy the endpoint references on the partner links
			Set<PartnerLink> plSetForEach = fpartnerLinksScope(fe);
			//System.out.println("scopeSet is: " + scopeSet);
			//System.out.println("sc2plMap is: " + sc2plMap);
			//System.out.println("plSetForEach is: " + plSetForEach);
			if(plSetForEach != null && plSetForEach.size() == 1){
				addAssignsToForEach(fEScope, set, plSetForEach.iterator().next(), id);// algorithm 3.16
			}
		}
	}

	/**
	 * Algorithm 3.16 addAssignsToForEach
	 * 
	 * @param {Element}     fEScope     The tag <scope> nested in the current <forEach> activity
	 * @param {String}      set         It is the set participant references over which the <forEach> activity iterates
	 * @param {PartnerLink} pl          It is partner link declaration between the owner and the iterator of the <forEach>
	 * @param {String}      id          It is its wsu:id
	 */
	private void addAssignsToForEach(Element fEScope, String set, PartnerLink pl, String id){
		Element nestedActivity = null;
		NodeList nl = fEScope.getChildNodes();
		Node childNode;
		for(int i=0; i<nl.getLength(); i++){
			childNode = nl.item(i);
			if(childNode instanceof Element){
				nestedActivity = (Element)childNode;								// last child of the <scope> activity
																					// this is the nested activity
			}
		}

		Element sequence = currentDocument.createElement("sequence");				// the new <sequence> activity
		Element assign   = currentDocument.createElement("assign");					// the new <assign> activity
		Element copy	 = currentDocument.createElement("copy");					// the new <copy> activity
		Element from	 = currentDocument.createElement("from");					// the new <from> activity
		Element query	 = currentDocument.createElement("query");					// the new <query> activity
		Element to		 = currentDocument.createElement("to");						// the new <to> activity
		
		fEScope.removeChild(nestedActivity);										// remove the nested activity
		from.setAttribute("variable", set);											// specify the <copy> construct
		query.setTextContent("[$i_" + id + "]");
		to.setAttribute("partnerLink", pl.getName());
		
		// arrange the <copy> construct and add it to the <assign> activity
		from.appendChild(query);
		copy.appendChild(from);
		copy.appendChild(to);
		assign.appendChild(copy);
		
		sequence.appendChild(assign);							// add the <assign> activity to the new <sequence> activity
		sequence.appendChild(nestedActivity);					// add the nested activity to the new <sequence> activity
		fEScope.appendChild(sequence);							// add the <sequence> activity to the <scope> activity
	}

	/**
	 * Algorithm 3.18 declareSrefVariables
	 * 
	 * @param {Element} construct     current BPEL construct
	 * @param {Set}     paSetList     list of sets of participant reference
	 */
	private void declareSrefVariables(Element construct, Set<String> paSetList){
		// the input construct points on the tag of the current BPEl construct, and PaSetList is the list of sets of
		// participant references for which a variable containing the corresponding array of endpoint references needs
		// to be declared.
		Element variables, variable;							// single BPEL constructs
		String paSetElement;									// a participant references
		
		if(paSetList != null){									// there are variables to be declared
			// add the name space declaration for the data type service-refs
			String srefsNamespace = "http://www.bpel4chor.org/service-references";
			construct.setAttribute("xmlns:srefs", srefsNamespace);
			// add a <variables> declaration
			//System.out.println(getChildElement(construct, "variables"));
			if((getChildElement(construct, "variables")) == null){
				variables = currentDocument.createElement("variables");
				construct.appendChild(variables);
			}
			// variables becomes the <variables> declaration
			variables = getChildElement(construct, "variables");
			// declare all necessary variables
			//System.out.println("paSet is: " + paSet);
			Iterator<String> it = paSet.iterator();
			while(it.hasNext()){
				paSetElement = it.next();
				if(paSetList.contains(paSetElement)){
					// variable becomes a new <variable> declaration
					variable = currentDocument.createElement("variable");
					// add a name and a type attribute to the <variable> declaration
					variable.setAttribute("name", paSetElement);
					variable.setAttribute("type", "srefs:service-refs");
					// add the <variable> declaration to the <variables> declaration
					variables.appendChild(variable);
				}
			}
			//System.out.println("paSetList is: " + paSetList);
		}
	}
	
	/**
	 * function: fprefixNSofPBD
	 * 
	 * @param  {Element} currentElement     
	 * @return {String}  result
	 */
	private String fprefixNSofPBD(Element currentElement){
		if (!(currentElement instanceof Node || currentElement instanceof Document)) {
			return null;
		}
		
		String result = EMPTY;
		if (currentElement.getNodeName().equals("process") && currentElement.hasAttribute("targetNamespace")){
			String tns = currentElement.getAttribute("targetNamespace");
			if (ns2prefixMap.containsValue(tns)){
				Iterator<String> it = ns2prefixMap.keySet().iterator();
				while(it.hasNext()){
					String key = it.next().toString();
					String value = ns2prefixMap.get(key);
					if (value.equals(tns)){
						return result = key; 
					}
				}
			}
			else
			{
				result = "process with topology not matched!!";
			}
		}
		return result;
	}
	
	/**
	 * fremoveNSPrefix function: this function returns the second NCName of the	QName
	 * 
	 * @param {String} name     It is a name of QName
	 * @return{String} output   a NCName, name without ":"
	 */
	private String fremoveNSPrefix(String name){
		if(name.contains(":")){
			int index = name.indexOf(":");
			name = name.substring(index+1);
			return name;
		}
		return name;
	}

	/**
	 * getAttributeValueAsList function: this function returns the value of the attribute 
	 *                                   having the name name as list.
	 * 
	 * @param {Element} currentElement     current Element
	 * @param {String}  attributeName      name of attribute
	 * @return {Set}    valueSet		   valueSet according the specified attribute
	 */
	private Set<String> getAttributeValueAsList(Element currentElement, String attributeName){
		Set<String> valueSet = new HashSet<String>();
		if (!currentElement.hasAttribute(attributeName)){
			return valueSet;
		}
		
		String values = currentElement.getAttribute(attributeName);
		if (values.contains(" ")){
			String[] valuesList = values.split(" ");
			for (int i=0; i<valuesList.length; i++){
				valueSet.add(valuesList[i]);
			}
		}
		else
		{
			valueSet.add(values);
		}
		
		return valueSet;
	}

	/**
	 * addAttributeList function: this function adds an attribute having the name name, 
	 *                            as value the attribute gets the list.
	 * 
	 * @param {Element} currentElement     current Element
	 * @param {String}  attributeName      name of attribute
	 * @param {Set}     valueList		   the set of value
	 */
	private void addAttributeList(Element currentElement, String attributeName, Set<String> valueList){
		Object[] valuesArray = valueList.toArray();
		if (!valueList.isEmpty()){
			String values = valuesArray[0].toString();
			for (int i=1; i<valuesArray.length; i++){
				if (!valuesArray[i].toString().equals(EMPTY)){
					values = values + " " + valuesArray[i].toString();
				}
			}
			currentElement.setAttribute(attributeName, values);
		}
	}

	/**
	 * getChildElement function: this function get the specified name of the childElement of currentElement.
	 * 
	 * @param {Element} currentElement     
	 * @param {String}  childElement       
	 * @return {Element}returnElement      
	 */
	private Element getChildElement(Element currentElement, String childElement){
		if (currentElement.hasChildNodes()){
			NodeList nl = currentElement.getChildNodes();
			Node child;
			for(int i=0; i<nl.getLength(); i++){
				child = nl.item(i);
				if (child instanceof Element && child.getNodeName().equals(childElement)){
					return (Element)child;
				}
			}
		}
		return null;
	}

	/**
	 * function 3.24: partnerLinksScope: (Scope U Process) -> 2^PL
	 * 
	 * @param {String} sc             The element of scopeSet and processSet
	 * @return {Set}   partnerLinkSet The partner link set
	 */
	private Set<PartnerLink> fpartnerLinksScope(String sc){
		if(sc2plMap.containsKey(sc)){
			return sc2plMap.get(sc);
		}
		return null;
	}

	/**
	 * function 3.26: typePL: PL -> PLType
	 * 
	 * @param {PartnerLink} pl       The partner link
	 * @return {String}     plType   The partner link type
	 */
	private String ftypePL(PartnerLink pl){
		if(pl2plTypeMap.containsKey(pl.getName())){
			return pl2plTypeMap.get(pl.getName());
		}
		return EMPTY;
	}

	/**
	 * function 3.18: nsprefixPT: PT -> NSPrefix
	 * 
	 * @param {String} portType     The port type
	 * @return {String} nsprefix    The name space prefix
	 */
	private String fnsprefixPT(String portType){
		String[] nsprefixSplit;
		if(portType.contains(":")){
			nsprefixSplit = portType.split(":");
			return nsprefixSplit[0];
		}
		return EMPTY;
	}

	/**
	 * function for 3.20: To create a mapping[messageConstruct, messageLink] 
	 * we assume here, that we have already messageLinkSet, ml2mcMap, constrctsML from topologyAnalyze
	 *  
	 * @return {HashMap} mc2mlMap     The mapping of messageConstruct to messageLink
	 */
	private HashMap<String, String> getMc2mlMap(){
		HashMap<String, String> mc2mlMap = new HashMap<String, String>();
		ArrayList<String> valueList = new ArrayList<String>();
		Iterator<String> it = messageLinkSet.iterator();
		while (it.hasNext()){
			String ml = (String)it.next();
			valueList = (ArrayList<String>)ml2mcMap.get(ml);
			String mc1Str = valueList.get(0).toString().split(":")[1];
			String mc2Str = valueList.get(1).toString().split(":")[1];
			mc2mlMap.put(mc1Str, ml);
			mc2mlMap.put(mc2Str, ml);
		}
		return mc2mlMap;
	}

	/**
	 * function 3.20: portTypeMC: MC -> PT
	 * 
	 * @param {String}  mc          The message construct
	 * @return {String} portType    The port type
	 */
	private String fportTypeMC(String mc){
		HashMap<String, String> mc2mlMap = getMc2mlMap();
		if(ml2ptMap.containsKey(mc2mlMap.get((mc.split(":"))[1]))){
			return ml2ptMap.get(mc2mlMap.get((mc.split(":"))[1]));
		}
		return EMPTY;
	}

	/**
	 * function 3.21: operationMC: MC -> O
	 * 
	 * @param {String}  mc         The message construct
	 * @return {String} operation  The operation
	 */
	private String foperationMC(String mc){
		HashMap<String, String> mc2mlMap = getMc2mlMap();
		if(ml2opMap.containsKey(mc2mlMap.get((mc.split(":"))[1]))){
			return ml2opMap.get(mc2mlMap.get((mc.split(":"))[1]));
		}
		return EMPTY;
	}

	/**
	 * function 3.23: partnerLinkMC: MC -> PL
	 * 
	 * @param {String}       mc       The message construct
	 * @return {PartnerLink} pl       The partner link
	 */
	private PartnerLink fpartnerLinkMC(String mc){
		if(mc2plMap.containsKey(mc)){
			return mc2plMap.get(mc);
		}
		return null;
	}

	/**
	 * function 3.30: myRolePL: PL -> Pa U {EMPTY}
	 * 
	 * @param {PartnerLink} pl           The partner link
	 * @return {String}     myRoleValue  The value of myRole in partner link
	 */
	private String fmyRolePL(PartnerLink pl){
		if(pl2myRoleMap.containsKey(pl.getName())){
			return pl2myRoleMap.get(pl.getName());
		}
		return EMPTY;
	}

	/**
	 * function 3.31: partnerRolePL: PL -> Pa U {EMPTY}
	 * 
	 * @param {PartnerLink} pl                The partner link
	 * @return {String}     partnerRoleValue  The value of partnerRole in partner link
	 */
	private String fpartnerRolePL(PartnerLink pl){
		if(pl2partnerRoleMap.containsKey(pl.getName())){
			return pl2partnerRoleMap.get(pl.getName());
		}
		return EMPTY;
	}

	/**
	 * function 3.34: assigning a property to each property name. 
	 *                propertyCorrPropName: CorrPropName -> Property
	 * 
	 * @param  {String} propNameInput      The property name
	 * @return {String} property           The WSDLproperty value
	 */
	private String fpropertyCorrPropName(String propNameInput){
		if(corrPropName2propertyMap.containsKey(propNameInput)){
			return corrPropName2propertyMap.get(propNameInput);
		}
		return EMPTY;
	}

	/**
	 * function 3.35: assigning a name space prefix to each WSDL property. 
	 *                nsprefixProperty: property -> nsprefix
	 * 
	 * @param  {String} propertyInput        The WSDLproperty value in grounding
	 * @return {String} nsprefix             The name space prefix of this value
	 */
	private String fnsprefixProperty(String propertyInput){
		if(property2nsprefixOfPropMap.containsKey(propertyInput)){
			return property2nsprefixOfPropMap.get(propertyInput);
		}
		return EMPTY;
	}

	/**
	 * function 3.36: assigning a set of participant references to each <forEach> activity
	 * Attention: the following functions of 3.36 will be just used within the tag <participantSet> of XML files.
	 * 
	 * @param  {String} sc         The QName of <scope> or <forEach> activity
	 * @return {String} paSetName  The name of <participantSet>
	 */
	private String fsetForEach(String sc){
		if(forEach2setMap.containsKey(sc)){
			return forEach2setMap.get(sc);
		}
		return EMPTY;
	}

	
	/**
	 * function: To build QName for function 3.12 
	 * 
	 * @param {String} prefix     The prefix
	 * @param {String} NCName     The NCName
	 * @return {String} QName     The QName
	 */
	private static String buildQName(String prefix, String NCName){
		return prefix + ":" + NCName;
	}

}
