package de.hpi.petrinet;

import java.util.ArrayList;
import java.util.List;

import de.hpi.util.Bounds;

/**
 * Copyright (c) 2008 Gero Decker
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
public class NodeImpl implements Node {

	protected String id;
	private List<FlowRelationship> incomingFlowRelationships;
	private List<FlowRelationship> outgoingFlowRelationships;
	private Bounds bounds;
	protected String resourceId;

	public String getId() {
		return id;
	}

	public void setId(String label) {
		if (label != null && label.indexOf('#') > -1) {
			label = label.replace("#","");
			if (this instanceof Place) {
				label = "place_" + label; 
			} else if (this instanceof Transition) {
				label = "transition_" + label;
			}
		}
		
		this.id = label;
	}

	public List<FlowRelationship> getIncomingFlowRelationships() {
		if (incomingFlowRelationships == null)
			incomingFlowRelationships = new ArrayList<FlowRelationship>();
		return incomingFlowRelationships;
	}

	public List<FlowRelationship> getOutgoingFlowRelationships() {
		if (outgoingFlowRelationships == null)
			outgoingFlowRelationships = new ArrayList<FlowRelationship>();
		return outgoingFlowRelationships;
	}

	public String toString() {
		return getId();
	}

	public Bounds getBounds() {
		return bounds;
	}

	public void setBounds(Bounds bounds) {
		this.bounds = bounds;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
		
	@Override
	public Object clone() throws CloneNotSupportedException {
		Node n = (Node) super.clone();
		
		n.setIncomingFlowRelationships(new ArrayList<FlowRelationship>());
		n.setOutgoingFlowRelationships(new ArrayList<FlowRelationship>());
		
		if (this.getId() != null)
			n.setId(new String(this.id));
		
		if (this.getResourceId() != null)
			n.setResourceId(new String(this.resourceId));
		
		if (this.getBounds() != null)
			n.setBounds((Bounds) this.getBounds().clone());
		return n;
	}
	
	public List<Node> getPrecedingNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for(FlowRelationship f : this.getIncomingFlowRelationships()) {
			nodes.add(f.getSource());
		}
		return nodes;
	}
	
	public List<Node> getSucceedingNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for(FlowRelationship f : this.getOutgoingFlowRelationships()) {
			nodes.add(f.getTarget());
		}
		return nodes;
	}

	public void setIncomingFlowRelationships(
			List<FlowRelationship> incomingFlowRelationships) {
		this.incomingFlowRelationships = incomingFlowRelationships;
	}

	public void setOutgoingFlowRelationships(
			List<FlowRelationship> outgoingFlowRelationships) {
		this.outgoingFlowRelationships = outgoingFlowRelationships;
	}


}


