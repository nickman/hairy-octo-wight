/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
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
package org.helios.octo.server.jboss;

import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.helios.octo.server.OctoServer;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;

/**
 * <p>Title: JBossOctoServer</p>
 * <p>Description: JBoss {@link ServiceMBean} wrapper for {@link OctoServer}.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.jboss.JBossOctoServer</code></p>
 */

public class JBossOctoServer extends OctoServer implements JBossOctoServerMBean {
	/** The delegated service mbean */
	protected ServiceMBeanSupport serviceMBean = new ServiceMBeanSupport(getClass());

	/**
	 * Creates a new JBossOctoServer
	 */
	public JBossOctoServer() {
		
	}
	
	//============================================================================================
	//   ServiceMBeanSupport delegation methods
	//   Had to implement the ServiceMBeanSupport using delegates
	//   since we cannot extend.
	//============================================================================================
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void addNotificationListener(NotificationListener arg0,
			NotificationFilter arg1, Object arg2) {
		serviceMBean.addNotificationListener(arg0, arg1, arg2);
	}

	/**
	 * @throws Exception
	 * @see org.jboss.system.ServiceMBeanSupport#create()
	 */
	public void create() throws Exception {
		serviceMBean.create();
	}

	/**
	 * 
	 * @see org.jboss.system.ServiceMBeanSupport#destroy()
	 */
	public void destroy() {
		serviceMBean.destroy();
	}

	/**
	 * @return
	 * @throws JMException
	 * @see org.jboss.system.ServiceMBeanSupport#getDeploymentInfo()
	 */
	public DeploymentInfo getDeploymentInfo() throws JMException {
		return serviceMBean.getDeploymentInfo();
	}

	/**
	 * @return
	 * @see org.jboss.system.ServiceMBeanSupport#getLog()
	 */
	public Logger getLog() {
		return serviceMBean.getLog();
	}

	/**
	 * @return
	 * @see org.jboss.system.ServiceMBeanSupport#getName()
	 */
	public String getName() {
		return serviceMBean.getName();
	}

	/**
	 * @return
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#getNotificationInfo()
	 */
	public MBeanNotificationInfo[] getNotificationInfo() {
		return serviceMBean.getNotificationInfo();
	}

	/**
	 * @return
	 * @see org.jboss.system.ServiceMBeanSupport#getServer()
	 */
	public MBeanServer getServer() {
		return serviceMBean.getServer();
	}

	/**
	 * @return
	 * @see org.jboss.system.ServiceMBeanSupport#getServiceName()
	 */
	public ObjectName getServiceName() {
		return serviceMBean.getServiceName();
	}

	/**
	 * @return
	 * @see org.jboss.system.ServiceMBeanSupport#getState()
	 */
	public int getState() {
		return serviceMBean.getState();
	}

	/**
	 * @return
	 * @see org.jboss.system.ServiceMBeanSupport#getStateString()
	 */
	public String getStateString() {
		return serviceMBean.getStateString();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#handleNotification(javax.management.NotificationListener, javax.management.Notification, java.lang.Object)
	 */
	public void handleNotification(NotificationListener arg0,
			Notification arg1, Object arg2) {
		serviceMBean.handleNotification(arg0, arg1, arg2);
	}

	/**
	 * @param method
	 * @throws Exception
	 * @see org.jboss.system.ServiceMBeanSupport#jbossInternalLifecycle(java.lang.String)
	 */
	public void jbossInternalLifecycle(String method) throws Exception {
		serviceMBean.jbossInternalLifecycle(method);
	}

	/**
	 * @return
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#nextNotificationSequenceNumber()
	 */
	public long nextNotificationSequenceNumber() {
		return serviceMBean.nextNotificationSequenceNumber();
	}

	/**
	 * 
	 * @see org.jboss.system.ServiceMBeanSupport#postDeregister()
	 */
	public void postDeregister() {
		serviceMBean.postDeregister();
	}

	/**
	 * @param arg0
	 * @see org.jboss.system.ServiceMBeanSupport#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean arg0) {
		serviceMBean.postRegister(arg0);
	}

	/**
	 * @throws Exception
	 * @see org.jboss.system.ServiceMBeanSupport#preDeregister()
	 */
	public void preDeregister() throws Exception {
		serviceMBean.preDeregister();
	}

	/**
	 * @param server
	 * @param name
	 * @return
	 * @throws Exception
	 * @see org.jboss.system.ServiceMBeanSupport#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		this.server = server;
		return serviceMBean.preRegister(server, name);
	}

	/**
	 * @param listener
	 * @param filter
	 * @param handback
	 * @throws ListenerNotFoundException
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#removeNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	public void removeNotificationListener(NotificationListener listener,
			NotificationFilter filter, Object handback)
			throws ListenerNotFoundException {
		serviceMBean.removeNotificationListener(listener, filter, handback);
	}

	/**
	 * @param listener
	 * @throws ListenerNotFoundException
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#removeNotificationListener(javax.management.NotificationListener)
	 */
	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		serviceMBean.removeNotificationListener(listener);
	}

	/**
	 * @param arg0
	 * @see org.jboss.mx.util.JBossNotificationBroadcasterSupport#sendNotification(javax.management.Notification)
	 */
	public void sendNotification(Notification arg0) {
		serviceMBean.sendNotification(arg0);
	}

	/**
	 * @throws Exception
	 * @see org.jboss.system.ServiceMBeanSupport#start()
	 */
	public void start() throws Exception {
		serviceMBean.start();
	}

	/**
	 * 
	 * @see org.jboss.system.ServiceMBeanSupport#stop()
	 */
	public void stop() {
		serviceMBean.stop();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return serviceMBean.toString();
	} 
}
