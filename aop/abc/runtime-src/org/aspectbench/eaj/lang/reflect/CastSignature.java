/* *******************************************************************
 * Copyright (c) 2004 Julian Tibble. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Julian Tibble     initial implementation
 * ******************************************************************/

package org.aspectbench.eaj.lang.reflect;

import org.aspectj.lang.Signature;

public interface CastSignature extends Signature
{
    public Class getCastType();
}
