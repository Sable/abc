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

package abc.tm.weaving.aspectinfo;

import polyglot.util.Position;

import abc.weaving.aspectinfo.*;

import java.util.*;

/** 
 * This is used for advice generated to implement tracematches,
 * which have different precedence rules to regular advice.
 *
 *  @author Julian Tibble
 */
public class TMAdviceDecl extends AdviceDecl
{
    protected String tm_id;
    protected Position tm_pos;
    protected int kind;

    public TMAdviceDecl(AdviceSpec spec, Pointcut pc, MethodSig impl,
                        Aspect aspct, int jp, int jpsp, int ejp,
                        List methods, Position pos, String tm_id,
                        Position tm_pos, int kind)
    {
        super(spec, pc, impl, aspct, jp, jpsp, ejp, methods, pos);
        this.tm_id = tm_id;
        this.tm_pos = tm_pos;
        this.kind = kind;
    }

    public String getTraceMatchID()
    {
        return tm_id;
    }

    public Position getTraceMatchPosition()
    {
        return tm_pos;
    }

    public int getPrecWithinTM()
    {
        return kind;
    }

    public boolean isSome()
    {
        return (kind == abc.tm.ast.TMAdviceDecl.SOME);
    }

    public boolean isSynch()
    {
    	return (kind == abc.tm.ast.TMAdviceDecl.SYNCH);
    }
 
    public boolean isBody()
    {
        return (kind == abc.tm.ast.TMAdviceDecl.BODY);
    }

    public boolean isOther()
    {
        return (kind == abc.tm.ast.TMAdviceDecl.OTHER);
    }

    /**
     * override warning message (when advice does not apply
     * to any static join points) to say "symbol" instead of
     * "advice declaration"
     */
    public String getApplWarning()
    {
        if (isOther() && super.getApplWarning() != null)
            return "Symbol doesn't match anywhere";
        else
            return null;
    }
}
