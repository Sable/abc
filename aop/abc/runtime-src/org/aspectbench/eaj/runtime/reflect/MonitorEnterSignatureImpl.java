/* *******************************************************************
 * Copyright (c) 2007 Eric Bodden. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Eric Bodden     initial implementation
 * ******************************************************************/

package org.aspectbench.eaj.runtime.reflect;

import org.aspectbench.eaj.lang.reflect.MonitorEnterSignature;
import org.aspectbench.runtime.reflect.SignatureImpl;
import org.aspectbench.runtime.reflect.StringMaker;

public class MonitorEnterSignatureImpl extends SignatureImpl
                               implements MonitorEnterSignature
{
    
    MonitorEnterSignatureImpl(String stringRep)
    {
        super(stringRep);
    }
    
	public Class getSynchronizedObjectType() {
        return extractType(3);
	} 
    
	public String getSynchronizedVariableName() {
        return extractString(4); 
	} 

	public String toString(StringMaker sm)
    {
        return getSynchronizedObjectType().getName() + " "+ getSynchronizedVariableName();
    }

}
