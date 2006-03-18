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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import soot.Body;
import soot.PatchingChain;
import soot.Unit;
import soot.baf.BafBody;
import soot.tagkit.CodeAttribute;
import soot.tagkit.Tag;
import soot.tagkit.TagAggregator;

/**
 * @author Chris Goard
 */
public abstract class InstructionTagAggregator extends TagAggregator {

    /* (non-Javadoc)
     * @see soot.tagkit.TagAggregator#considerTag(soot.tagkit.Tag, soot.Unit)
     */
    public void considerTag(Tag t, Unit u) {
        tags.add(t);
        units.add(u);
    }

    protected void internalTransform(Body b, String phaseName, Map options)
    {
        BafBody body = (BafBody) b;
       
        /* clear the aggregator first. */
        tags.clear();
        units.clear();

        /* aggregate all tags */
        for( Iterator unitIt = body.getUnits().iterator(); unitIt.hasNext(); ) {
            final Unit unit = (Unit) unitIt.next();
            for( Iterator tagIt = unit.getTags().iterator(); tagIt.hasNext(); ) {
                final Tag tag = (Tag) tagIt.next();
                if( wantTag( tag ) ) considerTag( tag, unit );
            }
        }

        // tag only first unit in range
        // XXX: use FirstTagAggregator instead?
        
        // Add NO_TAG to untagged successor instructions
        PatchingChain bodyUnits = b.getUnits();
        LinkedList newUnits = new LinkedList();
        LinkedList newTags = new LinkedList();
        for(Iterator unitIt = units.iterator(), tagIt = tags.iterator(); unitIt.hasNext();) {
            Unit u = (Unit) unitIt.next();
            Tag t = (Tag) tagIt.next();
            newUnits.add(u);
            newTags.add(t);
            
            Unit succ = (Unit) bodyUnits.getSuccOf(u);
            if(succ == null) {
                // no successor, do nothing
            } else if(units.contains(succ)) {
                // successor is already tagged
            } else {
                newUnits.add(succ);
                newTags.add(InstructionKindTag.NO_TAG);
            }
        }
        units = newUnits;
        tags = newTags;

        // Pack lists
        {
            //Unit u = null, prevU = null;
            Tag t = null, prevT = null;
            for(Iterator unitIt = units.iterator(), tagIt = tags.iterator(); unitIt.hasNext();) {
                //prevU = u;
                prevT = t;
                //u = (Unit) unitIt.next();
                unitIt.next();
                t = (Tag) tagIt.next();
                if(prevT != null && Arrays.equals(t.getValue(), prevT.getValue())) {
                    unitIt.remove();
                    tagIt.remove();
                }
            }
        }

        /*
        if(!units.isEmpty()) {
            System.out.println(b.getMethod().getName() + " Aggregated tags: ");
            System.out.println(aggregatedName());
            for(Iterator ui = units.iterator(), ti = tags.iterator(); ui.hasNext();) {
                Unit u = (Unit)ui.next();
                InstructionTag t = (InstructionTag)ti.next();
                System.out.println("(" + u.hashCode() + ", " + t.value() + ") ");
            }
            System.out.println();
        }
        */
        
        if(units.size() > 0) {
            b.addTag( new CodeAttribute(aggregatedName(), 
                  new LinkedList(units),
                  new LinkedList(tags)) );
        }
        fini();
    }
}
