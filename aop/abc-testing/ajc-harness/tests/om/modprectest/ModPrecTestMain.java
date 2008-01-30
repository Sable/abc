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
import org.aspectj.testing.Tester;

/**
 * @author Neil Ongkingco
 *
 */
public class ModPrecTestMain {
    public static String s = "";
    public static final String correct = "Ext3;B1;B2;B3;A1;A2;A3;C1;C2;C3;Ext1;Y1;Y2;Y3;X1;X2;X3;Ext2;";
    public static void main(String[] args) {
        f();
        System.out.println(s);
        Tester.checkEqual(s.compareTo(correct), 0);
    }
    public static void f() {}
}
aspect A1 {
    protected String getName() {
        return "A1";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect A2 {
    protected String getName() {
        return "A2";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect A3 {
    protected String getName() {
        return "A3";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect B1 {
    protected String getName() {
        return "B1";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect B2 {
    protected String getName() {
        return "B2";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect B3 {
    protected String getName() {
        return "B3";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect C1 {
    protected String getName() {
        return "C1";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect C2 {
    protected String getName() {
        return "C2";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect C3 {
    protected String getName() {
        return "C3";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect X1 {
    protected String getName() {
        return "X1";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect X2 {
    protected String getName() {
        return "X2";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect X3 {
    protected String getName() {
        return "X3";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect Y1 {
    protected String getName() {
        return "Y1";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect Y2 {
    protected String getName() {
        return "Y2";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect Y3 {
    protected String getName() {
        return "Y3";
    }
    before() : call(* f()) {
        ModPrecTestMain.s += getName() + ";";
    }
}
aspect Ext1 {
    declare precedence : A1, Ext1, X1;
	protected String getName() {
	    return "Ext1";
	}
	before() : call(* f()) {
	    ModPrecTestMain.s += getName() + ";";
	}
}
aspect Ext2 {
    declare precedence : X1, Ext2;
	protected String getName() {
	    return "Ext2";
	}
	before() : call(* f()) {
	    ModPrecTestMain.s += getName() + ";";
	}
}
aspect Ext3 {
    declare precedence : Ext3, A1;
	protected String getName() {
	    return "Ext3";
	}
	before() : call(* f()) {
	    ModPrecTestMain.s += getName() + ";";
	}
}