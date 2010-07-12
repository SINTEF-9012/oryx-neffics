package de.hpi.diagram.reachability;

import de.hpi.bpt.hypergraph.abs.Vertex;

public class ReachabilityNode<Marking> extends Vertex {
	protected Marking marking;
	
	public ReachabilityNode(Marking m){
		marking = m;
	}
	
	public boolean equals(Object o){
		//System.out.println("Calling MarkingNode#equals");
		if(o instanceof ReachabilityNode<?>){
			return marking.equals(((ReachabilityNode<?>)o).getMarking());
		}
		return false;
	}

	public Marking getMarking() {
		return marking;
	}

	public void setMarking(Marking marking) {
		this.marking = marking;
	}
}