import java.io.*;
import org.aspectj.runtime.internal.*;

public class Test
{
    static int outsidex;
    int a;

    static
    {
        outsidex = 3;
    }

    public Test()
    {
        super();

        a = 12;
        return;
    }

    public int c(int i0)
    {
        int i1;

        Aspect.aspectOf().ajc$before$Aspect$1$6f3b8613();
        System.out.println((new StringBuffer("I\'m at the beginning of c,  being called with ")).append(i0).toString());

        if (i0 == 0)
        {
            i1 = 0;
        }
        else
        {
            i1 = i0 * i0;
        }

        System.out.println((new StringBuffer("I\'m at the end of c,  returning with ")).append(i1).toString());
        return i1;
    }

    public static void main(java.lang.String[] r0)
    {
        int i0, i1, i2, i3, i4;
        Test r1, r2, r4;

        i0 = 4;
        r1 = new Test();
        System.out.println("--- calling c(4) --------");
        i1 = i0;
        r2 = r1;
        Aspect.aspectOf().ajc$before$Aspect$2$30cfcf29();
        i2 = Test.c_aroundBody1$advice(r2, i1, Aspect.aspectOf(), i1, null);
        System.out.println("\n--- calling c(5) ---------");
        i3 = i0 + 1;
        r4 = r1;
        Aspect.aspectOf().ajc$before$Aspect$2$30cfcf29();
        i4 = Test.c_aroundBody3$advice(r4, i3, Aspect.aspectOf(), i3, null);
        System.out.println((new StringBuffer("final values are ")).append(i2).append(" ").append(i4).toString());
        return;
    }

    private static final int c_aroundBody0(Test r0, int i0)
    {
        return r0.c(i0);
    }

    private static final int c_aroundBody1$advice(Test r0, int i0, Aspect r1, int i1, org.aspectj.runtime.internal.AroundClosure r2)
    {
        int i2, i3, i4, i5, i6, i7, i11;
        org.aspectj.runtime.internal.AroundClosure r4, r5;

        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at beginning of around, value of first param is ")).append(i1).toString());
        i2 = i1 + 1;
        Aspect.ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(Aspect.ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel() + 1);
        i3 = 0;
        i4 = 0;

        while (i4 < 10)
        {
            if (i4 % 2 != 0)
            {
                Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message("else inside for");
                r5 = r2;
                i7 = i4;
                i3 = Test.c_aroundBody0(r0, i7);
            }
            else
            {
                Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message("if inside for");
                i5 = i4 * 2;
                r4 = r2;
                i6 = i2 + i4;
                i11 = Test.c_aroundBody0(r0, i6);
                i3 = i11 + i5;
            }

            Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("in loop ")).append(i4).append(" result is ").append(i3).toString());
            i4 = i4 + 1;
        }

        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at end of around, return value is ")).append(i3).toString());
        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at end of around, mylocalx is ")).append(i2).toString());
        Aspect.ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(Aspect.ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel() - 1);
        return i3;
    }

    private static final int c_aroundBody2(Test r0, int i0)
    {
        return r0.c(i0);
    }

    private static final int c_aroundBody3$advice(Test r0, int i0, Aspect r1, int i1, org.aspectj.runtime.internal.AroundClosure r2)
    {
        int i2, i3, i4, i5, i6, i7, i11;
        org.aspectj.runtime.internal.AroundClosure r4, r5;

        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at beginning of around, value of first param is ")).append(i1).toString());
        i2 = i1 + 1;
        Aspect.ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(Aspect.ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel() + 1);
        i3 = 0;
        i4 = 0;

        while (i4 < 10)
        {
            if (i4 % 2 != 0)
            {
                Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message("else inside for");
                r5 = r2;
                i7 = i4;
                i3 = Test.c_aroundBody2(r0, i7);
            }
            else
            {
                Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message("if inside for");
                i5 = i4 * 2;
                r4 = r2;
                i6 = i2 + i4;
                i11 = Test.c_aroundBody2(r0, i6);
                i3 = i11 + i5;
            }

            Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("in loop ")).append(i4).append(" result is ").append(i3).toString());
            i4 = i4 + 1;
        }

        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at end of around, return value is ")).append(i3).toString());
        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at end of around, mylocalx is ")).append(i2).toString());
        Aspect.ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(Aspect.ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel() - 1);
        return i3;
    }
}
