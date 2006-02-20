/* abc - The AspectBench Compiler
 * Copyright (C) 2006
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

/**
 * @author Neil Ongkingco
 *
 */
public class ModPrecAbstractTestMain {
    public static String s = "";
    public static void main(String[] args) {
        f();
        System.out.println(s);
    }
    public static void f() {}
}
abstract aspect Q {
    abstract protected String getName(); 
    before() : call(* f()) {
        ModPrecAbstractTestMain.s += getName() + ";";
    }    
}
aspect A1 extends Q{
    protected String getName() {
        return "A1";
    }
}

aspect B1 extends Q{
    protected String getName() {
        return "B1";
    }
}

aspect C1 extends Q{
    protected String getName() {
        return "C1";
    }
}

aspect X1 extends Q{
    protected String getName() {
        return "X1";
    }
}

aspect Y1 extends Q{
    protected String getName() {
        return "Y1";
    }
}

aspect Ext1 extends Q{
    declare precedence : Ext1, Ext2, Ext3, A1, B1, C1, X1, Y1;
	protected String getName() {
	    return "Ext1";
	}
}
aspect Ext2 extends Q{
	protected String getName() {
	    return "Ext2";
	}
}
aspect Ext3 extends Q{
	protected String getName() {
	    return "Ext3";
	}
}