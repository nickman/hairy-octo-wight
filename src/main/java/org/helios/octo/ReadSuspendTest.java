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
package org.helios.octo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * <p>Title: ReadSuspendTest</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.ReadSuspendTest</code></p>
 */

public class ReadSuspendTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
		
		EmbeddedChannel channel = new EmbeddedChannel(
				new Throttler(), 
				new Decoder()
		); 
	    channel.config().setAutoRead(false);

	    for (int i = 0; i < 20; i++) { 
	    	LOG.info("Writing [" + i + "]");
	    	channel.write(Unpooled.wrappedBuffer("any payload".getBytes())).syncUninterruptibly();
	    	LOG.info("Wrote [" + i + "]");
	    } 

	}
	
	private static final Logger LOG = Logger.getLogger(ReadSuspendTest.class);
	
	public static void log(Object msg) {
		System.out.println(msg);
	}
	
	private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);
	
	private static class Throttler extends MessageToByteEncoder<ByteBuffer> {

	    @Override 
	    protected void encode(ChannelHandlerContext ctx, ByteBuffer msg, ByteBuf out) throws Exception {} // does nothing, just a requited stub implementation

	    @Override 
	    public void read(final ChannelHandlerContext ctx) throws Exception {
	            LOG.info("READ REQUESTED"); // just some debug message
	            Long sleepPeriod = ctx.channel().attr(Decoder.READ_SLEEPER_MILLISECONDS).get();
	            if (sleepPeriod != null) {
	            	LOG.info("SUSPENDING FOR: " + sleepPeriod + " MILLISECONDS!");
	                //TimeUnit.MILLISECONDS.sleep(sleepPeriod);
	                timer.schedule(new Runnable(){
	                	public void run() {
	                		LOG.info("Resuming...");
	                		ctx.channel().read();
	                	}
	                }, sleepPeriod, TimeUnit.MILLISECONDS);
	                ctx.fireChannelReadSuspended();
	            }
	    }
	}
	
	private static class Decoder extends SimpleChannelInboundHandler<ByteBuf> {
		private int tempCounter = 0;
		public static final AttributeKey<Long> READ_SLEEPER_MILLISECONDS = new AttributeKey<Long>("sleep_throttler");
		private static final RecvByteBufAllocator recvByteBufAllocator = new AdaptiveRecvByteBufAllocator(256, 256, 1024);

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
			ctx.channel().config().setRecvByteBufAllocator(recvByteBufAllocator); // this has no visible effect whatsoever - no impact on inbound buffer size! is it a bug? how can I enforce max receiving buffer size?
		}

		@Override public void messageReceived(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
			//    accept three messages AFTER the first message with defined interval between each message; outbound thread will just sleep during this interval
			if (tempCounter++ < 3) {
				ctx.channel().attr(READ_SLEEPER_MILLISECONDS).set(2000L);
			} else {
				ctx.channel().attr(READ_SLEEPER_MILLISECONDS).set(null);
			}
			//    NOTE: need to explicitly call ctx.read(); ctx.channel().config().setAutoRead() has no visible effect...
			//    without this call, overriden read() method in Throttler handler will get called, but it WON'T read updated attribute map
			ctx.read();

			// do the required decoding etc...
		}
	}

}
