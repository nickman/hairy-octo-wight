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

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.MessageList;

import org.apache.log4j.Logger;
import org.helios.octo.server.io.SystemStreamRedirector;
import org.helios.octo.util.NettyUtil;

/**
 * <p>Title: InvocationHandler</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.InvocationHandler</code></p>
 */
@Sharable
public class InvocationHandler extends ChannelInboundHandlerAdapter {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Creates a new InvocationHandler
	 */
	public InvocationHandler() {
		
	}
	
	/**
	 * {@inheritDoc}
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelActive(io.netty.channel.ChannelHandlerContext)
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {		
		super.channelActive(ctx);
	}
	

	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs) throws Exception {
		StringBuilder b = new StringBuilder("\n======= Invocation ======");
		int cnt = 0;
		for(Object obj: msgs) {
			b.append("\n\t [").append(cnt).append("] ");
			if(obj instanceof ByteBuf) {
				ByteBuf buff = (ByteBuf)obj;
				log.info(NettyUtil.formatBuffer(buff));
				byte[] bytes = new byte[buff.readableBytes()];
				buff.readBytes(bytes);
				b.append(new String(bytes));
			} else {
				if(obj.getClass().isArray()) {
					b.append("[").append(Arrays.toString((Object[])obj)).append("]");
				} else {
					b.append("[").append(obj).append("]");
				}
			}
			
			cnt++;
		}
		b.append("\n=================================");
		SystemStreamRedirector.install();
		SystemStreamRedirector.set(ctx.channel());
		b.append("\n");
		byte[] bytes = b.toString().getBytes();
		log.info("Out Stream:" + System.out.getClass().getSimpleName());
		System.out.write(bytes);
		System.out.println(b);
		System.out.flush();
		msgs.releaseAllAndRecycle();
	}	

}
