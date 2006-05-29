/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis;

import soot.tagkit.AttributeValueException;
import soot.tagkit.StringTag;

/**
 * A tag holding a unit graph state machine.
 * @author Eric Bodden
 */
public class UGStateMachineTag extends StringTag {
    
    public final static String NAME = "UGStateMachineTag";
    
    protected final UGStateMachine sm;

    /**
     * @param sm
     */
    public UGStateMachineTag(UGStateMachine sm) {
        super(sm.toString());
        this.sm = sm;
    }

    public String getName() {
        return NAME;
    }

    public byte[] getValue() throws AttributeValueException {
        throw new RuntimeException("No binary representation for this tag.");
    }

    /**
     * @return the state machine
     */
    public UGStateMachine getStateMachine() {
        return sm;
    }
}