
package abc.aspectj.types;

import polyglot.types.Flags;

public class AJFlags extends Flags {

    public static final Flags PRIVILEGEDASPECT  =  createFlag("privilegedaspect", null);
    public static final Flags ASPECTCLASS =  createFlag("aspectclass",null);
    public static final Flags INTERTYPE = createFlag("intertype",null);
    public static final Flags INTERFACEORIGIN = createFlag("interfaceorigin",null);

    public AJFlags() {
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
   
   public static Flags intertype(Flags f) {
   		return f.set(INTERTYPE);
   }
   
   public static Flags clearIntertype(Flags f) {
   		return f.clear(INTERTYPE);
   }
   
   public static boolean isIntertype(Flags f) {
   		return f.contains(INTERTYPE);
   }
   
   public static Flags interfaceorigin(Flags f) {
   		return f.set(INTERFACEORIGIN);
   }
   
   public static Flags clearInterfaceorigin(Flags f) {
   		return f.clear(INTERFACEORIGIN);
   }
   
   public static boolean isInterfaceorigin(Flags f) {
   		return f.contains(INTERFACEORIGIN);
   }
   
}
