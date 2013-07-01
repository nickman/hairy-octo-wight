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
package org.helios.octo.server.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Title: ChannelOutputStream</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.io.ChannelOutputStream</code></p>
 */

public class ChannelOutputStream extends OutputStream {
	/** The channel the print stream will stream to */
	protected final Channel channel;
	/** The output stream provided to facade the print stream */
	protected final AtomicReference<ByteBufOutputStream> os = new AtomicReference<ByteBufOutputStream>(null);
	/** A print stream for this output stream */
	protected final PrintStream ps;
	/** Indicates if this is std-out or std-err */
	protected final boolean isStdOut;
	/** The chanel context of the downstream aggregator for this stream */
	protected final ChannelHandlerContext targetCtx;
	/** All the OUT streams */
	
	protected static final Map<Channel, ChannelOutputStream> OUT = new ConcurrentHashMap<Channel, ChannelOutputStream>();
	/** All the ERR streams */
	protected static final Map<Channel, ChannelOutputStream> ERR = new ConcurrentHashMap<Channel, ChannelOutputStream>();
	
	/**
	 * Acquires an output stream for the passed channel
	 * @param isStdOut true for std-out, false for std-err
	 * @param channel The channel
	 * @return a channel output stream
	 */
	public static ChannelOutputStream getInstance(boolean isStdOut, Channel channel) {
		final Map<Channel, ChannelOutputStream> map = isStdOut ? OUT : ERR;
		ChannelOutputStream cos = map.get(channel);
		if(cos==null) {
			synchronized(map) {
				if(cos==null) {
					cos = new ChannelOutputStream(isStdOut, channel);
					map.put(channel, cos);
					channel.closeFuture().addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future) throws Exception {
							map.remove(future.channel());							
						}
					});
				}
			}
		}
		return cos;
	}
	
	/**
	 * Creates a new ChannelOutputStream
	 * @param isStdOut true if this is std-out, false for std-err
	 * @param channel The channel the output stream will write to
	 */
	private ChannelOutputStream(boolean isStdOut, Channel channel) {
		this.channel = channel;
		this.isStdOut = isStdOut;
		os.set(new ByteBufOutputStream(channel.alloc().directBuffer()));
		ps = new PrintStream(this, false);
		targetCtx = channel.pipeline().context((this.isStdOut ? "out" : "err"));
	}
	
	/**
	 * Returns a print stream that writes to this output stream
	 * @return a print stream that writes to this output stream
	 */
	public PrintStream getPrintStream() {
		return ps;
	}
	
//	/**
//	 * {@inheritDoc}
//	 * @see java.io.OutputStream#flush()
//	 */
//	@Override
//	public void flush() throws IOException {
//		ByteBufOutputStream toFlush = os.getAndSet(new ByteBufOutputStream(channel.alloc().directBuffer()));		
//		// first byte for STREAM, second byte for std-err/std-out flag
//		// Prefix:
//			// 1 long for the request id
//			// 1 byte (0) for response type STREAM
//			// 1 byte (0 or 1) for std-out or std-err
//		channel.write(Unpooled.buffer(10).writeLong(System.nanoTime()).writeByte(0).writeByte(isStdOut ? 0 : 1));
//		channel.write(toFlush);		
//		channel.unsafe().flushNow();
//	}

	/**
	 * {@inheritDoc}
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		//os.get().write(b);		
		//targetCtx.write(MessageList.newInstance(Unpooled.buffer(1).writeByte(b)));
		//targetCtx.write(Unpooled.buffer(1).writeByte(b));
		channel.write(Unpooled.buffer(1).writeByte(b));
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		//os.get().write(b);
		//targetCtx.write(MessageList.newInstance(Unpooled.wrappedBuffer(b)));
		//targetCtx.write(Unpooled.wrappedBuffer(b));
		channel.write(Unpooled.wrappedBuffer(b));
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		//os.get().write(b, off, len);		
		//targetCtx.write(MessageList.newInstance(Unpooled.wrappedBuffer(b, off, len)));
		//targetCtx.write(Unpooled.wrappedBuffer(b, off, len));
		channel.write(Unpooled.wrappedBuffer(b, off, len));
	}


}
