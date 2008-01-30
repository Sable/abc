/*
 * Created on 10-Feb-2005
 */
package abcexer2.runtime.reflect;


import org.aspectbench.runtime.reflect.Factory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import abcexer2.lang.reflect.ArrayGetSignature;

/**
 * @author Sascha Kuzins
 *
 */
public class Abcexer2Factory extends Factory {
    Class lexicalClass;
    ClassLoader lookupClassLoader;
    String filename;

    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l, int c,
            int offset) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, makeSourceLoc(l, c),
                offset);
    }

    public Abcexer2Factory(String filename, Class lexicalClass)
    {
        super(filename, lexicalClass);
        this.filename = filename;
        this.lexicalClass = lexicalClass;
        lookupClassLoader = lexicalClass.getClassLoader();
    }
    
    public ArrayGetSignature makeArrayGetSig(String stringRep) {
    	ArrayGetSignatureImpl ret = new ArrayGetSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
}
