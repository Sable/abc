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


package org.aspectbench.eaj.runtime.reflect;

import org.aspectbench.eaj.lang.reflect.*;
import org.aspectj.lang.Signature;
import org.aspectj.lang.JoinPoint;
import org.aspectbench.runtime.reflect.Factory;

public class EajFactory extends Factory
{
    Class lexicalClass;
    ClassLoader lookupClassLoader;
    String filename;

    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l, int c,
            int offset) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, makeSourceLoc(l, c),
                offset);
    }

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
    
    public ThrowSignature makeThrowSig(String stringRep) {
        ThrowSignatureImpl ret = new ThrowSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public ArrayGetSignature makeArrayGetSig(String stringRep) {
    	ArrayGetSignatureImpl ret = new ArrayGetSignatureImpl(stringRep);
    	ret.setLookupClassLoader(lookupClassLoader);
    	return ret;
    }

    public ArraySetSignature makeArraySetSig(String stringRep) {
    	ArraySetSignatureImpl ret = new ArraySetSignatureImpl(stringRep);
    	ret.setLookupClassLoader(lookupClassLoader);
    	return ret;
    }
    
    public MonitorEnterSignature makeMonitorEnterSig(String stringRep) {
        MonitorEnterSignatureImpl ret = new MonitorEnterSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public MonitorExitSignature makeMonitorExitSig(String stringRep) {
        MonitorExitSignatureImpl ret = new MonitorExitSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
}
