/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package uk.ac.ox.comlab.abc.eaj.runtime.reflect;

import uk.ac.ox.comlab.abc.eaj.lang.reflect.CastSignature;
import uk.ac.ox.comlab.abc.runtime.reflect.Factory;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public class EajFactory extends Factory
{
    Class lexicalClass;
    ClassLoader lookupClassLoader;
    String filename;

    public EajFactory(String filename, Class lexicalClass)
    {
        super(filename, lexicalClass);
        this.filename = filename;
        this.lexicalClass = lexicalClass;
        lookupClassLoader = lexicalClass.getClassLoader();
    }
    
    public CastSignature makeCastSig(String stringRep) {
        CastSignatureImpl ret = new CastSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
}
