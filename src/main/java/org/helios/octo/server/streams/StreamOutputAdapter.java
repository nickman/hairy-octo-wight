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
package org.helios.octo.server.streams;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufIndexFinder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.MessageList;
import io.netty.util.AttributeKey;

import org.jboss.logging.Logger;

/**
 * <p>Title: StreamOutputAdapter</p>
 * <p>Description: Accumulates outbound buffers until an EOL is detected, then prepends the approriate indicators and flushes the content out.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.streams.StreamOutputAdapter</code></p>
 */
@Sharable
public class StreamOutputAdapter extends ChannelOutboundHandlerAdapter {
	/** The outbound stream type, 0 for std-out, 1 for std-in */
	protected final byte streamType;
	
	protected final Logger log = Logger.getLogger(getClass());
	
	/** The attribute key for the request id */
	public static final AttributeKey<Long> REQ_ID = new AttributeKey<Long>("ReqId"); 
	/** The attribute key for the buffer accumulation */
	public static final AttributeKey<ByteBuf> ACC = new AttributeKey<ByteBuf>("Accumulation");

	/** The stream response type encoded in a buffer */
	public static final ByteBuf RESPONSE_TYPE = Unpooled.unmodifiableBuffer(Unpooled.buffer(1).writeByte(0));
	
	/**
	 * Creates a new StreamOutputAdapter
	 * @param streamType 0 for std-out, 1 for std-in
	 */
	public StreamOutputAdapter(byte streamType) {
		super();
		this.streamType = streamType;
	}


	@Override
	public void write(ChannelHandlerContext ctx, MessageList<Object> msgs, ChannelPromise promise) {
		MessageList<ByteBuf> buffers = msgs.cast();
		ByteBuf acc = ctx.attr(ACC).get();
		if(acc==null) {
			acc = Unpooled.buffer();
			ctx.attr(ACC).set(acc);
		}
		for(ByteBuf b: buffers) {
			acc.writeBytes(b);
		}
		msgs.releaseAllAndRecycle();
		while(true) {			
			int loc = acc.bytesBefore(ByteBufIndexFinder.LF);
			if(loc==-1) break;
			ByteBuf buf = Unpooled.buffer(10);
			buf.writeLong(System.nanoTime()).writeByte(0).writeByte(streamType);
			//.writeBytes(acc.readBytes(loc+1));
			ChannelHandler se = ctx.pipeline().remove("stringEncoder");
			ctx.write(buf);
			ctx.pipeline().addAfter("out", "stringEncoder", se);
			byte[] bytes = new byte[loc];
			acc.readBytes(bytes);
			String s = new String(bytes);
			log.info("Writing out [" + s + "]");
			ctx.write(s + "\n");
		}
	}
}
