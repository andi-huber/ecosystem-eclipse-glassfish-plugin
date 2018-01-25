/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import org.eclipse.glassfish.tools.sdk.server.FetchLog;
import org.eclipse.ui.console.MessageConsoleStream;

public class LogReader implements Runnable {
	
	private FetchLog logFetcher;
	private MessageConsoleStream output;
	private CountDownLatch latch;
	private ILogFilter filter;

	LogReader(FetchLog logFetcher, MessageConsoleStream outputStream, CountDownLatch latch, ILogFilter filter) {
		this.logFetcher = logFetcher;
		this.output = outputStream;
		this.latch = latch;
		this.filter = filter;
	}

	@Override
	public void run()
	{
		try
		{
		    BufferedReader reader = new BufferedReader( new InputStreamReader( logFetcher.getInputStream(), StandardCharsets.UTF_8 ) );
		    
			for( String line = null; (line = reader.readLine()) != null; )
			{
				//System.out.println(line);
				line = filter.process(line);
				if (line != null) {
					//output.println("line:");
					output.println(line);
				}
			}
			output.flush();
		} catch (IOException e) {
			//this happens when input stream is closed, no need to print
			//e.printStackTrace();
		} finally {
			//System.out.println("end, closing streams...");
			logFetcher.close();
			latch.countDown();
		}
	}

	public void stop() {
		//System.out.println("stop called...");
		logFetcher.close();
	}

}

