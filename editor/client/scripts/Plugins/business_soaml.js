/**
 * Copyright (c) 2012
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

/**
   @namespace Oryx name space for plugins
   @name ORYX.Plugins
*/
if(!ORYX.Plugins)
	ORYX.Plugins = new Object();
	
/**
 * This plugin provides methodes to serialize Business_SoaML into SoaML (XMI).
 * 
 * In other parts of ORYX, this is how an export is made (for example in bpmn2xpdl20.js):
 * 1. send an ajax request to the server to ask for the export. Send the diagram in JSON.
 * 2. Receive the response from the server. It contains the result of the export (XPDL for instance)
 * 3. Open a download window, which will:
 *  3a.	build a form in a new window that will send the result we received (the XPDL) with the filename
 *  3b. submit the form to the server: [...]ORYX/download (servlet MultiDownloader.java)
 * 4. the server replies (=creates a file with the content we sent),
 *    we receive the file (for the second time) which will trigger a "save-as" popup.
 *
 * This is really not efficient, but restricted by browser behaviour. There is however a better way to
 * do it:
 * 1. send the ajax request.
 * 2. receive a URL (one-time use) that we use in a form to retrieve the files.
 * This way, the response files are sent only once between the client and server (and not 3 times)
 * 
 * This might be a lot of change in the code at the moment, especially with testing where openDownloadWindow is used.
 * I chose to have a different strategy: instead of sending an ajax request, we build a form that makes that request (POST),
 * and as a response we receive the file, which triggers a save-as popup, and we don't check for export failure.
 * This solution does not allow a "export is in progress..." message. We use only a timeout of 2 seconds
 *
 * 
 * @class ORYX.Plugins.Business_SoaML_Serialization
 * @extends ORYX.Plugins.AbstractPlugin
 * @param {Object} facade
 * 		The facade of the Editor
 */
ORYX.Plugins.Business_SoaML_Serialization = {
	bsoaml_soamlxmiSerializationHandlerUrl: ORYX.CONFIG.ROOT_PATH + "bsoaml_soamlxmiserialization",
/*	bpmnDeserializationHandlerUrl : ORYX.CONFIG.ROOT_PATH + "Business_SoaML_deserialization",
*/	
	construct: function(facade) {
		arguments.callee.$.construct.apply(this, arguments);

		this.facade.offer({
			'name'				: "Export to SoaML (XMI)",
			'functionality'		: this.exportSoaMLXMI.bind(this),
			'group'				: 'Export',
            dropDownGroupIcon : ORYX.PATH + "images/export2.png",
			'icon' 				: ORYX.PATH + "images/page_white_code.png",
			'description'		: "Exports the diagram to a ZIP file containing the SoaML description of the model (in XMI) together with the associated SoaML Profile. Use the files to import in a UML case tool.",
			'index'				: 0,
			'minShape'			: 0,
			'maxShape'			: 0
		});
	},
	
	
	
	exportSoaMLXMI: function() {
		this.generateSoaMLXMI(this.bsoaml_soamlxmiSerializationHandlerUrl);
	},
	
		
	
	generateSoaMLXMI: function(bsoaml_xmiHandleFunction) {
		var loadMask = new Ext.LoadMask(Ext.getBody(), {msg:"Export of Business-SoaML model into SoaML (XMI)"});
		loadMask.show();
		window.setTimeout (function(){loadMask.hide()}, 2000);	// we simulate the length of the call.

		var jsonString = this.facade.getSerializedJSON();

		// Code inspired from openDownloadWindow. We could in fact put the form in a hidden frame.
		var win = window.open("");
		if (win != null) {
			win.document.open();
			win.document.write("<html><body>");
			var submitForm = win.document.createElement("form");
			win.document.body.appendChild(submitForm);
			
			var createHiddenElement = function(name, value) {
				var newElement = document.createElement("input");
				newElement.name=name;
				newElement.type="hidden";
				newElement.value = value;
				return newElement
			}
			
			submitForm.appendChild( createHiddenElement("data", jsonString) );
						
			submitForm.method = "POST";
			win.document.write("</body></html>");
			win.document.close();
			submitForm.action = bsoaml_xmiHandleFunction;
			submitForm.submit();
		}		

		return;
	}

};

ORYX.Plugins.Business_SoaML_Serialization = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.Business_SoaML_Serialization);