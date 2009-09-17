package org.oryxeditor.server;

/**
 * Copyright (c) 2009 
 * 
 * Sven Wagner-Boysen
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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.json.JSONException;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;

import de.hpi.bpmn2_0.model.Definitions;
import de.hpi.bpmn2_0.transformation.Diagram2BpmnConverter;

/**
 * This servlet provides the access point to the interchange format of BPMN 2.0
 * 
 * @author Sven Wagner-Boysen
 *
 */
public class Bpmn2_0Servlet extends HttpServlet {

	private static final long serialVersionUID = -4308758083419724953L;
	
	/**
	 * The post request
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		res.setContentType("application/xhtml");
		String json = req.getParameter("data");
		
		/* Transform and return from DI */
		try {
			this.performTransformationToDi(json, res.getWriter());
		} catch (Exception e) {
			try {
				res.sendError(500, e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		
		
	}
	
	/**
	 * Triggers the transformation from Diagram to BPMN model and writes the 
	 * resulting BPMN XML on success.
	 * 
	 * @param json
	 * 		The diagram in JSON format
	 * @param writer
	 * 		The HTTP-response writer
	 * @throws Exception
	 * 		Exception occurred while processing
	 */
	protected void performTransformationToDi(String json, PrintWriter writer) throws Exception {
		/* Retrieve diagram model from JSON */
	
		Diagram diagram = DiagramBuilder.parseJson(json);
			
		/* Build up BPMN 2.0 model */
		Diagram2BpmnConverter converter = new Diagram2BpmnConverter(diagram);
		Definitions bpmnDefinitions = converter.getDefinitionsFormDiagram();
		
		/* Perform XML creation */
		JAXBContext context = JAXBContext.newInstance(Definitions.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(bpmnDefinitions, writer);
		
	}

}
