/***************************************
 * Copyright (c) 2008
 * Bjoern Wagner
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

package org.b3mn.poem.business;

import java.util.ArrayList;
import java.util.Collection;

import org.b3mn.poem.Identity;
import org.b3mn.poem.Persistance;


public abstract class BusinessObject {

	protected Identity identity;
	
	public int getId() {
		return identity.getId();
	}
	
	public BusinessObject(Identity identity) {
		this.identity = identity;
	}
	
	protected void persistDBObject(Object o) {
		Persistance.getSession().save(o);
	}
	
	protected void updateDBObject(Object o) {
		Persistance.getSession().update(o);
	}
	
	public Identity getIdentity() {
		return this.identity;
	}
	
	protected  static Collection<String> toStringCollection(Collection<? extends Object> collection) {
		ArrayList<String> list = new ArrayList<String>();
		for (Object o: collection) {
			// TODO implement type checking (--> erasure)
			try {
				list.add((String) o);
			} catch (Exception e) {}
		}
		return list;
	}
}
