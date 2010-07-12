/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.hpi.nunet;

import java.util.ArrayList;
import java.util.List;


public class FlowRelationship extends de.hpi.petrinet.FlowRelationship {

	protected Node source;
	protected Node target;
	protected List<String> variables;
	
	public Node getSource() {
		return this.source;
	}

	public void setSource(Node value) {
		if (source != null)
			source.getOutgoingFlowRelationships().remove(this);
		source = value;
		if (source != null)
			source.getOutgoingFlowRelationships().add(this);
	}

	public Node getTarget() {
		return this.target;
	}

	public void setTarget(Node value) {
		if (target != null)
			target.getIncomingFlowRelationships().remove(this);
		target = value;
		if (target != null)
			target.getIncomingFlowRelationships().add(this);
	}

	public List<String> getVariables() {
		if (variables == null)
			variables = new ArrayList<String>();
		return variables;
	}
	
	public String toString() {
		return "(("+source+", "+target+"), "+getVariables()+")";
	}

} // FlowRelationship