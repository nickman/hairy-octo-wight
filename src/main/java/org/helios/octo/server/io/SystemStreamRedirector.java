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

import io.netty.channel.Channel;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Title: SystemStreamRedirector</p>
 * <p>Description: Utility to redirect out and err streams back to the calling client.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.octo.server.io.SystemStreamRedirector</code></p>
 */

public class SystemStreamRedirector extends PrintStream {
	/** The original system out print stream */
	public static final PrintStream SYSTEM_OUT = System.out;
	/** The original system err print stream */
	public static final PrintStream SYSTEM_ERR = System.err;
	
	/** A non channel stream redirector for std out */
	private static final SystemStreamRedirector outRedirector = new SystemStreamRedirector(true, SYSTEM_OUT);
	/** A non channel stream redirector for std err */
	private static final SystemStreamRedirector errRedirector = new SystemStreamRedirector(false, SYSTEM_ERR);
	
	/** A thread local for thread's output streams */
	private static InheritableThreadLocal<PrintStream> setOutStream = new InheritableThreadLocal<PrintStream>() {
		protected PrintStream initialValue() {
			return SYSTEM_OUT; 
		}
	};
	
	/** A thread local for thread's error streams */
	private static InheritableThreadLocal<PrintStream> setErrStream = new InheritableThreadLocal<PrintStream>() {
		protected PrintStream initialValue() {
			return SYSTEM_ERR; 
		}
	};
	
	/** Indicates if the system redirector is globally installed */
	private static final AtomicBoolean installed = new AtomicBoolean(false);
	
	/** Indicates if this redirector is for std out */
	protected final boolean isStdOut;
	/** The channel the print stream should write to if not null */
	protected final Channel channel;
	
	
	
	/**
	 * Installs the global system redirector if it is not installed already
	 */
	public static void install() {
		if(!installed.get()) {
			System.setOut(outRedirector);
			System.setErr(errRedirector);
			installed.set(true);
		} 
	}
	/**
	 * Uninstalls the global system redirector if it is installed 
	 */	
	public static void uninstall() {
		if(installed.get()) {
			System.setOut(SYSTEM_OUT);
			System.setErr(SYSTEM_ERR);
			installed.set(false);
		} 
	}

	
	/**
	 * Determines if either stdout or stderr are redirected
	 * @return true if stdout or stderr is redirected, false otherwise
	 */
	public static boolean isInstalledOnCurrentThread() {
		return (
				!setOutStream.get().equals(SYSTEM_OUT) ||
				!setErrStream.get().equals(SYSTEM_ERR)
		);
	}
	
	/**
	 * Redirects the out and error streams for the current thread
	 * @param outPs The output stream redirect for Standard Out
	 * @param errPs The output stream redirect for Standard Err
	 */
	public static void set(PrintStream outPs, PrintStream errPs) {
		if(!installed.get()) {
			throw new RuntimeException("The SystemRedirector is not installed");			
		}
		if(outPs==null) {
			throw new RuntimeException("Out PrintStream was null");
		}
		setOutStream.set(outPs);
		setErrStream.set(errPs==null ? outPs : errPs);
	}
	
	/**
	 * Redirects the out and error streams for the current thread to the passed channel
	 * @param channel The channel to write to
	 */
	public static void set(Channel channel) {
		if(!installed.get()) {
			throw new RuntimeException("The SystemRedirector is not installed");			
		}
		if(channel==null) {
			throw new RuntimeException("Channel was null");
		}
		setOutStream.set(new SystemStreamRedirector(true, channel));
		setErrStream.set(new SystemStreamRedirector(false, channel));		
	}
	
	/**
	 * Resets the out and error streams for the current thread to the default
	 */
	public static void reset() {
		setOutStream.set(SYSTEM_OUT);
		setErrStream.set(SYSTEM_ERR);		
	}
	
	/**
	 * Creates a new SystemStreamRedirector
	 * @param isStdOut true if this is for std out
	 * @param channel The channel the streamed content should be written to
	 */
	private SystemStreamRedirector(boolean isStdOut, Channel channel) {
		super(ChannelOutputStream.getInstance(isStdOut, channel));
		this.isStdOut = isStdOut;		
		this.channel = channel;		
	}
	
	/**
	 * Creates a new SystemStreamRedirector
	 * @param isStdOut true if this is for std out
	 * @param printStream The print stream to write to
	 */
	private SystemStreamRedirector(boolean isStdOut, PrintStream printStream) {
		super(printStream);
		this.isStdOut = isStdOut;
		channel = null;		
	}
	

}
