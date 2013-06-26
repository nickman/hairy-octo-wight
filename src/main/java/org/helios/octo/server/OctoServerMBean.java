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

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;

/**
 * <p>Title: OctoServerMBean</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.OctoServerMBean</code></p>
 */

public interface OctoServerMBean extends ServiceMBean {
	/**
	 * Returns the listening port or -1 if the server channel is not listening
	 * @return the listening port or -1
	 */
	public int getPort();

	/**
	 * Returns the interface the listener is bound to or null if the server channel is not listening
	 * @return the interface the listener is bound to
	 */
	public String getAddress();
	
	/**
	 * Sets the port to listen on 
	 * @param port the port to listen on
	 */
	public void setPort(int port);

	/**
	 * Returns the class loader ref object name
	 * @return the class loader ref object name
	 */
	public ObjectName getClassLoaderRef();

	/**
	 * Sets the class loader ref object name
	 * @param classLoaderRef the classLoaderRef to set
	 */
	public void setClassLoaderRef(ObjectName classLoaderRef);

	/**
	 * Sets the interface the listener should bind to
	 * @param address the address to set
	 */
	public void setAddress(String address);	
}
