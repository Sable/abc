
package arc.aspectj.types;

import polyglot.types.Flags;

public class AspectJFlags extends Flags {

    public static final Flags PRIVILEGED  =  createFlag("privileged", null);
    public static final Flags ASPECT      =  createFlag("aspect",null);

    public AspectJFlags() {
	super(0);
    }

    public static Flags aspect(Flags f) {
	return f.set(ASPECT);
    }
	   
    public static Flags clearAspect(Flags f) {
	 return f.clear(ASPECT);
    }

    public static boolean isAspect(Flags f) {
	   return f.contains(ASPECT);
    }
	
    public static Flags privileged(Flags f) {
       return f.set(PRIVILEGED);
    }
	   
    public Flags clearPrivileged(Flags f) {
       return f.clear(PRIVILEGED);
    }

    public boolean isPrivileged(Flags f) {
       return f.contains(PRIVILEGED);
   }
   
}
