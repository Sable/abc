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

package org.aspectbench.eaj.runtime.reflect;

import org.aspectbench.eaj.lang.reflect.CastSignature;
import org.aspectbench.runtime.reflect.SignatureImpl;
import org.aspectbench.runtime.reflect.StringMaker;

public class CastSignatureImpl extends SignatureImpl
                               implements CastSignature
{
    Class castType;
    
    CastSignatureImpl(int modifiers, String name, Class declaringType, 
                      Class castType)
    {
        super(modifiers, name, declaringType);
        this.castType = castType;
    }
    
    CastSignatureImpl(String stringRep)
    {
        super(stringRep);
    }
    
    public Class getCastType()
    {
        if (castType == null)
            castType = extractType(3);
        return castType;
    }
    
    public String toString(StringMaker sm)
    {
        return "(" + getCastType().getName() + ") ...";
    } 
}
