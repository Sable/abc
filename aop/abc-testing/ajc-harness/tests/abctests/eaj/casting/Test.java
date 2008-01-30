import org.aspectj.testing.Tester;

import org.aspectbench.eaj.lang.reflect.CastSignature;

public class Test
{
    public static void main(String[] args)
    {
        int x;
        short y;

        x = 50000;
        y = (short) x;
	Tester.expectEvent("Warning: precision lost casting 50000 to a short");
	Tester.checkAllEvents();
    }
}

aspect BoundsCheck
{
    before(int x):
        cast(short) && args(x)
    {
        CastSignature s = (CastSignature)
                thisJoinPointStaticPart.getSignature();

        if (x > Short.MAX_VALUE || x < Short.MIN_VALUE)
            Tester.event("Warning: precision lost casting " + 
                    x + " to a " +
                    s.getCastType().getName());
    }
}
