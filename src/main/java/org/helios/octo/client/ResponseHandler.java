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

import static org.helios.octo.client.Response.FORWARD;
import static org.helios.octo.client.Response.RESPONSE_TYPE;
import static org.helios.octo.client.Response.STREAM_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.util.AttributeKey;

import org.jboss.logging.Logger;

/**
 * <p>Title: ResponseHandler</p>
 * <p>Description: Handles the response stream back from the server</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.client.ResponseHandler</code></p>
 */
@Sharable
public class ResponseHandler extends ReplayingDecoder<Response> {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Creates a new ResponseHandler
	 */
	public ResponseHandler() {
		super(Response.REQUEST_ID);
	}
	
	/**
	 * {@inheritDoc}
	 * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, io.netty.channel.MessageList)
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageList<Object> out) {
		long bytesAvailable = super.actualReadableBytes();
		log.info("Replay Bytes Available:" + bytesAvailable);
		if(bytesAvailable<1) return;
		switch(state()) {		
		case REQUEST_ID:
			log.info("--------------------------------->Processing [" + state() + "]");
			long reqId = in.readLong();
			ctx.channel().attr(OctoShared.REQ_ID).set(reqId);
			checkpoint(RESPONSE_TYPE);
			log.info("REQUEST_ID:" + reqId);
		case RESPONSE_TYPE:
			log.info("--------------------------------->Processing [" + state() + "]");
			byte responseType = in.readByte();
			log.info("RESPONSE_TYPE:" + responseType);
			if(responseType==0) {
				checkpoint(STREAM_TYPE);				
			} else {
//				if(!ctx.pipeline().get(name))
				ctx.pipeline().addAfter(OctoShared.RESPONSE_HANDLER, OctoShared.OBJECT_DECODER, new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
				out.add(in.copy(0, super.actualReadableBytes()));							
				checkpoint(FORWARD);
				return;
			}
		case STREAM_TYPE:
			log.info("--------------------------------->Processing [" + state() + "]");
			byte streamType = in.readByte();
			ctx.channel().attr(OctoShared.STREAM).set(streamType);
			log.info("STREAM_TYPE:" + streamType);
			checkpoint(FORWARD);
			return;
//			out.add(in.readBytes(super.actualReadableBytes()));			
//			return;
		case FORWARD:
			log.info("--------------------------------->Processing [" + state() + "]");
			log.info("Forwarding [" + super.actualReadableBytes() + "]....");
			out.add(in.readBytes(super.actualReadableBytes()));
			log.info("Forward Complete. Remaining: [" + super.actualReadableBytes() + "]");
			return;		
		default:
			log.warn("Unexpected state [" + state() + "]");
			break;			
		}
	}
	
}
