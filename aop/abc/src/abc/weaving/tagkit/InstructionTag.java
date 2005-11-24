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
 *
 * Created on 17-Mar-2005
 */
package abc.weaving.tagkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * @author Chris Goard
 */
public abstract class InstructionTag implements Tag {

    private String name;
    protected int value;
    private List inlined = new ArrayList();
    
    public int value() {
        return value;
    }

    public InstructionTag(String name, int value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void addInlined(InstructionTag t) {
        inlined.add(t);
    }
    
    public byte[] getValue() throws AttributeValueException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(value);
            dos.flush();
        } catch(IOException e) {
            // XXX: Handle Exception!!
        }
        return baos.toByteArray();
    }
}
