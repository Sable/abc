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
public class ModPrecModCycleTest {

    public static void main(String[] args) {
    }
}
aspect A1 {
   declare precedence : X1, A1, X2;
}
aspect A2 {
    
}
aspect B1 {
    
}
aspect B2 {
    
}
aspect C1 {
    
}
aspect C2 {
    
}
aspect X1 {
    
}
aspect X2 {
    
}