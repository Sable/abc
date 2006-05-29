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

import java.util.Iterator;
import java.util.List;

import abc.main.options.OptionsParser;

import soot.Unit;
import soot.jimple.Stmt;
import soot.util.Chain;

/** 
 * Utility class for tagging generated instructions.
 * 
 * @author Chris Goard
 */
public class Tagger {
    
    private static void debug(String message) {
        if (abc.main.Debug.v().instructionTagger) {
            System.err.println("TAG*** " + message); 
        }
    }

    /**
     * Tags a statement with the given <code>InstructionTag</code>.
     * If the statement already has a tag with the same name, the
     * new tag overwrites the old tag only if <code>overwrite</code>
     * is true.
     * 
     * @param s statement to be tagged.
     * @param tag instruction tag.
     * @param overwrite Specifies whether the given tag should overwrite an
     *                  extant tag with the same name.
     */
    public static void tagStmt(Stmt s, InstructionTag tag, boolean overwrite) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        if(tag == null) {
            return;
        }
        if(s.hasTag(tag.getName()) && overwrite) {
            InstructionTag oldTag = (InstructionTag) s.getTag(tag.getName());
            if(oldTag.value != tag.value) {
                debug("Overwriting " + oldTag.getName() + ": " + tag.value
                      + " with " + tag.value + " on " + s);
                /*try {
                    throw new Exception();
                } catch (Exception e) {
                    StackTraceElement[] se = e.getStackTrace();
                    for(int i = 0; i < se.length; i++) {
                        debug(se[i]);
                    }
                }*/
            }
            s.removeTag(tag.getName());
        } else if(s.hasTag(tag.getName()) && !overwrite) {
            InstructionTag oldTag = (InstructionTag) s.getTag(tag.getName());
            debug("Not overwriting " + oldTag.getName() + ": " + tag.value
                + " with " + tag.value + " on " + s);
            return;
        } else {
            debug("Adding new tag " + tag.getName() + "("+ tag.value + ") to " + s);
        }
        s.addTag(tag);
    }

    /**
     * Tags a statement with the given <code>InstructionTag</code>,
     * overwriting any extant tag with the same name. 
     * 
     * @param s statement to be tagged.
     * @param tag instruction tag.
     */
    public static void tagStmt(Stmt s, InstructionTag tag) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        tagStmt(s, tag, true);
    }
    
    /**
     * Tags statement with the (non-null) tags within a
     * <code>WeavingContext</code>. If the statement already has tags with the 
     * same names, the new tags overwrite the old tags only if 
     * <code>overwrite</code> is true.
     * 
     * @param s statement to be tagged.
     * @param tc contains kind, shadow, and source tags.
     * @param overwrite Specifies whether the given tags should overwrite
     *                  extant tags with the same names.
     */
    public static void tagStmt(Stmt s, TagContainer tc, boolean overwrite) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        /*if((wc.getShadowTag() != null ||  wc.getSourceTag() != null)
             && wc.getKindTag() == null)
        {
            System.err.println("TAG*** Null kindTag: " + s);
            try {
                throw new Exception();
            } catch (Exception e) {
                StackTraceElement[] se = e.getStackTrace();
                for(int i = 0; i < se.length; i++) {
                    System.err.println("TAG*** " + se[i]);
                }
            }
        }*/
        tagStmt(s, tc.getKindTag(), overwrite);
        tagStmt(s, tc.getShadowTag(), overwrite);
        tagStmt(s, tc.getSourceTag(), overwrite);
    }

    /**
     * Tags statement with the (non-null) tags within a
     * <code>WeavingContext</code>, overwriting any extant tags with the same 
     * names.
     * 
     * @param s statement to be tagged.
     * @param wc contains kind, shadow, and source tags.
     */
    public static void tagStmt(Stmt s, TagContainer tc) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        tagStmt(s, tc, true);
    }

    /**
     * Tags all statements in a <code>Chain</code> with the given
     * <code>InstructionTag</code>, overwriting any extant tags with
     * the same names.
     * 
     * @param c contains statements to be tagged.
     * @param t instruction tag.
     */
    public static void tagChain(Chain c, InstructionTag t) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        for(Iterator i = c.iterator(); i.hasNext();) {
            Stmt s = (Stmt) i.next();
            tagStmt(s, t);
        }
    }

    /**
     * Tags all statements in a <code>Chain</code> with the tags within
     * a <code>WeavingContext</code>, overwriting any extant tags with
     * the same names.
     * 
     * @param c contains statements to be tagged.
     * @param wc contains kind, shadow, and source tags. 
     */
    public static void tagChain(Chain c, TagContainer tc) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        for(Iterator i = c.iterator(); i.hasNext();) {
            Stmt s = (Stmt) i.next();
            tagStmt(s, tc.getKindTag());
            tagStmt(s, tc.getShadowTag());
            tagStmt(s, tc.getSourceTag());
        }
    }
    
    /**
     * Tags all statements in a <code>List</code> with the given
     * <code>InstructionTag</code>.
     * 
     * @param l list of statements.
     * @param t instruction tag.
     * @param overwrite Specifies whether the given tag should overwrite an
     *                  extant tag with the same name.
     */
    public static void tagList(List l, InstructionTag t, boolean overwrite) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        for(Iterator i = l.iterator(); i.hasNext();) {
            Stmt s = (Stmt) i.next();
            tagStmt(s, t, overwrite);
        }
    }
    
    /**
     * Returns the "propagated tag" for an inlined method.
     * 
     * @param t call site tag.
     * @return tag to be propagated to body.
     */
    public static InstructionKindTag propagateKindTag(InstructionKindTag t) {
        if(t == InstructionKindTag.ADVICE_EXECUTE) {
            return InstructionKindTag.INLINED_ADVICE;
        } else if(t == InstructionKindTag.AROUND_PROCEED) {
            return InstructionKindTag.INLINED_PROCEED;
        } else {
            return t;
        }
    }

    /**
     * Adds <code>InlineTag</code> to <code>Unit</code>. When a method body
     * is inlined, the shadow and source tags of the call site are added to 
     * the <code>InlineTag</code> of all inlined statements.
     * 
     * @param u unit to be tagged.
     * @param shadow shadow tag of inlined call site.
     * @param source source tag of inlined call site.
     */
    public static void addInlineTag(Unit u, 
                                    InstructionShadowTag shadow, 
                                    InstructionSourceTag source)
    {
        if(!OptionsParser.v().tag_instructions()) { return; }
        InstructionInlineTags list =
            (InstructionInlineTags) u.getTag(InstructionInlineTags.NAME);
        if(list == null) {
            list = new InstructionInlineTags();
            u.addTag(list);
        }
        list.prepend(new InstructionInlineTags.InlineTag(shadow, source)); 
    }
    
    /**
     * Adds <code>InstructionInlineTags</code> to <code>Unit</code>.
     * If an inlined call site is itself part of an inlined body, when it is 
     * inlined, its list of <code>InlineTag</code>s is added to statements in 
     * the inlined body.
     * 
     * @param u unit to be tagged.
     * @param tags inlined tag list of call site.
     */
    public static void addInlineTags(Unit u, InstructionInlineTags tags) {
        if(!OptionsParser.v().tag_instructions()) { return; }
        InstructionInlineTags list = 
            (InstructionInlineTags) u.getTag(InstructionInlineTags.NAME);
        if(list == null) {
            list = new InstructionInlineTags(tags);
            u.addTag(list);
        } else {
            list.prepend(tags.value);
        }
    }

    /**
     * Tags a range of instructions corresponding to an inlined proceed 
     * method.
     * 
     * @param proceedStmts inlined proceed method.
     */
    public static void tagProceedRange(List proceedStmts) {
        //Unit first = (Unit)newStmts.get(0);
        //Unit last = (Unit)newStmts.get(newStmts.size() - 1);
        for (Iterator i = proceedStmts.iterator(); i.hasNext();) {
            Unit u = (Unit) i.next();
            u.addTag(new InstructionProceedTag(0));
        }
        /*
        System.out.println("first: " + first.toString());
        System.out.println("last: " + last.toString());
        first.addTag(new InstructionProceedTag(0));
        last.addTag(new InstructionProceedTag(1));
        */
    }
}
