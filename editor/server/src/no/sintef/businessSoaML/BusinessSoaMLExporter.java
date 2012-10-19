package no.sintef.businessSoaML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.Shape;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.CollaborationUse;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resource.XMI2UMLResource;



/**
 * This class performs the transformation from an Oryx diagram to a SoaML model, using the SoaML UML Profile from Modelio.
 * 
 * We use just plain java for the transformation. This is motivated by pragmatism and the will to have something running quickly.
 * A more Modus-like solution that would suit SINTEF would be to use scala or MOFScript and the Model Transformation Service from COIN.
 * A more Oryx-solution seems to be XSLT transformations.
 * 
 * @author cyril
 *
 */
public class BusinessSoaMLExporter {

	/**
	 * Original model
	 */
	private Diagram diagram;

	/**
	 * Working directory where the export files are built. Contains as well the (copied) profile file.
	 * Deleted on exit.
	 */
	private File workingDir;
	private File profile = null;
	private String libDir = "/WEB-INF/lib/";
	private ResourceSetImpl resourceSet;
	
	private static final String soaMLProfile = "/WEB-INF/lib/SoaML.profile.xmi";
	

	private static final String DIAGRAM_NAME 			= "diagram_name";
	private static final String PARTICIPANT 			= "participant";
	private static final String PARTICIPANT_NAME 		= "participant_name";
	private static final String COLLABORATION_USE 		= "collaborationuse";
	private static final String COLLABORATION_USE_TYPE 	= "collaborationuse_type";
	private static final String SIMPLE_SERVICE_CONTRACT = "servicecontract";
	private static final String SERVICE_CONTRACT_NAME 	= "servicecontract_name";
	private static final String COMPOUND_SERVICE_CONTRACT = "compoundservicecontract";
	private static final String ROLE 					= "role";
	private static final String ROLE_NAME 				= "role_name";
	private static final String AGGREGATED_ROLE 		= "aggregatedrole";
	private static final String ROLE_BINDING 			= "rolebinding";
	private static final String ROLE_BINDING_ROLE 		= "rolebinding_role";

	// SoaML Stereotypes that we are using so far.
	private static final String SOAML_PACKAGE		 		= "SoaML::SoaMLPackage";
	private static final String SOAML_PARTICIPANT	 		= "SoaML::Participant";
	@SuppressWarnings("unused")
	private static final String SOAML_MESSAGE_TYPE	 		= "SoaML::MessageType";
	@SuppressWarnings("unused")
	private static final String SOAML_PROVIDER		 		= "SoaML::Provider";
	@SuppressWarnings("unused")
	private static final String SOAML_CONSUMER				= "SoaML::Consumer";
	private static final String SOAML_SERVICES_ARCHITECTURE = "SoaML::ServicesArchitecture";
	private static final String SOAML_SERVICE_CONTRACT 		= "SoaML::ServiceContract";
	private static final String SOAML_SERVICE_CHANNEL 		= "SoaML::ServiceChannel";
	@SuppressWarnings("unused")
	private static final String SOAML_SERVICE_INTERFACE 	= "SoaML::ServiceInterface";
	
	
	private class Traceability {
		Shape oryxShape;
		Element soamlElement;
		public Traceability (Shape shape) {
			oryxShape = shape;
		}
	}
	
	
	private boolean doDebug = false;
	
	HashMap<String, Traceability> transformedElements;
	

