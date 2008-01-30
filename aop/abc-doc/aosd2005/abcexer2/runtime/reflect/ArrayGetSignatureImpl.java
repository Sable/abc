/*
 * Created on 10-Feb-2005
 */
package abcexer2.runtime.reflect;

import org.aspectbench.runtime.reflect.SignatureImpl;
import org.aspectbench.runtime.reflect.StringMaker;

import abcexer2.lang.reflect.ArrayGetSignature;

/**
 * @author Sascha Kuzins
 *
 */
public class ArrayGetSignatureImpl extends SignatureImpl implements
		ArrayGetSignature {

   
	ArrayGetSignatureImpl(int modifiers, String name, Class declaringType)
    {
        super(modifiers, name, declaringType);
    }
    
	ArrayGetSignatureImpl(String stringRep)
    {
        super(stringRep);
    }
    
    public String toString(StringMaker sm)
    {
        return "[] ...";
    }
}
