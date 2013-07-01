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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * <p>Title: OctoShared</p>
 * <p>Description: A singleton resource manager for sharing resources amongst multiple clients</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.client.OctoShared</code></p>
 */

public class OctoShared implements ThreadFactory {
	/** The singleton instance */
	protected static volatile OctoShared instance = null;
	/** The singleton instance ctor lock */
	protected static final Object lock = new Object();
	
	/** The event loop thread provider for all clients */
	protected final EventLoopGroup group;
	/** The channel group where all client channels are registered */
	protected final ChannelGroup channelGroup;
	/** The bootstrap for connecting new clients */
	protected final Bootstrap bootstrap;
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	/** logging handler */
	protected final LoggingHandler loggingHandler = new LoggingHandler(OctoClient.class, LogLevel.INFO);
	/** Response handler */
	protected final ResponseHandler responseHandler = new ResponseHandler();
	/** Indicator switched on when {@link OctoShared#shutdownAll()} is being called */
	protected final AtomicBoolean stopping = new AtomicBoolean(false);
	
	/**
	 * Acquires the OctoShared singleton instance
	 * @return the OctoShared singleton instance
	 */
	public static OctoShared getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new OctoShared();
				}
			}
		}
		return instance;
	}
	
	/** The attribute key for the request id */
	public static final AttributeKey<Long> REQ_ID = new AttributeKey<Long>("ReqId"); 
	
	/** The attribute key for the stream type */
	public static final AttributeKey<Byte> STREAM = new AttributeKey<Byte>("StremType"); 
	
	
	
	/** The key for the response handler in the client pipeline */
	public static final String RESPONSE_HANDLER = "responseHandler";
	/** The key for the stream frame decoder in the client pipeline */
	public static final String STREAM_DECODER = "streamDecoder";
	/** The key for the object decoder in the client pipeline */
	public static final String OBJECT_DECODER = "objectDecoder";
	/** The key for the object encoder in the client pipeline */
	public static final String OBJECT_ENCODER = "objectEncoder";
	/** The key for the string decoder in the client pipeline */
	public static final String STRING_DECODER = "stringDecoder";
	/** The key for the string printer in the client pipeline */
	public static final String STRING_PRINTER = "stringPrinter";

	/** The key for the logging handler in the client pipeline */
	public static final String LOGGING_HANDLER = "logging";
	
	protected final MessageToMessageDecoder<String> stringPrinter = new MessageToMessageDecoder<String>() {
		protected void decode(io.netty.channel.ChannelHandlerContext ctx, String msg, io.netty.channel.MessageList<Object> out) throws Exception {
			byte streamType = ctx.channel().attr(STREAM).get();
			if(streamType==0) {
				System.out.println("[out]:" + msg);
			} else {
				System.err.println("[err]:" + msg);
			}
		};
	};
	
	private OctoShared() {
		group = new NioEventLoopGroup(MultithreadEventLoopGroup.DEFAULT_EVENT_LOOP_THREADS, this);
		channelGroup = new DefaultChannelGroup(group.next());
		bootstrap = new Bootstrap();
		bootstrap
			.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                	ch.pipeline().addLast(LOGGING_HANDLER, loggingHandler);
                	ch.pipeline().addLast(OBJECT_ENCODER, new ObjectEncoder());
                	ch.pipeline().addLast(RESPONSE_HANDLER, responseHandler);
                	//ch.pipeline().addLast(STREAM_DECODER, new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
                	ch.pipeline().addLast(STRING_DECODER, new StringDecoder()); 
                	ch.pipeline().addLast(STRING_PRINTER, stringPrinter);
                	//ch.pipeline().addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                	
                }});
		log.info("OctoShared Initialized");
	}
	
	public ChannelFuture connect(String host, int port) {
		ChannelFuture cf = bootstrap.connect();
		cf.addListener(new ChannelFutureListener(){
			/**
			 * {@inheritDoc}
			 * @see io.netty.util.concurrent.GenericFutureListener#operationComplete(io.netty.util.concurrent.Future)
			 */
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				// TODO Auto-generated method stub
				
			}
		});
		return cf;
	}
	
	/**
	 * Returns the shared bootstrap
	 * @return the shared bootstrap
	 */
	public Bootstrap getBootstrap() {
		return bootstrap;
	}
	
	/**
	 * Stops and disconnects all clients and de-allocates all resources
	 */
	public void shutdownAll() {
		if(!stopping.compareAndSet(false, true)) {
			throw new RuntimeException("This OctoShared is already being shutdown");
		}
		log.info("Stopping all OctoClients....");
		channelGroup.close().addListener(new ChannelGroupFutureListener(){
			@Override
			public void operationComplete(ChannelGroupFuture future) throws Exception {
				log.info("ChannelGroup Closed");
				group.shutdownGracefully().addListener(new FutureListener<Object>() {
					@Override
					public void operationComplete(Future<Object> future) throws Exception {
						log.info("Event Loop Group Closed.\n\n\t\tBye.");
						instance = null;
						stopping.set(false);
					}
				});
			}
		});
	}

	/**
	 * Returns the event loop thread provider for all clients
	 * @return the event loop thread provider for all clients
	 */
	public EventLoopGroup getEventLoopGroup() {
		return group;
	}

	/**
	 * Returns the channel group where all client channels are registered
	 * @return the shared channel group 
	 */
	public ChannelGroup getChannelGroup() {
		return channelGroup;
	}
	
	/**
	 * Indicates if OctoShared is being shut down
	 * @return true if OctoShared is being shut down, false otherwise
	 */
	public boolean isStopping() {
		return stopping.get();
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}
}
