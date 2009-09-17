package de.hpi.bpmn2_0.factory;

/**
 * Copyright (c) 2009
 * Philipp Giese, Sven Wagner-Boysen
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

import org.oryxeditor.server.diagram.Shape;

import de.hpi.bpmn2_0.model.BaseElement;

/**
 * This is the abstract factory that offers methods to create a process 
 * element and a related diagram element from a {@link Shape}.
 */
public abstract class AbstractBpmnFactory {
	
	/**
	 * Creates a process element based on a {@link Shape}.
	 * 
	 * @param shape
	 * 		The resource shape
	 * @return
	 * 		The constructed process element.
	 */
	protected abstract BaseElement createProcessElement(Shape shape);
	
	/**
	 * Creates a diagram element based on a {@link Shape}.
	 * 
	 * @param shape
	 * 		The resource shape
	 * @return
	 * 		The constructed process element.
	 */
	protected abstract Object createDiagramElement(Shape shape);
	
	/**
	 * Creates BPMNElement that contains DiagramElement and ProcessElement
	 * 
	 * @param shape
	 * 		The resource shape.
	 * @return
	 * 		The constructed BPMN element.
	 */
	public abstract BPMNElement createBpmnElement(Shape shape);
}
