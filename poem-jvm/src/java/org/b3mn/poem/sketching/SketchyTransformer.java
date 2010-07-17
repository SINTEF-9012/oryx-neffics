/***************************************
 * Copyright (c) 2008
 * Helen Kaltegaertner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************/

package org.b3mn.poem.sketching;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGSyntax;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

public class SketchyTransformer extends PDFTranscoder{

	private SVGDocument doc;
	private SVGSVGElement root;
	private SVGGeneratorContext ctx;
	private CSSStyleHandler styleHandler;
	private CDATASection styleSheet; 
	private String fontSize = "15px";
	private boolean createPDF;
	private OutputStream out;

	public SketchyTransformer(InputStream in, OutputStream out, boolean createPDF) {
		try {
			this.createPDF = createPDF;
			this.out = out;
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			//this.doc = f.createSVGDocument(svg);
			this.doc = f.createSVGDocument(null, in);
			this.root = (SVGSVGElement) this.doc.getDocumentElement();

			this.ctx = SVGGeneratorContext.createDefault(this.doc);
			this.ctx.setEmbeddedFontsOn(true);
			this.styleSheet = this.doc.createCDATASection("");
			this.styleHandler = new CSSStyleHandler(this.styleSheet, this.ctx);
			this.ctx.setStyleHandler(this.styleHandler);

		} catch (IOException e) {
			// TODO: handle exception
		}
		
		this.createArrowEnd();
	}

	public SVGDocument getDoc() {
		return doc;
	}

	public void setDoc(SVGDocument doc) {
		this.doc = doc;
	}

	public SVGSVGElement getRoot() {
		return root;
	}

	public void setRoot(SVGSVGElement root) {
		this.root = root;
	}
	
	public SVGGeneratorContext getCtx() {
		return ctx;
	}

	public void setCtx(SVGGeneratorContext ctx) {
		this.ctx = ctx;
	}

	public CSSStyleHandler getStyleHandler() {
		return styleHandler;
	}

	public void setStyleHandler(CSSStyleHandler styleHandler) {
		this.styleHandler = styleHandler;
	}

	public CDATASection getStyleSheet() {
		return styleSheet;
	}

	public void setStyleSheet(CDATASection styleSheet) {
		this.styleSheet = styleSheet;
	}
	
	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isCreatePDF() {
		return createPDF;
	}

	public void setCreatePDF(boolean createPDF) {
		this.createPDF = createPDF;
	}
	
	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public void transform() throws IOException, TranscoderException{
		this.transformPaths();
		this.transformRectangles();
		this.transformEllipses();
		this.setFont("PapaMano AOE", "18px");
		//this.setFont("Helenas Hand", "18px");
		this.produceOutput();
	}

	public void produceOutput() throws IOException, TranscoderException{
		this.applyStyles();

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(this.doc);
		boolean useCSS = true;
		boolean escaped = false;
		// Setup output
		OutputStream dest;
		if (this.createPDF)
			dest = new ByteArrayOutputStream();
		else 
			dest = new FileOutputStream("outcome.svg");
		Writer outWriter = new OutputStreamWriter(dest, "UTF-8");
		
		try {
			// stream to output
			svgGenerator.stream(this.root, outWriter, useCSS, escaped);
			outWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.createPDF)
			this.exportPdf( ((ByteArrayOutputStream)dest).toByteArray());
	}
	
	private void exportPdf(byte[] svg) throws IOException, TranscoderException{		
		
		// required for pdf creation otherwise default font-size is selected
		NodeList texts = this.doc.getElementsByTagName("tspan");
		for (int i = 0; i < texts.getLength(); i++)
			((Element)texts.item(i)).setAttribute("font-size", "15px");
		
		PDFTranscoder transcoder = new PDFTranscoder();
		try {
			// setup input
			InputStream in 			= new ByteArrayInputStream(svg);
			TranscoderInput input 	= new TranscoderInput(in);
	    	//Setup output
			TranscoderOutput output	= new TranscoderOutput(this.out); 
			
	    	try {
		    	// apply transformation
				transcoder.transcode(input, output);
	    	} finally {
	    		this.out.close();
				in.close();
	    	}
		} finally {}
	}

