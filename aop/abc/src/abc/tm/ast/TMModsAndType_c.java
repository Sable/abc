/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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

package abc.tm.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * @author Julian Tibble
 *
 * This class intentionally does not extend Node_c because it is just
 * a placeholder class to make the parser simpler - the members of this
 * class are copied to the parent TMDecl_c.
 */
public class TMModsAndType_c implements TMModsAndType
{
    private Flags flags;
    private boolean isPerThread;
    private boolean isAround;
    private TypeNode type;
    private AdviceSpec before_or_around;
    private AdviceSpec after;

    TMModsAndType_c(Flags flags, boolean isPerThread,
                    AdviceSpec before_or_around, AdviceSpec after,
                    boolean isAround, TypeNode type)
    {
        this.flags = flags;
        this.isPerThread = isPerThread;
        this.isAround = isAround;
        this.type = type;
        this.before_or_around = before_or_around;
        this.after = after;
    }

    public Flags getFlags()
    {
        return flags;
    }

    public boolean isPerThread()
    {
        return isPerThread;
    }

    public boolean isAround()
    {
        return isAround;
    }

    public TypeNode getReturnType()
    {
        return type;
    }

    public AdviceSpec beforeOrAroundSpec()
    {
        return before_or_around;
    }

    public AdviceSpec afterSpec()
    {
        return after;
    }
}
