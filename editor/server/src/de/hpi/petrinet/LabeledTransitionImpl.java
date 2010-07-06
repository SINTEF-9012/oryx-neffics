package de.hpi.petrinet;

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
public class LabeledTransitionImpl extends NodeImpl implements LabeledTransition {

	protected String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isSimilarTo(Node node) {
		if (node instanceof LabeledTransition && getLabel() != null) {
			return (getLabel().equals(((LabeledTransition)node).getLabel()));
		}
		return false;
	}
	
	public String toString() {
		return getId()+"("+getLabel()+")";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		LabeledTransition clone = (LabeledTransitionImpl) super.clone();
		if (this.getLabel() != null)
			clone.setLabel(new String(this.getLabel()));
		return clone;
	}
	
	/*
	 * Add communication channels
	 */
	private String comChan = "";
	private CommunicationType comType = CommunicationType.DEFAULT;
	@Override
	public String getCommunicationChannel() {
		return this.comChan;
	}

	@Override
	public CommunicationType getCommunicationType() {
		return this.comType;
	}

	@Override
	public void setCommunicationChannel(String channel) {
		this.comChan =channel;

	}

	@Override
	public void setCommunicationType(CommunicationType type) {
		this.comType =type;

	}

}


