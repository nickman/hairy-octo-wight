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
package org.helios.octo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.helios.octo.server.invocation.InvocationRequest;
import org.helios.octo.util.NettyUtil;

/**
 * <p>Title: OctoClient</p>
 * <p>Description: Optimized client for sending scripts and arguments to an {@link org.helios.octo.server.OctoServer} instance.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.client.OctoClient</code></p>
 */

public class OctoClient {
	/** The OctoServer host */
	protected final String host;
	/** The OctoServer listening port */
	protected final int port;
	/** The channel allocated for this client */
	protected Channel channel = null;
	/** Connected indicator */
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	/** Instance logger */
	protected final Logger log;
	/** Request id serial factory */
	protected final AtomicLong requestIdFactory = new AtomicLong(0L);
	
	
	/**
	 * Creates a new OctoClient and synchronously connects
	 * @param host The OctoServer host
	 * @param port The OctoServer listening port
	 */
	public OctoClient(String host, int port) {
		if(host==null || host.trim().isEmpty()) throw new IllegalArgumentException("The passed host was null or empty");
		this.host = host;
		this.port = port;
		log = Logger.getLogger(getClass().getName() + "." + host + ":" + port);
		try {
			channel = OctoShared.getInstance().getBootstrap().connect(host, port).sync().channel();
			log.info("Connected");
			connected.set(true);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to connect to [" + host + ":" + port + "]", ex);
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		OctoClient client = new OctoClient("localhost", 1093);
		client.execute("Hello World", new Date());
		//try { Thread.currentThread().join(3000); } catch (Exception x) {}
		try { Thread.currentThread().join(); } catch (Exception x) {}
		OctoShared.getInstance().shutdownAll();
		
		
		
	}
	
	
	
	/**
	 * Creates a script execution request and sends it
	 * @param s The script content to send
	 * @param args The invocation arguments
	 * @return the request id
	 */
	public long execute(String s, Object...args) {
		final long rId = requestIdFactory.incrementAndGet();
		if(s==null || s.trim().isEmpty()) throw new IllegalArgumentException("The passed script was null or empty");
		channel.write(new InvocationRequest(s, args, rId)).syncUninterruptibly();
		return rId;
	}

	/**
	 * Closes this client
	 */
	public void close() {
		this.channel.close().syncUninterruptibly();
	}

	/**
	 * Returns the OctoServer host
	 * @return the host
	 */
	public String getHost() {
		return host;
	}



	/**
	 * Returns the OctoServer listening port
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

}
