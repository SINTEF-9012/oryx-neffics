if(!ORYX.Plugins)
	ORYX.Plugins = new Object();
	
ORYX.Plugins.ServiceML = {
	serviceml_HandlerUrl: ORYX.CONFIG.ROOT_PATH + "serviceml",

	construct: function(facade) {
		arguments.callee.$.construct.apply(this, arguments);

		this.facade.offer({
			'name'				: "Export to Service XML",
			'functionality'		: this.exportServiceXML.bind(this),
			'group'				: 'Export',
            dropDownGroupIcon : ORYX.PATH + "images/export2.png",
			'icon' 				: ORYX.PATH + "images/page_white_code.png",
			'description'		: "Exports the Service model to an .xml file. This file can be imported into the NEFFICS VDML Editor.",
			'index'				: 0,
			'minShape'			: 0,
			'maxShape'			: 0
		});

			this.facade.offer({
			'name'				: "Import from Service XML",
			'functionality'		: this.importServiceXML.bind(this),
			'group'				: 'Export',
            dropDownGroupIcon : ORYX.PATH + "images/import.png",
			'icon' 				: ORYX.PATH + "images/page_white_code.png",
			'description'		: "Imports the Service model as an .xml file. This file was exported from the NEFFICS VDML Editor.",
			'index'				: 0,
			'minShape'			: 0,
			'maxShape'			: 0
		});
		
	},
		
	
	exportServiceXML: function() {
		this.generateServiceXML(this.serviceml_HandlerUrl);
	},

	importServiceXML: function() {
//		this.processServiceXML(this.serviceml_HandlerUrl);
	},	
		
	
	generateServiceXML: function(serviceml_HandleFunction) {
		var loadMask = new Ext.LoadMask(Ext.getBody(), {msg:"Export to Service XML)"});
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
			submitForm.action = serviceml_HandleFunction;
			submitForm.submit();
		}		

		return;
	}

};

ORYX.Plugins.ServiceML = ORYX.Plugins.AbstractPlugin.extend(ORYX.Plugins.ServiceML);