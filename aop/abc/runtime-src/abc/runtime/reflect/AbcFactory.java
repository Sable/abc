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


package abc.runtime.reflect;

import abc.lang.reflect.CastSignature;
import org.aspectj.runtime.reflect.Factory;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public class AbcFactory extends Factory
{
    Class lexicalClass;
    ClassLoader lookupClassLoader;
    String filename;

    public AbcFactory(String filename, Class lexicalClass)
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

    // shouldn't have to do this........
 
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l, int c)
    {
        return super.makeSJP(kind, sig, l, c);
    }

    public MethodSignature makeMethodSig(String stringRep)
    {
        return super.makeMethodSig(stringRep);
    }

    public ConstructorSignature makeConstructorSig(String stringRep)
    {
        return super.makeConstructorSig(stringRep);
    }

    public FieldSignature makeFieldSig(String stringRep)
    {
        return super.makeFieldSig(stringRep);
    }

    public AdviceSignature makeAdviceSig(String stringRep)
    {
        return super.makeAdviceSig(stringRep);
    }

    public InitializerSignature makeInitializerSig(String stringRep)
    {
        return super.makeInitializerSig(stringRep);
    }

    public CatchClauseSignature makeCatchClauseSig(String stringRep)
    {
        return super.makeCatchClauseSig(stringRep);
    }

    public SourceLocation makeSourceLoc(int line, int col)
    {
        return super.makeSourceLoc(line, col);
    }
}