	/**
	 * Initialises the exporter class with a diagram. It is then only to call the different methods in order to export in the appropriate format.
	 * @param diagram 
	 * @param context servlet context in which the request is performed. Used notably to retrieve the file-path for the profile file.
	 */
	public BusinessSoaMLExporter (Diagram diagram, ServletContext context) {
		this.diagram = diagram;
		workingDir = new File (System.getProperty("java.io.tmpdir"));

		// This is the most simple way to parameterise a debug thing. No need to create a property file just for that.
		doDebug = (new File(context.getRealPath(libDir + "dodebug.touch")).exists());
		
		// update the libDir with the real path.
		libDir = (new File (context.getRealPath(libDir)).getAbsolutePath() + File.separatorChar);
		
		File origProfile = new File (context.getRealPath(soaMLProfile));
		profile = new File(workingDir.getAbsolutePath(), soaMLProfile.substring(soaMLProfile.lastIndexOf('/')));
		profile.deleteOnExit();

		debug("profile copied in " + workingDir.getAbsolutePath() + "\n\t=> " + profile.getAbsolutePath());
		try {
			// we do not check if the file already exists, as there's big chances it is the same file.
			FileUtils.copyFile(origProfile, profile);
		} catch (IOException e) {
			profile = null;		// way to tell the other methods that someting went wrong (shoudl not happen). They will throw FileNotFound.
			// We use an anonymous logger more for convenience purposes: this is the only place we'll use a logger.
			Logger.getAnonymousLogger().log(Level.SEVERE, "Oryx: BusinessSoaMLExporter: Could not copy the SoaML Profile.", e);
			return;
		}
		
		resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(XMI2UMLResource.FILE_EXTENSION, XMI2UMLResource.Factory.INSTANCE);

        // prepare for the profile application. The jar file contains the UML profile, this is why it needs to be loaded.

        URI baseURI = URI.createURI("jar:file:" + libDir + "org.eclipse.uml2.uml.resources_3.1.100.v201008191510.jar!/");

        Map<URI, URI> uriMap = resourceSet.getURIConverter().getURIMap();
        uriMap.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP), baseURI.appendSegment("libraries").appendSegment(""));
        uriMap.put(URI.createURI(UMLResource.METAMODELS_PATHMAP), baseURI.appendSegment("metamodels").appendSegment(""));
        uriMap.put(URI.createURI(UMLResource.PROFILES_PATHMAP), baseURI.appendSegment("profiles").appendSegment(""));
}
	
	private void debug (String str) {
		if (doDebug)
			System.out.print (str);
	}

	
	/**
	 * Returns the file that is used as the SoaML profile. It is a built-in file taken from Modelio v2.2.
	 * @return XMI file. It is a copy that is in the same directory as the model file. Can be modified.
	 * @throws FileNotFoundException (this should not happen: if raised, there was a problem during deployment).
	 */
	public File getSoaMLProfile () throws FileNotFoundException {
		if (profile == null)
			throw new FileNotFoundException ("SoaML Profile not found.");
		return profile;
	}
	
	/**
	 * exports the diagram to SoaML, by using the SoaML profile (built in file taken from Modelio v2.2). See {@link #getSoaMLProfile()}
	 * @return XMI file, created in a tmp directory. It should be deleted when not in use, even though it has deletedOnExit set.
	 * @throws IOException
	 * @throws FileNotFoundException when the profile file was not found.
	 */
	public File toSoaMLProfileXMI () throws IOException, FileNotFoundException {
		if (profile == null)
			throw new FileNotFoundException ("SoaML Profile not found.");

		debug("Diagram: " + diagram.getResourceId() + "\n\t{");
		for (Entry<String, String> propEntry : diagram.getProperties().entrySet()) {
			debug(propEntry.getKey() + "=" + propEntry.getValue() + " ");
		}
		debug ("}\n");
		if (!diagram.getChildShapes().isEmpty()) {
			debug ("\tchild shapes:\n");
			for (Shape child : diagram.getChildShapes()) {
				debugShape(child, "\t\t");
			}
		}

		transformedElements = new HashMap<String, BusinessSoaMLExporter.Traceability>();

		// 1. prepare the file, resources and model, apply the SoaML profile to the model.
		// The model name will be used as the filename.
        String modelName = (diagram != null ? diagram.getProperty(DIAGRAM_NAME) : null);
        if (modelName == null || modelName.length() == 0)
        	modelName = "model";

        File modelFile = createModelFile (modelName);
        debug ("Model file: " + modelFile.getAbsolutePath() + "\n");
        URI uri = URI.createURI("file:/" + modelFile.getAbsolutePath());
        Resource res = resourceSet.createResource(uri);

        Model model = UMLFactory.eINSTANCE.createModel();
        model.setName(modelName);        
        res.getContents().add(model);

        applySoaMLProfile(model);
        

        // 2. do the transformation:
        // - create a SoaML Package (will contain everything. Future version could have several packages)
        // - create a Services Architecture.
        // - look through elements in diagram, and create Participant, CollaborationUse, ServiceContracts.
        //   For each element (unless collaboration use which is created empty), setup the properties
        //   and create contained elements (i.e. roles, ...), but not references to other elements.
        // - look at collaboration uses and update the references to ServiceContracts, as well as role bindings.
		// A hash table stores traceability links between ORYX:Shapes and UML elements.

        org.eclipse.uml2.uml.Package pkg = UMLFactory.eINSTANCE.createPackage();
        model.getPackagedElements().add(pkg);
        pkg.setName(modelName);
        pkg.applyStereotype(pkg.getApplicableStereotype(SOAML_PACKAGE));

        debug ("created: Package " + stereo(pkg) + pkg.getName() + "\n");
        
        Collaboration servicesArchitecture = UMLFactory.eINSTANCE.createCollaboration();
		pkg.getPackagedElements().add(servicesArchitecture);
		servicesArchitecture.setName(modelName);
		servicesArchitecture.applyStereotype(servicesArchitecture.getApplicableStereotype(SOAML_SERVICES_ARCHITECTURE));

        debug ("created: SA " + servicesArchitecture + stereo(servicesArchitecture) + "\n");

		for (Shape shape : diagram.getChildShapes()) {
			debug ("--------  Current shape: ");
			debugShape(shape, "\t");
			Traceability tracelink = getTraceabilityLink (shape);
			debug ("\tracelink.soaML = " + tracelink.soamlElement + "\n");
			if (tracelink.soamlElement == null ) {
				// we have to create an element and add it.
				if (PARTICIPANT.equals(shape.getStencilId())) {
					// We create a Component  <<Participant>> that we include in the package.
					// We then link the shape to a Property that is typed by that component.					
					Component participant = createParticipant (pkg, shape);
					Property prop = UMLFactory.eINSTANCE.createProperty();
					tracelink.soamlElement = prop;
					prop.setAggregation(AggregationKind.COMPOSITE_LITERAL);	// modelio has it this way.
					prop.setName(shape.getProperty(PARTICIPANT_NAME).toLowerCase());
					prop.setType(participant);
					servicesArchitecture.getOwnedAttributes().add(prop);
					
					debug ("created Property " + prop + stereo(prop) + "\n");
				}
				else if (SIMPLE_SERVICE_CONTRACT.equals(shape.getStencilId())) {
					Collaboration serviceContract = createSimpleServiceContract (pkg, shape);
					tracelink.soamlElement = serviceContract;
					pkg.getPackagedElements().add(serviceContract);
				}
				else if (COMPOUND_SERVICE_CONTRACT.equals(shape.getStencilId())) {
					Collaboration serviceContract = createCompoundServiceContract (pkg, shape);
					tracelink.soamlElement = serviceContract;
					pkg.getPackagedElements().add(serviceContract);
				}
				else if (COLLABORATION_USE.equals(shape.getStencilId())) {
					CollaborationUse collaborationUse = UMLFactory.eINSTANCE.createCollaborationUse();
					tracelink.soamlElement = collaborationUse;
					servicesArchitecture.getCollaborationUses().add(collaborationUse);
					debug ("created CU " + collaborationUse + stereo(collaborationUse) + "\n");
				}
				// else: don't know what to do anyway.			
			}
		}
		
		// We look at collaboration uses and fix the type and role bindings.
		for (Traceability collabUseTL : transformedElements.values()) {
			if (! (collabUseTL.soamlElement instanceof CollaborationUse))
				continue;
			Shape shape = collabUseTL.oryxShape;
			CollaborationUse collabUse = (CollaborationUse) collabUseTL.soamlElement;
			debug ("-- Collaboration Use: " + shape.getStencilId() + ":" + shape.getResourceId() + "\n");

			mapCollaborationUseType (collabUse, shape);
			
			// We need to look for incomings and outgoings: role bindings can be in both.
			for (Shape inc : shape.getIncomings()) {
				debug ("  incoming: " + inc.getStencilId() + ":" + inc.getResourceId()+"\n");
				if (!ROLE_BINDING.equals(inc.getStencilId()))	// should not happen.
					continue;
				String oryxSupplierId = null;
				// with getIncoming, the role binding has the collaboration use as outgoing. => we need incoming.
				if (inc.getIncomings().size() > 0 && inc.getIncomings().get(0) != null)
					oryxSupplierId= inc.getIncomings().get(0).getResourceId();
				mapCollaborationUseRoleBinding(collabUse, oryxSupplierId, inc.getProperty(ROLE_BINDING_ROLE));
			}
			for (Shape out : shape.getOutgoings()) {
				debug ("  incoming: " + out.getStencilId() + ":" + out.getResourceId()+"\n");
				if (!ROLE_BINDING.equals(out.getStencilId()))	// should not happen.
					continue;
				String oryxSupplierId = null;
				// with getIncoming, the role binding has the collaboration use as incoming. => we need outgoings.
				if (out.getOutgoings().size() > 0 && out.getOutgoings().get(0) != null)
					oryxSupplierId= out.getOutgoings().get(0).getResourceId();
				mapCollaborationUseRoleBinding(collabUse, oryxSupplierId, out.getProperty(ROLE_BINDING_ROLE));
			}
		}
		

		debugModel(model);
		
		// 3. save the result and send it back.
		res.save(Collections.EMPTY_MAP);

		return modelFile;
	}

	/**
	 * Sets the type of the collaboration use so it maps the oryx shape.
	 * Does nothing if the type of oryxShape does not map to a UML element already transformed.
	 * @param collabUse
	 * @param oryxShape
	 */
	private void mapCollaborationUseType(CollaborationUse collabUse, Shape oryxShape) {
		Traceability tracelink = transformedElements.get(oryxShape.getProperty(COLLABORATION_USE_TYPE));
		if (tracelink == null || tracelink.soamlElement == null
			|| !(tracelink.soamlElement instanceof Collaboration)) { // all this should not happen
			return;
		}
		collabUse.setType((Collaboration)tracelink.soamlElement);
		if (((Collaboration)tracelink.soamlElement).getName() != null)
			collabUse.setName(collabUse.getType().getName().toLowerCase());
		debug ("-- \tset type: " + toStr(collabUse.getType()) + "\n");		
	}
	
	/**
	 * Maps the oryx role binding to the collaboration use.
	 * @param collabUse
	 * @param oryxSupplierId ID of the oryx shape that is linked to the (oryx) collaboration use.
	 * @param oryxclientId ID of the oryx shape that represents the role in the collaboration use that is used for role binding.
	 */
	private void mapCollaborationUseRoleBinding (CollaborationUse collabUse, String oryxSupplierId, String oryxClientId) {

		debug ("\tmapping role binding: " + toStr(collabUse) + " supplier: " + oryxSupplierId + " client: " + oryxClientId + "\n");
		if (oryxSupplierId == null || oryxClientId == null)
			return;

		
		Dependency soamlRoleBinding = UMLFactory.eINSTANCE.createDependency();
		collabUse.getRoleBindings().add(soamlRoleBinding);
		soamlRoleBinding.getClients().add(collabUse);	// this is what Modelio is doing.

		
		// handle the supplier
		Traceability tlSupplier = transformedElements.get(oryxSupplierId);
		if (tlSupplier == null || tlSupplier.soamlElement == null
				|| !(tlSupplier.soamlElement instanceof NamedElement)) {
			debug ("-- \t\t=> ERROR found " + (tlSupplier == null?"NULL":tlSupplier.soamlElement)+"\n");
		}
		else {
			debug ("\t\tSupplier: " + toStr((NamedElement)tlSupplier.soamlElement) + "\n");
			soamlRoleBinding.getSuppliers().add((NamedElement)tlSupplier.soamlElement);
		}
		
		Traceability tlClient  = transformedElements.get(oryxClientId);
		if (tlClient == null || tlClient.soamlElement == null
				|| !(tlClient.soamlElement instanceof NamedElement)) {
			debug ("-- \t\t=> ERROR found " + (tlClient == null?"NULL":tlClient.soamlElement)+"\n");
		}
		else {
			debug ("\t\tClient: " + toStr((NamedElement)tlClient.soamlElement) + "\n");
			soamlRoleBinding.getClients().add((NamedElement)tlClient.soamlElement);			
		}
		
		
		debug ("-- Created dependency " + soamlRoleBinding);
		debug ("  Clt: " + soamlRoleBinding.getClients());
		debug ("  Sup: " + soamlRoleBinding.getSuppliers() + "\n");		
	}
	
	
	/**
	 * Creates a service contract (incl. stereotype), with the 2 roles and service channel between them
	 * Interfaces of roles are not handled yet.
	 * @param shape
	 * @return
	 */
	private Collaboration createSimpleServiceContract(org.eclipse.uml2.uml.Package containerPackage, Shape shape) {
		debug ("creating SC " + "\n");
		Collaboration result = UMLFactory.eINSTANCE.createCollaboration();
		containerPackage.getPackagedElements().add(result);

		result.applyStereotype(result.getApplicableStereotype(SOAML_SERVICE_CONTRACT));
		
		if (shape.getProperty(SERVICE_CONTRACT_NAME) == null)
			result.setName("");
		else
			result.setName(shape.getProperty(SERVICE_CONTRACT_NAME));
		
		// get the roles and connect them with a channel.
		Property r1 = null, r2 = null;
		for (Shape role : shape.getChildShapes()) {
			if (ROLE.equals(role.getStencilId())) {
				Traceability tracelink = getTraceabilityLink(role);
				if (tracelink.soamlElement == null) {
					Property myrole = createRole (role);
					tracelink.soamlElement = myrole;
					result.getOwnedAttributes().add(myrole);
					if (r1 == null) r1 = myrole;
					else if (r2 == null) r2 = myrole;
				}
				// else: it should not happen. 
			}
		}
		// Only 2 roles in a channel. Roles were newly created so there's no channels.
		if (r1 != null && r2 != null) {
			Connector connector = createServiceChannel (result, r1, r2);
			result.getOwnedConnectors().add(connector);
		}
		debug ("created CA " + result + stereo(result) + "\n");
		debug ("\t" + result.getOwnedAttributes() + "\n");
		debug ("\t" + result.getOwnedConnectors() + "\n");
		return result;
	}
	
	/**
	 * Creates a ServiceChannel and connects the two roles.
	 * @param r1
	 * @param r2
	 * @return
	 */
	private Connector createServiceChannel(StructuredClassifier container, Property r1, Property r2) {
		Connector result = UMLFactory.eINSTANCE.createConnector();
		container.getOwnedConnectors().add(result);
		result.applyStereotype(result.getApplicableStereotype(SOAML_SERVICE_CHANNEL));

		ConnectorEnd connectorEnd = UMLFactory.eINSTANCE.createConnectorEnd();
		connectorEnd.setRole(r1);
		result.getEnds().add(connectorEnd);
		connectorEnd = UMLFactory.eINSTANCE.createConnectorEnd();
		connectorEnd.setRole(r2);
		result.getEnds().add(connectorEnd);
		debug ("created ServiceChannel " + result + stereo(result) + " - " + result.getEnds() + "\n");
		return result;
	}
	
	/**
	 * Creates a role (from simple / aggregated one)
	 * TODO: handle service interfaces.
	 * @param role2
	 * @return
	 */
	private Property createRole(Shape role2) {
		Property result = UMLFactory.eINSTANCE.createProperty();
		result.setName(role2.getProperty(ROLE_NAME));
		result.setAggregation(AggregationKind.COMPOSITE_LITERAL);	// modelio has it this way.
		debug("created property (role) " + result + stereo(result) + "\n");
		return result;
	}
	
	/**
	 * Creates a Participant (component <<Participant>>)
	 * @param shape
	 * @return
	 */
	private Component createParticipant(org.eclipse.uml2.uml.Package containerPackage, Shape shape) {
		Component result = UMLFactory.eINSTANCE.createComponent();
		containerPackage.getPackagedElements().add(result);
		result.applyStereotype(result.getApplicableStereotype(SOAML_PARTICIPANT));
		result.setName(shape.getProperty(PARTICIPANT_NAME));
		debug ("created Participant: " +  result + stereo(result) + "\n");
		return result;
	}

	/**
	 * Creates a (compound) service contract. The code is very similar to createSimpleServiceContract:
	 * there might be a way to combine the two.
	 * @param shape
	 * @return
	 */
	private Collaboration createCompoundServiceContract(org.eclipse.uml2.uml.Package containerPackage, Shape shape) {
		debug ("creating SC compound\n");
		Collaboration result = UMLFactory.eINSTANCE.createCollaboration();
		containerPackage.getPackagedElements().add(result);
		result.applyStereotype(result.getApplicableStereotype(SOAML_SERVICE_CONTRACT));
		
		if (shape.getProperty(SERVICE_CONTRACT_NAME) == null)
			result.setName("");
		else
			result.setName(shape.getProperty(SERVICE_CONTRACT_NAME));
		
		for (Shape childShape : shape.getChildShapes()) {
			if (AGGREGATED_ROLE.equals(childShape.getStencilId())) {
				Traceability tracelink = getTraceabilityLink(childShape);
				if (tracelink.soamlElement == null) {
					Property myrole = createRole (childShape);
					tracelink.soamlElement = myrole;
					result.getOwnedAttributes().add(myrole);
				}
			}
			else if (COLLABORATION_USE.equals(childShape.getStencilId())) {
				Traceability tracelink = getTraceabilityLink(childShape);
				if (tracelink.soamlElement == null) {
					CollaborationUse collaborationUse = UMLFactory.eINSTANCE.createCollaborationUse();
					tracelink.soamlElement = collaborationUse;
					result.getCollaborationUses().add(collaborationUse);
					debug ("created CU " + collaborationUse + stereo(collaborationUse) + "\n");
				}
			}
		}
		debug ("created CompSC " + result + stereo(result) + "\n");
		debug ("\t" + result.getOwnedAttributes() + "\n");
		debug ("\t" + result.getOwnedConnectors() + "\n");

		return result;
	}

	
	/**
	 * Gets the traceability link related to the given element.
	 * If none found (=not transformed yet), then we create the traceability link
	 * with a null soamlElement.
	 * @param shape
	 * @return
	 */
	private Traceability getTraceabilityLink(Shape shape) {
		Traceability result = transformedElements.get(shape.getResourceId());
		if (result == null) {
			result = new Traceability(shape);
			transformedElements.put(shape.getResourceId(), result);
		}
		return result;
	}
	
	
	/**
	 * Creates a unique file in the working directory (temp dir).
	 * The name of the file is taken from the diagram name (if any), or "model" if no such name exists.
	 * @param modelname used to create the filename.
	 * @return
	 */
	private File createModelFile(String modelname) {
		// creates a unique file. We do this in case there exists already something (contrary to the profile, there's more chances as the user is also giving the filename)

		String extension = "xmi";
		File tmpFile = new File (workingDir, modelname + "." + extension);
		int i = 0;
		while (tmpFile.exists()) {
			i++;
			tmpFile = new File (workingDir, modelname + "-" + i + "." + extension);
		}
		tmpFile.deleteOnExit();
		return tmpFile;
	}
	
	/**
	 * Applies the SoaML profile to the model.
	 * @param model
	 */
	private void applySoaMLProfile (Model model) {
		if (profile == null)	// safety. It should not be null at this moment.
			return;	

        URI uriProf = URI.createURI("file:/" + profile.getAbsolutePath());
        Resource resProf = resourceSet.getResource(uriProf, true);              
        Profile prof = (Profile) resProf.getContents().get(0);

        model.applyProfile(prof);
	}
	
	////////////////////// debugging functions
	
	private void debugShape (Shape s, String indent) {

		debug (indent + s.getStencilId() + ":" +  s.getResourceId() + " {");
		for (Entry<String, String> propEntry : s.getProperties().entrySet()) {
			debug (propEntry.getKey() + "=" + propEntry.getValue() + " ");
		}
		debug ("}\n");
		if (!s.getIncomings().isEmpty()) {
			debug (indent + "  incomings " + debugResourceIds(s.getIncomings()) + "\n");
		}
		if (!s.getOutgoings().isEmpty()) {
			debug (indent + "  outgoings " + debugResourceIds(s.getOutgoings()) + "\n");
		}
		if (!s.getChildShapes().isEmpty()) {
			debug (indent + "  child shapes:\n");
			for (Shape child : s.getChildShapes()) {
				debugShape(child, indent + "\t");
			}
		}
	}
	

	private String toStr (NamedElement elt) {
		if (elt == null)
			return "NULL";
		return "'" + elt.getName() + "'[" + Integer.toHexString(elt.hashCode()) + "]" + stereo(elt);
	}
	private void debugModel (Model m) {
		debug ("***** Model " + toStr(m) + " ****\n");
		for (PackageableElement pkg : m.getPackagedElements()) {
			debug ("Package " + toStr(pkg) + "\n");
			if (pkg instanceof org.eclipse.uml2.uml.Package) {
				for (PackageableElement elt : ((org.eclipse.uml2.uml.Package)pkg).getPackagedElements()) {
					if (elt instanceof Component) {
						debug ("\tComponent " + toStr(elt) + "\n");
					}
					else if (elt instanceof Collaboration) {
						debug ("\tCollaboration " + toStr(elt) + "\n");
						for (Property prop : ((Collaboration)elt).getOwnedAttributes()) {
							debug ("\t\tprop " + toStr(prop) + " type="
									+ (prop.getType() != null ? toStr(prop.getType()) : "null") + "\n");
						}
						for (Connector conn : ((Collaboration)elt).getOwnedConnectors()) {
							debug  ("\t\tConnector " + toStr(conn) + "\n");
							for (ConnectorEnd ce : conn.getEnds()) {
								debug ("\t\t\tConnEnd: " + toStr (ce.getRole()) + "\n");
							}
						}
						for (CollaborationUse cu : ((Collaboration)elt).getCollaborationUses()) {
							debug ("\t\tCollabUse: " + toStr(cu) + "\n");
							debug ("\t\t\tType=" + toStr (cu.getType()) + "\n");
							debug ("\t\t\tRoleBindings:\n");
							for (Dependency dep : cu.getRoleBindings()) {
								debug ("\t\t\t\t Clients {");
								for (NamedElement ne : dep.getClients()) {
									debug (toStr(ne) + " ");
								}
								debug ("}\n\t\t\t\t Supplier {" );
								for (NamedElement ne : dep.getSuppliers()) {
									debug (toStr(ne) + " ");
								}
								debug ("}\n");
							}
						}
					}
					else {
						debug ("Unknown elemeent " + elt + "\n");
					}
				}
			}
			else {
				debug ("Not a Package???\n");
			}
		}
		debug ("**************\n");
	}

	
	
	private String debugResourceIds (ArrayList<Shape> list) {
		String result = "{";
		for (Shape s : list)
			result += s.getStencilId() + ":" + s.getResourceId() + ", ";
		if (result.length() > 1)
			result = result.substring(0, result.length()-2);
		return result + "}";
	}

	private String stereo (Element e) {
		if (!e.getAppliedStereotypes().isEmpty()) {
			String result = "<<";
			for (Stereotype s : e.getAppliedStereotypes())
				result += s.getName() + " ";
			return result.substring(0, result.length()-1) + ">>";
		}
		else
			return "";
	}

}
