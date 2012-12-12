
/**
 * @namespace Oryx name space for different utility methods
 * @name ORYX.Utils
*/
ORYX.Utils = {
    /**
     * General helper method for parsing a param out of current location url
     * @example
     * // Current url in Browser => "http://oryx.org?param=value"
     * ORYX.Utils.getParamFromUrl("param") // => "value" 
     * @param {Object} name
     */
    getParamFromUrl: function(name){
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regexS = "[\\?&]" + name + "=([^&#]*)";
        var regex = new RegExp(regexS);
        var results = regex.exec(window.location.href);
        if (results == null) {
            return null;
        }
        else {
            return results[1];
        }
    },
	
	adjustGradient: function(gradient, reference){
		
		if (ORYX.CONFIG.DISABLE_GRADIENT && gradient){
		
			var col = reference.getAttributeNS(null, "stop-color") || "#ffffff";
			
			$A(gradient.getElementsByTagName("stop")).each(function(stop){
				if (stop == reference){ return; }
				stop.setAttributeNS(null, "stop-color", col);
			})
		}
	},

	
	/**
	 * Get information of all the models in the repository (current user).
	 * If an error occurs, prints it in the log (error), opens an alert to the user and returns the information we managed to retrieve.
	 * @return {Array.{
	 *      id: string,
	 * 		summary: string,
     * 		author: string,
     * 		creationDate: string,
     * 		title: string,
     * 		thumbnailUri: string,
     * 		pngUri: string,
     * 		type: string
     *  }} 
	 * array of meta information (one for each model), possibly empty. 'type' is the namespace of the model.
	 */
    getRepositoryContent: function () {
    	// We first get the list of models (i.e. list of IDs). Then for each ID we retrieve the meta information.
    	// Code inspired from repository.js::applyFilter and call to a "doRequest" (dataCache.js)
    	var modelIds = [];
    	var modelInformations = [];
    	var error = "";

    	new Ajax.Request(ORYX.CONFIG.BACKEND_PATH + "filter", 
   			 {
   				method			: "get",
   				asynchronous 	: false,
   				onSuccess		: function (response) {
   					modelIds = eval(response.responseText);
   				},
   				onFailure		: function(response){
					ORYX.Log.error('Utils.getRepositoryContent: server communication failed to get list of models: ' + response.status + " (" + response.statusText + ")");
					error += "Server communication failed when retreiving list of models.";
   				},
   				parameters 		: null
   			});

    	modelIds.each (function (modelId) {
    		var modelInfo = ORYX.Utils.getModelMetaInformation(modelId);
    		if (modelInfo)
    			modelInformations.push (modelInfo);
    		else {
    			if (error.length == 0) {
    				error = "Server communication failed when retreiving information for models: ";
    			}
    			error += modelId + "; ";
    		}
    	});
    	if (error.length > 0) {
			Ext.Msg.alert('Oryx',error);
    	}
    	return modelInformations;
    },
    
    
    /**
     * Returns the meta information on the given modelId.
     * @param {string} modelId string representing the modelId. Example: "model/14", "model/15"
     * @return {
     * 	{   id: string,
     *    	summary: string,
     * 		author: string,
     * 		creationDate: string,
     * 		title: string,
     * 		thumbnailUri: string,
     * 		pngUri: string,
     * 		type: string
     *  }} 
     *  The 'type' is in fact the namespace of the model. Returns 'undefined' if no such model exists (error is logged as well)
     */
    getModelMetaInformation: function (modelId) {
    	var modelInfo;
    	new Ajax.Request (ORYX.CONFIG.BACKEND_PATH + modelId + "/meta",
    			{
    				method			: "get",
    				asynchronous 	: false,
    				onSuccess		: function (response) {
    					if (response.responseText.length > 0) {
    						modelInfo =  Ext.util.JSON.decode(response.responseText);
    						modelInfo.id = modelId;
    					}
    				},
    				onFailure		: function(response){
    					ORYX.Log.error('Utils.getModelMetaInformation: server communication failed to get metainfo of model "' + modelId + '": ' + response.status + " (" + response.statusText + ")");
    				},
    				parameters 		: null
    			});
    	return modelInfo;
    },
    
    
    /**
     * Returns the model that has the given ID. The result is the JSON representation of the model.
     * @param {string} modelId string representing the modelId. Example: "model/14", "model/15"
     * @return JSON representation of the model (i.e. decoded). Returns 'undefined' if no such model exists (error is logged as well)
     */
    getModel: function (modelId) {
    	var model;
    	
		new Ajax.Request(ORYX.CONFIG.BACKEND_PATH + modelId + "/json",
			{
				method			: "get",
				asynchronous 	: false,
				onSuccess		: function (response) {
					model = Ext.util.JSON.decode(response.responseText);
				},
				onFailure		: function(response){
					ORYX.Log.error('Utils.getModel: server communication failed to get model "' + modelId + '": ' + response.status + " (" + response.statusText + ")");
				},
				parameters 		: null
			});
		return model;
	},
	
	
	/**
	 * In the model, search the shape that has the ID 'shapeID'
	 * Returns the shape in JSON representation
	 * @param modelJSON JSON representation of a model
	 * @param shapeId {string} the resource ID of the shape to return.
	 * @return shape in JSON format. If no model or not shape is found, returns undefined.
	 */
	getShapeFromModel: function (modelJSON, shapeId) {
		var shape;

		// Recursively finds the shape with shapeId, starting from the modelResource.
		// Returns undefined if none found.
		var findShape = function (modelResource) {
			if (modelResource.resourceId && modelResource.resourceId === shapeId)
				return modelResource;
			var result;
			for (var i = 0; i < modelResource.childShapes.length; i++) {
				result = findShape(modelResource.childShapes[i]);
				if (result)
					break;
			}
			return result;
		}

		return findShape(modelJSON);
	},
	
	
	/**
	 * Look recursively in the modelJSON and look for the shapes (i.e. stencil ID) given in shapeFilter.
	 * Returns an array of the shapes that gave a match.
	 * @param modelJSON JSON representation of a model
	 * @param shapeFilter {Array.{string}} list of stencil ID used to filter the shapes. The ID should not be prefixed by a namespace.
	 * @return Array of JSON representation of the filtered shapes. Shapes will have a stencil ID that is contained in shapeFilter. 
	 * Return empty array if modelJSON is not a JSON representation of a model or if shapeFilter is not an array.
	 */
	filterShapesFromJSONModel: function (modelJSON, shapeFilter) {
		var result = [];

		if (!(shapeFilter instanceof Array))
			return result;
		
		var addSearchedModelElement = function (modelResource) {
			if (   modelResource.stencil
				&& modelResource.stencil.id
				&& shapeFilter.indexOf(modelResource.stencil.id) >= 0) {
				result.push (modelResource);
			}
			if (modelResource.childShapes && modelResource.childShapes.size() > 0) {
				modelResource.childShapes.each(function (elt) { addSearchedModelElement (elt) });
			}
		}
		
		addSearchedModelElement (modelJSON);
		return result;
	}
}
    
 

