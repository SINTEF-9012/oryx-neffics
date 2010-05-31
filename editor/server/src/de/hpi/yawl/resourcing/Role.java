package de.hpi.yawl.resourcing;

/**
 * Copyright (c) 2010, Armin Zamani
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
 * s
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class Role extends ResourcingType {

	/**
	 * constructor of class 
	 */
	public Role() {
		super();
		id = "RO-" + id;
	}
	
	/**
	 * @see de.hpi.yawl.resourcing.ResourcingType#writeAsMemberOfDistributionSetToYAWL()
	 */
	public String writeAsMemberOfDistributionSetToYAWL(){
		String s = "";
		s += String.format("\t\t<role>%s</role>\n", id);
		return s;
	}
	
	/**
	 * @see de.hpi.yawl.resourcing.ResourcingType#writeToYAWL()
	 */
	public String writeToYAWL(){
		String s = "";
		s += String.format("\t\t<role id=\"%s\">\n", id);
		s = writeNameToYAWL(s);
		s = writeDescriptionToYAWL(s);
		s = writeNotesToYAWL(s);
		s += "\t\t</role>\n";
		
		return s;
	}
	
}
