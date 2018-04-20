/*
Copyright (c) 2018, Peer Törngren
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the p2r-foxport project nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package se.p2r.foxport.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import se.p2r.foxport.internal.exceptions.FatalException;


/**
 * Logging utils.
 * 
 * @author peer
 *
 */
public class Log {

	private Log() {	}

	public static final Logger LOG = createLogger();
	
	private static Logger createLogger() {
		// read log configuration
	     InputStream stream = Log.class.getClassLoader().getResourceAsStream("logging.properties");
	     if (stream!=null) {
	         try {
				LogManager.getLogManager().readConfiguration(stream);
			} catch (SecurityException | IOException e) {
				String msg = "Unable to read log configuration. Proceeding with default configuration. Cause: "+e.getMessage();
				Logger.getAnonymousLogger().warning(msg);
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					throw new FatalException("Unable to close stream", e);
				};
			}
	     }
	     
	     // create logger
         return Logger.getLogger(Log.class.getName());
	}

	public static void log(String msg) {
		LOG.info(msg);
	}

	public static void debug(String msg) {
		LOG.fine(msg);
	}
	
	public static void fatal(IOException e) {
		LOG.log(Level.SEVERE, "Fatal error, aborting", e);
	}

	public static void error(String msg) {
		LOG.severe(msg);
	}

}
