/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools.log;

public class GlassfishStartupConsole extends GlassfishConsole {

	GlassfishStartupConsole(String name, ILogFilter filter) {
		super(name, filter);
	}

	@Override
	public synchronized void stopLogging() {
		// do nothing...
		readers = null;
	}

}
