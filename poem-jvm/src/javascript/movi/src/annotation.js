/**
 * Copyright (c) 2009
 * Jan-Felix Schwarz
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
 **/

MOVI.namespace("util");

(function() {
	
	var _BUBBLE_VISIBLE_CLASS_NAME 		= "movi-bubble-visible",
		_BUBBLE_HIDDEN_CLASS_NAME 		= "movi-bubble-hidden",
		_BUBBLE_UL_CLASS_NAME 			= "movi-bubble-ul",
		_BUBBLE_UR_CLASS_NAME 			= "movi-bubble-ur",
		_BUBBLE_LL_CLASS_NAME 			= "movi-bubble-ll",
		_BUBBLE_LR_CLASS_NAME 			= "movi-bubble-lr",
		_BUBBLE_BORDERTOP_CLASS_NAME 	= "movi-bubble-bt",
		_BUBBLE_BORDERBOTTOM_CLASS_NAME	= "movi-bubble-bb",
		_BUBBLE_BORDERLEFT_CLASS_NAME   = "movi-bubble-bl",
		_BUBBLE_BORDERRIGHT_CLASS_NAME  = "movi-bubble-br",
		_BUBBLE_ARROW_LEFT_CLASS_NAME 	= "movi-bubble-al",
		_BUBBLE_ARROW_RIGHT_CLASS_NAME 	= "movi-bubble-ar",
		_BUBBLE_ARROW_TOP_CLASS_NAME 	= "movi-bubble-at",
		_BUBBLE_ARROW_BOTTOM_CLASS_NAME	= "movi-bubble-ab",
		_BUBBLE_CONTENT_CLASS_NAME	 	= "movi-bubble-bc",
		_BUBBLE_CLOSEBUTTON_CLASS_NAME 	= "movi-bubble-closebutton"
		
	var Event	= YAHOO.util.Event,
		Element	= YAHOO.util.Element;
	
	/**
	 * Create an Annotation for a Marker to enrich the model with additional information 
	 * and functionality. The Annotation renders a speech bubble containing arbitrary
	 * XHTML content and attachs it to the Marker.
	 * @namespace MOVI.util
	 * @class MOVI.util.Annotation
	 * @extends YAHOO.util.Element
	 * @constructor
	 * @param {Marker} marker The Marker to attach the Annotation to
	 * @param {String} content The Annotation's inner HTML content 
	 */
    MOVI.util.Annotation = function(marker, content) {
	
		if(!marker) {
			throw new Error("No marker specified for annotation.", "annotation.js");
			return false;
		}
		
		if(!YAHOO.lang.isString(content)) {
			throw new TypeError("A String is expected for the annotation's content.", "annotation.js");
		}
		
		// create host element
        el = _createHostElement.call(this);
		
		MOVI.util.Annotation.superclass.constructor.call(this, el, {}); 
		
		_createBubble.call(this, content);
		
		this._content = new Element(
			this.getElementsByClassName(_BUBBLE_CONTENT_CLASS_NAME)[0]);
		
		this._marker = marker;
		this._marker.onChanged(this._update, this);
		
		(new Element(this.getElementsByClassName(_BUBBLE_CLOSEBUTTON_CLASS_NAME)[0]))
			.addListener("click", this._close, this, this);
		
		// don't bubble mouse events on annotations
		this.addListener("mouseover", function(ev) { Event.stopPropagation(ev) });
		this.addListener("mouseout", function(ev) { Event.stopPropagation(ev) });
		this.addListener("click", function(ev) { Event.stopPropagation(ev) });
		this.addListener("mousedown", function(ev) { Event.stopPropagation(ev) });
		
		// allow user to select text 
		this.setStyle("-moz-user-select", "text");    // Gecko-based, Mozilla
		this.setStyle("-webkit-user-select", "text"); // Safari 3.0
		this.setStyle("-khtml-user-select", "text");  // Safari 2.0
		if (YAHOO.env.ua.ie > 0)
			this.set("unselectable", "off");           // IE
			
		this._update();
	}

	MOVI.extend(MOVI.util.Annotation, Element, {
		
		/**
		 * The Element containing the specified content as inner HTML.
		 * @property _contentElement
		 * @type Element
		 * @private
		 */
		_contentElement: null,
		
		/**
		 * The marker that the annotation is attached to
		 * @property _marker
		 * @type Marker
		 * @private
		 */
		_marker: null,
		
		/**
		 * The canvas that the annotation is attached to
		 * @property _canvas
		 * @type Canvas
		 * @private
		 */
		_canvas: null,
		
		/**
		 * The callback that is executed when the annotation bubble is closed
		 * @property _closeCallback
		 * @type Object
		 * @private
		 */
		_closeCallback: null,
		
		/**
	     * Callback that is executed when the close button is clicked
	     * @method _onClose
		 * @private
	     */
		_close: function() {
			this.hide();
			if(this._closeCallback) {
				this._closeCallback.callback.call(
					this._closeCallback.callback.scope,
					this,
					this._closeCallback.callback.data
				);
			}
		},
		
		/**
	     * Update the position of the annotation bubble
	     * @method _update
		 * @private
	     */
		_update: function() {
			var zoomFactor = 1;
			if(!this._canvas) {
				this._canvas = this._marker.canvas;
				if(this._canvas) {
					this._canvas.appendChild(this);
					// now that we have our canvas and therefore the model viewer we can subscribe
					// to the zoom event
					this._canvas.getModelViewer().onZoomLevelChangeEnd.subscribe(function() {
						this._update();
					}, this, true);
					
					zoomFactor = this._canvas.getModelViewer().getZoomLevel() / 100;
					
				} else {
					// canvas not available, update again when it becomes available
					this._marker.onCanvasAvailable(this._update, this);
				}
			} else {
				zoomFactor = this._canvas.getModelViewer().getZoomLevel() / 100;
			}
			
			var bounds = this._marker.getAbsBounds();
			var left = Math.round(bounds.upperLeft.x * zoomFactor);
			var right = Math.round(bounds.lowerRight.x * zoomFactor);
			var top = Math.round((bounds.upperLeft.y + (bounds.lowerRight.y-bounds.upperLeft.y)*0.7) * zoomFactor);
			var w = parseInt(this._canvas.getStyle("width"), 10);

			if(left>(w/2))
				this.setPosition(left, top);
			else 
				this.setPosition(right, top);
			
		},
		
		/**
	     * Set the absolute position of the annotation bubble 
	     * @method setPosition
		 * @param {Integer} x The x coordinate of the position the bubble points to
		 * @param {Integer} y The y coordinate of the position the bubble points to
	     */
		setPosition: function(x, y) {
			var w = parseInt(this._canvas.getStyle("width"), 10);
			if( w && w>300 && (x>(w/2)) ) {
				// align the annotation to the left (only if the canvas is wider than 300px)
				this.setStyle("right", (w-x) + "px");
				this.setStyle("left", "");
				this.addClass("movi-bubble-right");
			} else {
				// align the annotation to the right
				this.setStyle("left", x + "px");
				this.setStyle("right", "");
				this.removeClass("movi-bubble-right");
			}
			this.setStyle("top", y + "px");
		},
		
		/**
	     * Show the annotation bubble
	     * @method show
	     */
		show: function() {
			//this._marker.show();
			this.replaceClass(_BUBBLE_HIDDEN_CLASS_NAME, _BUBBLE_VISIBLE_CLASS_NAME);
			this.bringToFront();
			this.visible = true;
		},
		
		/**
	     * Hide the annotation bubble
	     * @method hide
	     */
		hide: function() {
			this.replaceClass(_BUBBLE_VISIBLE_CLASS_NAME, _BUBBLE_HIDDEN_CLASS_NAME);
			this.visible = false;
		},
		
		/**
	     * Toggle show/hide the annotation bubble
	     * @method toggle
	     */
		toggle: function() {
			if(this.isVisible())
				this.hide();
			else 
				this.show();
		},
		
		/**
		 * Returns true if the annotation is visible
		 * @method isVisible
		 * @returns {Boolean} 
		 */
		isVisible: function() {
		    return "undefined" == typeof(this.visible) ? this.hasClass(_BUBBLE_VISIBLE_CLASS_NAME) : this.visible;
		},
		
		/**
	     * Remove the element from the DOM
	     * @method remove
	     */
		remove: function() {
			this.get("element").parentNode.removeChild(this.get("element"));
		},
		
		/**
	     * Specfiy a callback that is executed when the annotation bubble is
		 * closed using the close button
	     * @method onClose
		 * @param {Function} callback The callback function
		 * @param {Object} scope The object to use as the scope for the callback
		 * @param {Any} data The variable to pass to the callback function
	     */
		onClose: function(callback, scope, data) {
			this._closeCallback = {
				callback: callback,
				scope: scope,
				data: data
			};
		},
		
		/**
	     * Brings the annotation to the front of all the other annotations on the canvas
	     * @method bringToFront
	     */
		bringToFront: function() {
			// TODO: method is outdated, as the markers z-index is updated and the annotation
			// node is no longer a child of it. also, this method has very poor performance in IE6
			return; // skip all calls of this method for the first
			
			// if element has not been added to the dom do nothing
			if(!this.get("element").parentNode) return;
			
			MOVI.util.Annotation
			
			// detemine the maximal z-index of markers of visible annotation elements
			var maxZIndex = 0;
			var canvasEl = new Element(this.get("element").parentNode.parentNode);
			var elements = canvasEl.getElementsByClassName(_BUBBLE_VISIBLE_CLASS_NAME);
			for(var key in elements) {
				if(!YAHOO.lang.hasOwnProperty(elements, key)) continue;
				var zIndex = parseInt((new Element(elements[key].parentNode)).getStyle("z-index"), 10);
				if(zIndex>maxZIndex) maxZIndex = zIndex;
			}
			// set the z-index of parent marker to maximum+1*/
			this._marker.markerRect.setStyle("z-index", maxZIndex+1);
		},
		
		/**
	     * Specfiy a new marker to which the annotation is attached
	     * @method setMarker
		 * @param {MOVI.util.Marker} marker The marker element the annotation shall be attached to
	     */
		setMarker: function(marker) {
			this._canvas = undefined;
			this._marker = marker;
			this._marker.onChanged(this._update, this);
			this._update();
		},
		
		/**
	     * Returns the Marker instance this annotation is attached to
	     * @method getMarker
	     */
		getMarker: function(marker) {
			return this._marker;
		}
		
	});
	
	/**
     * Create the host element.
	 * @method
	 * @private
     */
    var _createHostElement = function() {
        var el = document.createElement('div');
        return el;
    };

	/**
     * Create the bubble elements inside the host element.
	 * @method
	 * @private
     */
	var _createBubble = function(content) {
		
		var isIE6 = YAHOO.env.ua.ie === 6;
		
		this.set("className", _BUBBLE_HIDDEN_CLASS_NAME);
		
		this.set("innerHTML", 	
						["<div class=\"" + _BUBBLE_UL_CLASS_NAME + "\">",
							(isIE6 ? "<img class=\""+ _BUBBLE_UL_CLASS_NAME +"\">":""),
							"<div class=\"" + _BUBBLE_UR_CLASS_NAME + "\">",
								(isIE6 ? "<img class=\""+ _BUBBLE_UR_CLASS_NAME +"\">":""),
					   			"<div class=\"" + _BUBBLE_LL_CLASS_NAME + "\">",
									(isIE6 ? "<img class=\""+ _BUBBLE_LL_CLASS_NAME +"\">":""),
									"<div class=\"" + _BUBBLE_LR_CLASS_NAME + "\">",
										(isIE6 ? "<img class=\""+ _BUBBLE_LR_CLASS_NAME +"\">":""),
								    	"<div class=\"" + _BUBBLE_BORDERTOP_CLASS_NAME + "\"></div>",
								    	"<div class=\"" + _BUBBLE_BORDERLEFT_CLASS_NAME + "\">",
											(isIE6 ? "<img class=\""+ _BUBBLE_BORDERLEFT_CLASS_NAME +"\">":""),
								    		"<div class=\"" + _BUBBLE_BORDERRIGHT_CLASS_NAME + "\">",
												(isIE6 ? "<img class=\""+ _BUBBLE_BORDERRIGHT_CLASS_NAME +"\">":""),
								   		   		"<div class=\"" + _BUBBLE_CONTENT_CLASS_NAME + "\">",
										    		"<div class=\"" + _BUBBLE_CLOSEBUTTON_CLASS_NAME + "\"></div>",
								            			content,
								         			"</div>",
								        		"</div>",
											"</div>",
								        "<div class=\"" + _BUBBLE_BORDERBOTTOM_CLASS_NAME + "\"></div>",
					   				"</div>",
								"</div>",
					   		"</div>",
						"</div>",
					    "<div class=\"" + _BUBBLE_ARROW_LEFT_CLASS_NAME + "\"/>"].join(""));
	}
	
})();