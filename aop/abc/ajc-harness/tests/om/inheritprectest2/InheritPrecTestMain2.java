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
public class InheritPrecTestMain2 {

    public static String s = "";
    public static final String correct = "B1;B2;B3;A1;A2;A3;C1;C2;C3;";
        
    public static void main(String[] args) {
        f();
        System.out.println(s);
        Tester.checkEqual(s.compareTo(correct), 0);
    }
    
    private static void f() {
    }
}
aspect A1{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "A1;";
    }
}

aspect A2{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "A2;";
    }
}
aspect A3{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "A3;";
    }
}
aspect B1{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "B1;";
    }
}
aspect B2{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "B2;";
    }
}
aspect B3{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "B3;";
    }
}
aspect C1{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "C1;";
    }
}
aspect C2{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "C2;";
    }
}
aspect C3{
    before() : call(* f()) {
        InheritPrecTestMain2.s += "C3;";
    }
}