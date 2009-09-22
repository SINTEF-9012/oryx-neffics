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
package de.hpi.bpmn2_0.factory;

import org.oryxeditor.server.diagram.Shape;

import de.hpi.bpmn2_0.exceptions.BpmnConverterException;
import de.hpi.bpmn2_0.factory.annotations.StencilId;
import de.hpi.bpmn2_0.model.BaseElement;
import de.hpi.bpmn2_0.model.data_object.DataInput;
import de.hpi.bpmn2_0.model.data_object.DataObject;
import de.hpi.bpmn2_0.model.data_object.DataOutput;
import de.hpi.bpmn2_0.model.diagram.BpmnNode;
import de.hpi.bpmn2_0.model.diagram.DataInputShape;
import de.hpi.bpmn2_0.model.diagram.DataObjectShape;
import de.hpi.bpmn2_0.model.diagram.DataOutputShape;

/**
 * Factory for Data Objects
 * 
 * @author Philipp Giese
 * @author Sven Wagner-Boysen
 *
 */
@StencilId({
	"DataObject"
})
public class DataObjectFactory extends AbstractBpmnFactory {

	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createBpmnElement(org.oryxeditor.server.diagram.Shape, de.hpi.bpmn2_0.factory.BPMNElement)
	 */
	@Override
	public BPMNElement createBpmnElement(Shape shape, BPMNElement parent)
			throws BpmnConverterException {
	
		BpmnNode dataShape = (BpmnNode) this.createDiagramElement(shape);
		BaseElement data = this.createProcessElement(shape);
		
		if(data instanceof DataObject)
			((DataObjectShape) dataShape).setDataObjectRef((DataObject) data);
		else if(data instanceof DataInput)
			((DataInputShape) dataShape).setDataInputRef((DataInput) data);
		else if(data instanceof DataOutput)
			((DataOutputShape) dataShape).setDataOutputRef((DataOutput) data);
		
		return new BPMNElement(dataShape, data, shape.getResourceId());		
	}

	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createDiagramElement(org.oryxeditor.server.diagram.Shape)
	 */
	@Override
	protected Object createDiagramElement(Shape shape) {
		String prop = shape.getProperty("input_output");
		
		BpmnNode dataObjectShape = null;
		
		if(prop.equals("None")) {
			dataObjectShape = new DataObjectShape();
		} else if(prop.equals("Input")) {
			dataObjectShape = new DataInputShape();
		} else if(prop.equals("Output")) {
			dataObjectShape = new DataOutputShape();
		}
		
		if(dataObjectShape != null)
			this.setVisualAttributes(dataObjectShape, shape);
		
		return dataObjectShape;
	}

	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createProcessElement(org.oryxeditor.server.diagram.Shape)
	 */
	@Override
	protected BaseElement createProcessElement(Shape shape)
			throws BpmnConverterException {
		
		String prop = shape.getProperty("input_output");
		
		BaseElement dataObjectShape = null;
		
		if(prop.equals("None")) {
			dataObjectShape = new DataObject();
			((DataObject) dataObjectShape).setName(shape.getProperty("name"));
		} else if(prop.equals("Input")) {
			dataObjectShape = new DataInput();
			((DataInput) dataObjectShape).setName(shape.getProperty("name"));
		} else if(prop.equals("Output")) {
			dataObjectShape = new DataOutput();
			((DataOutput) dataObjectShape).setName(shape.getProperty("name"));
		}
		
		dataObjectShape.setId(shape.getResourceId());		
		
		return dataObjectShape;
	}

}
