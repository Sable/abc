import java.io.*;
import org.aspectj.runtime.internal.*;
import org.aspectj.lang.*;

public class Aspect
{
    static int aspectlevel;
    private static java.lang.Throwable ajc$initFailureCause;
    public static final Aspect ajc$perSingletonInstance;

    static
    {
        java.lang.Throwable r0;

        label_0:
        {
            try
            {
                aspectlevel = 0;
                Aspect.ajc$postClinit();
            }
            catch (Throwable $r1)
            {
                r0 = $r1;
                ajc$initFailureCause = r0;
                break label_0;
            }
        }
    }

    public Aspect()
    {
        super();

        return;
    }

    static void message(java.lang.String r0)
    {
        int i0;

        i0 = 0;

        while (i0 < aspectlevel)
        {
            System.out.print("===+");
            i0 = i0 + 1;
        }

        System.out.println((new StringBuffer(" ")).append(r0).toString());
        return;
    }

    public void ajc$before$Aspect$1$6f3b8613()
    {
        Aspect.message("beginning of method");
        return;
    }

    public void ajc$before$Aspect$2$30cfcf29()
    {
        Aspect.message("Here is some before advice");
        return;
    }

    public int ajc$around$Aspect$3$87b03be6(int i0, org.aspectj.runtime.internal.AroundClosure r1) throws java.lang.Throwable
    {
        int i1, i2, i3, i4, i9;

        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at beginning of around, value of first param is ")).append(i0).toString());
        i1 = i0 + 1;
        Aspect.ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(Aspect.ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel() + 1);
        i2 = 0;
        i3 = 0;

        while (i3 < 10)
        {
            if (i3 % 2 != 0)
            {
                Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message("else inside for");
                i2 = Aspect.ajc$around$Aspect$3$87b03be6proceed(i3, r1);
            }
            else
            {
                Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message("if inside for");
                i4 = i3 * 2;
                i9 = Aspect.ajc$around$Aspect$3$87b03be6proceed(i1 + i3, r1);
                i2 = i9 + i4;
            }

            Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("in loop ")).append(i3).append(" result is ").append(i2).toString());
            i3 = i3 + 1;
        }

        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at end of around, return value is ")).append(i2).toString());
        Aspect.ajc$inlineAccessMethod$Aspect$Aspect$message((new StringBuffer("1: at end of around, mylocalx is ")).append(i1).toString());
        Aspect.ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(Aspect.ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel() - 1);
        return i2;
    }

    static int ajc$around$Aspect$3$87b03be6proceed(int i0, org.aspectj.runtime.internal.AroundClosure r0) throws java.lang.Throwable
    {
        java.lang.Object[] $r1;

        $r1 = new Object[1];
        $r1[0] = Conversions.intObject(i0);
        return Conversions.intValue(r0.run($r1));
    }

    public static Aspect aspectOf()
    {
        Aspect $r0;

        $r0 = ajc$perSingletonInstance;

        if ($r0 == null)
        {
            throw new NoAspectBoundException("Aspect", ajc$initFailureCause);
        }
        else
        {
            return $r0;
        }
    }

    public static boolean hasAspect()
    {
        if (ajc$perSingletonInstance == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private static void ajc$postClinit()
    {
        ajc$perSingletonInstance = new Aspect();
        return;
    }

    public static void ajc$inlineAccessMethod$Aspect$Aspect$message(java.lang.String r0)
    {
        Aspect.message(r0);
        return;
    }

    public static int ajc$inlineAccessFieldGet$Aspect$Aspect$aspectlevel()
    {
        return aspectlevel;
    }

    public static void ajc$inlineAccessFieldSet$Aspect$Aspect$aspectlevel(int i0)
    {
        aspectlevel = i0;
        return;
    }
}
