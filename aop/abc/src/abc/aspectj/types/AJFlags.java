/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.aspectj.types;

import polyglot.types.Flags;

/**
 * 
 * @author Oege de Moor
 *
 */
public class AJFlags extends Flags {

    // all these flags are pretty-printed as comments
    // to maintain source-to-source compatibility when pretty-printing intermediate
    // ASTs from Polyglot
    public static final Flags PRIVILEGEDASPECT  =  createFlag("/* privilegedaspect */", null);
    public static final Flags ASPECTCLASS =  createFlag("/* aspectclass */",null);
    public static final Flags INTERTYPE = createFlag("/* intertype */",null);
    public static final Flags INTERFACEORIGIN = createFlag("/* interfaceorigin */",null);

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
