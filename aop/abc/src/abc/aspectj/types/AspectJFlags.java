
package abc.aspectj.types;

import polyglot.types.Flags;

public class AspectJFlags extends Flags {

    public static final Flags PRIVILEGEDASPECT  =  createFlag("privilegedaspect", null);
    public static final Flags ASPECTCLASS =  createFlag("aspectclass",null);

    public AspectJFlags() {
	super(0);
    }

    public static Flags aspectclass(Flags f) {
	return f.set(ASPECTCLASS);
    }
	   
    public static Flags clearAspectclass(Flags f) {
	 return f.clear(ASPECTCLASS);
    }

    public static boolean isAspectclass(Flags f) {
	   return f.contains(ASPECTCLASS);
    }
	
    public static Flags privilegedaspect(Flags f) {
       return f.set(PRIVILEGEDASPECT);
    }
	   
    public static Flags clearPrivilegedaspect(Flags f) {
       return f.clear(PRIVILEGEDASPECT);
    }

    public static boolean isPrivilegedaspect(Flags f) {
       return f.contains(PRIVILEGEDASPECT);
   }
   
}
