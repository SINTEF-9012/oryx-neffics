package org.oryxeditor.server;

/**
 * Copyright (c) 2012 
 * 
 * Cyril Carrez (SINTEF)
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import no.sintef.businessSoaML.BusinessSoaMLExporter;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.DiagramBuilder;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import de.hpi.bpmn2_0.ExportValidationEventCollector;
import de.hpi.bpmn2_0.factory.AbstractBpmnFactory;
import de.hpi.bpmn2_0.model.Definitions;
import de.hpi.bpmn2_0.transformation.BPMNPrefixMapper;
import de.hpi.bpmn2_0.transformation.Diagram2BpmnConverter;
import de.hpi.util.reflection.ClassFinder;

/**
 * This servlet provides the access point to the exportation of Business SoaML to SoaML (XMI) with the SoaML Profile.
 * 
 * See also the plugin business_soaml.js for an explanation how we communicate with the browser (ORYX sends the result 3 times
 * between the browser and the server).
 * 
 * @author Cyril Carrez
 * 
 */
public class BusinessSoaMLSerialization extends HttpServlet {

	private static final long serialVersionUID = 7210249544379576019L;

	private static final int BUFFER_SIZE = 2048;	// used when zipping the files together.

    /**
     * The post request
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
    	
    	String json = req.getParameter("data");

        /* Transform and return from DI */
        try {
            // following code taken from MultiDownloader.prepareHeader:
            res.setHeader("Pragma", "public");
        	res.setHeader("Expires", "0");
        	res.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        	res.addHeader("Cache-Control", "private");
        	res.setHeader("Content-Transfer-Encoding", "binary");
        	res.setHeader("Content-Type", "application/zip");

            res.setStatus(200);
            this.performTransformationToSoaML(json, res);
        } catch (Exception e) {
            try {
                e.printStackTrace();
                res.setStatus(500);
                res.setContentType("text/plain");
                String errorMessage = "Internal server error 500: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                res.getWriter().write(errorMessage);
                Logger.getAnonymousLogger().log(Level.SEVERE, "Sending error: " + errorMessage);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * Triggers the transformation from Diagram to SoaML (XMI). On success, builds a zip file that contains the result and the SoaML profile.
     * 
     * @param json The business-soaml diagram in JSON format
     * @param output the output stream where the (zipped) files will be produced.
     * @throws JSONException 
     * @throws IOException 
     */
    protected void performTransformationToSoaML(String json, HttpServletResponse res) throws JSONException, IOException, FileNotFoundException {
        /* Retrieve diagram model from JSON */
    	Diagram diagram = DiagramBuilder.parseJson(json);

    	BusinessSoaMLExporter soaMLExporter = new BusinessSoaMLExporter(diagram, this.getServletContext());
    	
    	// 1. get the result of the export
    	// 2. copy the files as necessary
    	// 3. zip the 2 files, put the result in the writer (to be returned)
    	// 4. delete the files
    	
    	File[] files = new File[2];

        files[0] = soaMLExporter.toSoaMLProfileXMI();
        files[1] = soaMLExporter.getSoaMLProfile();
        // build the zip filename from the model filename.
    	res.setHeader("Content-Disposition", "attachment; filename=\"" + files[0].getName().substring(0, files[0].getName().length()-3) + "zip\"");

         // we zip the two files. The following code could be moved to a specific class.
        // Note that Oryx has a MultiDownloader that has some inputs there
        // (browser sends several (text) files in a form and receives a zip that the user can download).
        ZipOutputStream out = new ZipOutputStream(res.getOutputStream());

        for (File f : files) {
        	ZipEntry entry = new ZipEntry(f.getName());
        
        	out.putNextEntry(entry);
        
        	BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE);

        	int count;
        	byte data[] = new byte[BUFFER_SIZE];
        	while((count = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                 out.write(data, 0, count);
        	}
        	out.closeEntry();
        }
        out.close();
        
        for (File f : files)
        	f.delete();
    }
    
    
}