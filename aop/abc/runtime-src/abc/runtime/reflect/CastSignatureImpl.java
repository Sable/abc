package abc.runtime.reflect;

import abc.lang.reflect.CastSignature;

import org.aspectj.runtime.reflect.*;

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
