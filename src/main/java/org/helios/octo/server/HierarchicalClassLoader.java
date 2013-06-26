/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2013, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.octo.server;

/**
 * <p>Title: HierarchicalClassLoader</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.HierarchicalClassLoader</code></p>
 */

public class HierarchicalClassLoader extends ClassLoader {
	/** The delegate classloaders in the order they should be searched */
	protected final ClassLoader[] delegates;

	/**
	 * Creates a new HierarchicalClassLoader
	 * @param parent the parent classloader
	 * @param delegates The delegate classloaders in the order they should be searched
	 */
	public HierarchicalClassLoader(ClassLoader parent, ClassLoader...delegates) {
		super(parent);
		this.delegates = (delegates==null||delegates.length==0) ? new ClassLoader[0] : delegates;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	public Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = null;	
		try { 
			clazz = this.getParent().loadClass(name);
			return clazz;
		} catch (ClassNotFoundException ex) { /* No Op */ }
		for(int i = 0; i < delegates.length; i++) {
			try {
				clazz = delegates[i].loadClass(name);
				return clazz;
			}  catch (ClassNotFoundException ex) { /* No Op */ }
		}
		throw new ClassNotFoundException("Could not load the class [" + name + "]");
	}

}