/* 
 *  Javascript (good) hack fixing Chrome and Chromium bug that prevent using insertAdjacentHTML with namespaces
 * 
 *  Copyright (c) 2011 Florent FAYOLLE
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

// check if Chrome/Chromium is used
if(/Chrome/.test(navigator.userAgent)){
        // test if the bug is really present
        var div = document.createElement("div");
        div.insertAdjacentHTML("BeforeEnd", "<p foo:bar='hello'>world</p>");
        
        // the bug is when the div Element, after the call of insertAdjacentHTML method, still has no chrildren
        if(div.children.length === 0){
                // save the native function of insertAdjacentHTML
                var proxy_insertAdjacentHTML = HTMLElement.prototype.insertAdjacentHTML;
                // function that replace all modified attributes to their real name
                function __clean_attr(node){
                        var name;
                        for(var i = 0; i < node.attributes.length; i++){
                                name = node.attributes[i].nodeName;
                                if( node.attributes[i].nodeName.indexOf("__colon__") >= 0){
                                        node.setAttribute(name.replace(/__colon__/g, ":"), node.getAttribute(name));
                                        node.removeAttribute(name);
                                }
                        }
                }
                // the new function insertAdjacentHTML will replace all attributes of that form : namespace:attribute="value"
                // to that form : namespace__colon__attribute="value"
                HTMLElement.prototype.insertAdjacentHTML = function(where, html){
                        var new_html = html.replace( /([\S]+):([\S]+)=/g ,"$1__colon__$2=");
                        // we call the native insertAdjacentHTML that will parse the HTML string to DOM
                        proxy_insertAdjacentHTML.call(this, where, new_html);
                        var nodes = this.getElementsByTagName("*");
                        for(var i = 0; i < nodes.length; i++)
                                __clean_attr(nodes[i]);
                }
        }
}
