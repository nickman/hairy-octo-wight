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
package org.helios.octo.server.invocation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: InvocationRequest</p>
 * <p>Description: Represents an invocation submitted by a remote client</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.invocation.InvocationRequest</code></p>
 */

public class InvocationRequest implements Externalizable {
	/** The script text */
	protected String scriptText;
	/** The arguments to the script */
	protected Object[] arguments;
	/** The client supplied request id */
	protected long requestId;
	
	private final byte[] EMPTY_BYTES = {};
	
	/**
	 * Creates a new InvocationRequest.
	 * FOR EXTERN ONLY
	 */
	public InvocationRequest() {
		
	}
	
	/**
	 * Creates a new InvocationRequest
	 * @param scriptText the script text
	 * @param arguments The arguments to pass when invoking
	 * @param requestId The client supplied request id
	 */
	public InvocationRequest(String scriptText, Object[] arguments, long requestId) {
		this.scriptText = scriptText;
		this.arguments = arguments;
		this.requestId = requestId;
	}


	/**
	 * {@inheritDoc}
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(requestId);
		byte[] bytes = scriptText==null ? EMPTY_BYTES : scriptText.getBytes();
		out.writeInt(bytes.length);
		if(bytes.length>0) {
			out.write(bytes);
		}
		int argCount = arguments==null ? 0 : arguments.length;
		out.writeInt(argCount);
		if(argCount>0) {			
			List<Object> validatedArgs = new ArrayList<Object>(argCount);
			if(arguments!=null) {
				for(int i = 0; i < argCount; i++) {
					if(arguments[i] != null) {
						validatedArgs.add(arguments[i]);
						out.write(1);
						out.writeObject(arguments[i]);
					} else {
						out.write(0);
					}
				}				
			}
		}
		System.out.println("Wrote out requestId [" + requestId + "]");
	}


	/**
	 * {@inheritDoc}
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		requestId = in.readLong();
		int strLen = in.readInt();
		byte[] bytes = new byte[strLen];
		in.read(bytes);
		scriptText = new String(bytes);
		int argCount = in.readInt();
		arguments = new Object[argCount];
		for(int i = 0; i < argCount; i++) {
			if(in.read()==1) {
				arguments[i] = in.readObject();
			} else {
				arguments[i] = null;
			}
		}
		System.out.println("Read in requestId [" + requestId + "]");
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder b = new StringBuilder("InvocationRequest [");
		b.append("\n\trequestId:").append(requestId);
		b.append("\n\tScript:").append(scriptText);
		b.append("\n\tArguments:");
		if(arguments!=null) {
			for(Object o: arguments) {
				b.append("\n\t\t [").append(o).append("]");
			}
		}
		b.append("\n]");
		return b.toString();
	}
	
}
