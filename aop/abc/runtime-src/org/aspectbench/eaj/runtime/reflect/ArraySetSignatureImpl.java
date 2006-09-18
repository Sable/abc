/* *******************************************************************
 * Copyright (c) 2004 Pavel Avgustinov. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Pavel Avgustinov    initial implementation
 * ******************************************************************/

package org.aspectbench.eaj.runtime.reflect;

import org.aspectbench.eaj.lang.reflect.ArraySetSignature;
import org.aspectbench.runtime.reflect.SignatureImpl;
import org.aspectbench.runtime.reflect.StringMaker;

public class ArraySetSignatureImpl extends SignatureImpl implements
		ArraySetSignature {

	ArraySetSignatureImpl(Class declaringType) {
		super(0, "arrayset", declaringType);
	}
	
	ArraySetSignatureImpl(String stringRep) {
		super(stringRep);
	}
	
	public String toString(StringMaker sm) {
		return "";
	}

}
