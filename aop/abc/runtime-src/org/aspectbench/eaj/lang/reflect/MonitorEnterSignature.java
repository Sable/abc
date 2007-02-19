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

package org.aspectbench.eaj.lang.reflect;

import org.aspectj.lang.Signature;

public interface MonitorEnterSignature extends Signature
{
    public Class getSynchronizedObjectType();
}
