/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Chris Goard
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

package abc.weaving.tagkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * List of shadow/source IDs of inlined call sites.
 * 
 * @author Chris Goard
 */
public class InstructionInlineTags implements Tag {

    public static final String NAME = "ca.mcgill.sable.InstructionInlineShadowSource";
    
    List value = new ArrayList();
    
    public InstructionInlineTags() {
        super();
    }
    
    public InstructionInlineTags(InstructionInlineTags tag) {
        super();
        value.addAll(tag.value);
    }

    public String getName() {
        return NAME;
    }

    public byte[] getValue() throws AttributeValueException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(value.size());
            for (Iterator i = value.iterator(); i.hasNext();) {
                InlineTag tag = (InlineTag) i.next();
                dos.writeInt(tag.shadow.value);
                dos.writeInt(tag.source.value);
            }
            dos.flush();
        } catch(IOException e) {
            // XXX: Handle Exception!!
        }
        return baos.toByteArray();
    }

    public void append(InlineTag t) {
        value.add(t);
    }
    
    public void prepend(InlineTag t) {
        value.add(0, t);
    }
    
    public void prepend(List l) {
        value.addAll(0, l);
    }
    
    public static class InlineTag {
        
        public InlineTag(InstructionShadowTag shadow, InstructionSourceTag source) {
            this.shadow = shadow;
            this.source = source;
        }
        
        public InstructionShadowTag shadow;
        public InstructionSourceTag source;
    }
    
}