	private void applyStyles() {
		//this.doc.normalizeDocument();
		// append CDATASection for CSS styles
		Element defs = (Element)this.root.getElementsByTagName("defs").item(0);
		Element style = this.doc.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG);
		style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
		style.appendChild(this.styleSheet);
		defs.appendChild(style);
	}
	
	private void createArrowEnd(){
		
		Element marker = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "marker");
		marker.setAttribute("id", "oryx_arrow");
		marker.setAttribute("refX", "7");
		marker.setAttribute("refY", "6");
		marker.setAttribute("markerWidth", "7");
		marker.setAttribute("markerHeight", "12");
		marker.setAttribute("orient", "auto");
		
		Element path = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
		path.setAttribute("d", "M 0 0 L 7 6 L 0 10");
		
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("fill", "none");
		styles.put("stroke", "black");
		styles.put("stroke-width", "1");
		this.styleHandler.setStyle(path, styles);
		
		marker.appendChild(path);
		
		this.root.getFirstChild().appendChild(marker);

	}
	
	public void transformPaths() {
		NodeList paths = this.doc.getElementsByTagName("path");
		for (int i = 0; i < paths.getLength(); i++) {
			Element e = (Element) paths.item(i);
			if (!((Element) e.getParentNode()).getAttribute("display").equals("none")
					&& !e.getParentNode().getNodeName().equals("marker")
					&& !((Element)e.getParentNode()).hasAttribute("oryx:anchors")) {
				
				SVGPath path = new SVGPath(e, this.doc, this.styleHandler);
				try {
					path.transform();
				} catch (SketchyException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public void transformRectangles() {
		NodeList rectangles = this.doc.getElementsByTagName("rect");
		HashMap<Element, Element> replaceMap = new HashMap<Element, Element>();

		for (int i = 0; i < rectangles.getLength(); i++) {
			Element e = (Element) rectangles.item(i);
			SVGRectangle rect;
			
			if ( ((Element)e.getParentNode()).getAttribute("title").contains("Data Object") ){
				rect = new SVGRectangle(e, this.doc, this.styleHandler, true);
			}
			else if (e.getAttribute("stroke").equals("none"))
				continue;
			else {
				rect = new SVGRectangle(e, this.doc, this.styleHandler, false);
			}
			replaceMap.put(e, rect.transform());
		}

		// replace rectangles by sketchy paths
		for (Element rect : replaceMap.keySet()){
			if (replaceMap.get(rect) != null)
				rect.getParentNode().replaceChild(replaceMap.get(rect), rect);
		}
	}
	
	public void transformEllipses() {
		HashMap<Element, ArrayList<Element>> replaceMap = new HashMap<Element, ArrayList<Element>>();
		
		NodeList circles = this.doc.getElementsByTagName("circle");
		for (int i = 0; i < circles.getLength(); i++) {
			Element e = (Element) circles.item(i);
			if (e.getAttribute("stroke").equals("none"))
				continue;
			SVGEllipse ellipse = new SVGEllipse(e, this.doc, this.styleHandler);
			replaceMap.put(e, ellipse.transform());
		}
		
		NodeList ellipses = this.doc.getElementsByTagName("ellipse");
		for (int i = 0; i < ellipses.getLength(); i++) {
			Element e = (Element) ellipses.item(i);
			if (e.getAttribute("stroke").equals("none"))
				continue;
			SVGEllipse ellipse = new SVGEllipse(e, this.doc, this.styleHandler);
			replaceMap.put(e, ellipse.transform());
		}

		// replace rectangles by sketchy paths
		for (Element ellipse : replaceMap.keySet()){
			if (replaceMap.get(ellipse) != null){
				// insert stroke before old element
				ellipse.getParentNode().insertBefore(replaceMap.get(ellipse).get(1), ellipse);
				// replce old element by new ellipse
				ellipse.getParentNode().replaceChild(replaceMap.get(ellipse).get(0), ellipse);
			}
		}
	}

	public void setFont(String font, String size) {
		
		this.fontSize = size;
		((Element)this.root.getLastChild()).setAttribute("font-family", font);
		((Element)this.root.getLastChild()).setAttribute("font-size", size);
		
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("font-size", size);
		
		if (this.createPDF) {
			NodeList texts = this.doc.getElementsByTagName("tspan");
			for (int i = 0; i < texts.getLength(); i++)
				this.styleHandler.setStyle((Element) texts.item(i), styles);
		}
		
		
	}

}
