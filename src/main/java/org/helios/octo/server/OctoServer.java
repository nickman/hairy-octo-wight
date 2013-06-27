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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * <p>Title: OctoServer</p>
 * <p>Description: The Octo server</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.OctoServer</code></p>
 */

public class OctoServer implements OctoServerMBean {
	/** The listening port */
	protected int port = -1;
	/** The interface to bind to */
	protected String address = "0.0.0.0";
	/** The ObjectName that defines the classloader to load into our scope */
	protected ObjectName classLoaderRef = null;
	/** The classloader to use for the script invocation handler */
	protected ClassLoader classLoader = null;
	
	/** The MBeanServer where this server is deployed  */
	protected MBeanServer server = null;
	
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	/** The close future for the server socket */
	protected ChannelFuture closeFuture = null;
	/** The server channel */
	protected NioServerSocketChannel serverChannel = null;
	/** The boss event loop */
	protected EventLoopGroup bossGroup = null;
	/** The worker event loop */
	protected EventLoopGroup workerGroup = null;
	
	/** The optimized object decoder */
	protected ObjectDecoder objectDecoder = null;
	/** Invocation handler */
	protected final InvocationHandler invocationHandler = new InvocationHandler();
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		OctoServer server = new OctoServer();
		server.setAddress("0.0.0.0");
		server.setPort(1093);
		try {
			server.setClassLoaderRef(new ObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME));
			server.startService();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	
	/**
	 * <p>Starts the OctoServer</p>
	 * {@inheritDoc}
	 * @see org.jboss.system.ServiceMBeanSupport#startService()
	 */
	protected void startService() throws Exception {
		log.info("\n\t===========================================\n\tStarting OctoServer\n\t===========================================");
		InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
		initClassLoader();
		log.info("Starting listener on [" + address + ":" + port + "]");
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap(); 
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.localAddress(new InetSocketAddress(address, port))
				.childHandler(objectDecoder)
				.childHandler(invocationHandler);
			
			b.bind().addListener(new ChannelFutureListener(){
				@Override
				public void operationComplete(ChannelFuture f) throws Exception {
					if(f.isSuccess()) {
						serverChannel = (NioServerSocketChannel) f.channel();
						closeFuture = serverChannel.closeFuture();
						log.info("Started and listening on " + serverChannel.localAddress());
						log.info("\n\t===========================================\n\tStarted OctoServer\n\t===========================================");
					}					
				}
			});
		
	}
	
	/**
	 * <p>Stops the OctoServer</p>
	 * {@inheritDoc}
	 * @see org.jboss.system.ServiceMBeanSupport#stopService()
	 */
	protected void stopService() {
		log.info("\n\t===========================================\n\tStopping OctoServer\n\t===========================================");
		if(serverChannel!=null) {
			try { serverChannel.close().sync(); } catch (Exception ex) {/* No Op */}
			serverChannel = null;
		}
		try { bossGroup.shutdownGracefully().sync(); } catch (Exception ex) {/* No Op */}
		bossGroup = null;
		try { workerGroup.shutdownGracefully().sync(); } catch (Exception ex) {/* No Op */}
		workerGroup = null;		
		log.info("\n\t===========================================\n\tStopped OctoServer\n\t===========================================");
	}
	
	/**
	 * Initializes the class loader
	 */
	protected void initClassLoader() {
		ClassLoader cl = null;
		try {
			if(server==null) server = ManagementFactory.getPlatformMBeanServer();
			classLoader = server.getClassLoaderFor(classLoaderRef);
			if(classLoader==null) classLoader = OctoServer.class.getClassLoader().getParent();
			cl = new HierarchicalClassLoader(OctoServer.class.getClassLoader(), classLoader);
			log.info("Ref ClassLoader set for [" + classLoaderRef + "] --> [" + classLoader + "]");
		} catch (Exception ex) {
			log.error("Failed to get Classloader for Ref [" + classLoaderRef + "]", ex);
			cl = OctoServer.class.getClassLoader();
		}
		objectDecoder = new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(cl));
	}
	
	
	/**
	 * Returns the listening port or -1 if the server channel is not listening
	 * @return the listening port or -1
	 */
	public int getPort() {
		return serverChannel==null ? -1 : serverChannel.localAddress().getPort();
	}

	/**
	 * Returns the interface the listener is bound to or null if the server channel is not listening
	 * @return the interface the listener is bound to
	 */
	public String getAddress() {
		return serverChannel==null ? null : serverChannel.localAddress().getHostName();
	}

	/**
	 * Sets the port to listen on 
	 * @param port the port to listen on
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the class loader ref object name
	 * @return the class loader ref object name
	 */
	public ObjectName getClassLoaderRef() {
		return classLoaderRef;
	}

	/**
	 * Sets the class loader ref object name
	 * @param classLoaderRef the classLoaderRef to set
	 */
	public void setClassLoaderRef(ObjectName classLoaderRef) {
		this.classLoaderRef = classLoaderRef;
	}

	/**
	 * Sets the interface the listener should bind to
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	
}
