/* *******************************************************************
 * Copyright (c) 2004 Pavel Avgustinov
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Pavel Avgustinov     initial implementation 
 * ******************************************************************/

package abc.testing;

import java.util.List;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.SilentErrorQueue;
import abc.main.CompilerAbortedException;

public class SilentMain extends abc.main.Main {
	private SilentErrorQueue errorQueue;
	public SilentMain(String[] args) throws IllegalArgumentException, CompilerAbortedException {
		super(args);
	}
	
	public ErrorQueue createErrorQueue() {
		errorQueue = new SilentErrorQueue(100,
				"testAbc");
			getAbcExtension().setErrorQueue(errorQueue);
		return errorQueue;
	}
	List getErrors() {
		return (errorQueue != null) ? errorQueue.getErrors() : null;
	}
}
