/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.eaj.util;

import java.util.regex.Pattern;

import polyglot.ast.Prefix;
import polyglot.ast.Receiver;
import polyglot.util.Position;

import abc.eaj.ast.EAJNodeFactory;

/**
 * @author Julian Tibble
 */
public class ToReceiver
{
    final private static Pattern dot = Pattern.compile("\\.");
    public static Receiver fromString(EAJNodeFactory nf, Position pos, String longName)
    {
        String[] parts = dot.split(longName);

        if (parts.length == 1)
            return nf.AmbReceiver(pos, parts[0]);

        Prefix p = nf.AmbPrefix(pos, parts[0]);

        for (int i = 1; i < parts.length - 1; i++)
            p = nf.AmbPrefix(pos, p, parts[i]);

        return nf.AmbReceiver(pos, p, parts[parts.length - 1]);
    }
}
