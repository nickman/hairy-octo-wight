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
package org.helios.octo.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageList;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import org.apache.log4j.Logger;

/**
 * <p>Title: InvocationDecoder</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.InvocationDecoder</code></p>
 */

public class InvocationDecoder extends ByteToMessageDecoder {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	protected static final AttributeKey<Integer> STR_SIZE_KEY = new AttributeKey<Integer>("STR_SIZE");
	protected static final AttributeKey<Boolean> HAS_ARGS_KEY = new AttributeKey<Boolean>("HAS_ARGS");


	/**
	 * {@inheritDoc}
	 * @see io.netty.handler.codec.ByteToMessageDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, io.netty.channel.MessageList)
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, MessageList<Object> out) throws Exception {
		int _strSize = -1;
		boolean _hasArgs = false;
		Attribute<Integer> strSize = ctx.attr(STR_SIZE_KEY);
		Attribute<Boolean> hasArgs = ctx.attr(HAS_ARGS_KEY);
		if(strSize.get()==null) {
			if (in.readableBytes() < 5) {
	            return;
	        }
			_strSize = in.readInt();
			_hasArgs = in.readByte()==1 ? true : false;
			strSize.set(_strSize);
			hasArgs.set(_hasArgs);
	    }
		if(_strSize > 0) {
			if (in.readableBytes() >= _strSize) {
				strSize.set(null);
				hasArgs.set(null);
				byte[] str = new byte[_strSize];
				in.readBytes(str);
				String s = new String(str);
				log.info("Script:" + s + "  hasArgs:" + _hasArgs);				
				out.add(s);
				out.add(_hasArgs);
				ctx.pipeline().remove(this);
//				StringBuilder b = new StringBuilder("\n========== Remaining Pipeline ==========");
//				for(String pn: ctx.pipeline().names()) {
//					b.append("\n\t").append(pn);
//				}
//				log.info(b);
			}			
		}
	}	


}
