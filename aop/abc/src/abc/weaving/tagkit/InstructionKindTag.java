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

/**
 * Represents the nature of overhead instructions generated during weaving.
 * 
 * @author Chris Goard
 */
public class InstructionKindTag extends InstructionTag {
    
    public static final String NAME = "ca.mcgill.sable.InstructionKind"; 

    public static final InstructionKindTag NO_TAG = new InstructionKindTag(-1);

    /* Default value; normally gets resolved to BASE_CODE: non-overhead instructions
     * corresponding to code in the base program.
     */
    public static final InstructionKindTag DEFAULT = new InstructionKindTag(0);
    
    /* Instructions responsible for executing advice methods. */
    public static final InstructionKindTag ADVICE_EXECUTE = new InstructionKindTag(1);
    
    /* Instructions responsible for setup before executing advice. Acquiring the aspect
     * instance, for example.
     */
    public static final InstructionKindTag ADVICE_ARG_SETUP = new InstructionKindTag(2);

    /* Testing advice guards (dynamic residues for advice execution.) */
    public static final InstructionKindTag ADVICE_TEST = new InstructionKindTag(3);

    /* After / after throwing exception handling instructions. */
    public static final InstructionKindTag AFTER_THROWING_HANDLER = new InstructionKindTag(4);

    /* Exception softening instructions. */
    public static final InstructionKindTag EXCEPTION_SOFTENER = new InstructionKindTag(5);

    /* Instructions that expose return value in after / after returning advice. */
    public static final InstructionKindTag AFTER_RETURNING_EXPOSURE = new InstructionKindTag(6);

    /* Per-object aspect instance binding. */
    public static final InstructionKindTag PEROBJECT_ENTRY = new InstructionKindTag(7);

    /* Cflow management instructions. Push / pop cflow stacks or counters. */
    public static final InstructionKindTag CFLOW_EXIT = new InstructionKindTag(8);
    public static final InstructionKindTag CFLOW_ENTRY = new InstructionKindTag(9);

    /* Accessor methods for private members and privileged aspects. */
    public static final InstructionKindTag PRIV_METHOD = new InstructionKindTag(10);
    public static final InstructionKindTag PRIV_FIELD_GET = new InstructionKindTag(11);
    public static final InstructionKindTag PRIV_FIELD_SET = new InstructionKindTag(12);

    /* Aspect class initialization. */
    public static final InstructionKindTag ASPECT_CLINIT = new InstructionKindTag(13);
    
    /* Intertype declaration dispatch methods. */
    public static final InstructionKindTag INTERMETHOD = new InstructionKindTag(14);
    public static final InstructionKindTag INTERFIELD_GET = new InstructionKindTag(15);       
    public static final InstructionKindTag INTERFIELD_SET = new InstructionKindTag(16);
    public static final InstructionKindTag INTERFIELD_INIT = new InstructionKindTag(17);
    public static final InstructionKindTag INTERCONSTRUCTOR_PRE = new InstructionKindTag(18);
    public static final InstructionKindTag INTERCONSTRUCTOR_POST = new InstructionKindTag(19);
    public static final InstructionKindTag INTERCONSTRUCTOR_CONVERSION = new InstructionKindTag(20);

    /* Per-object instance binding. */
    public static final InstructionKindTag PEROBJECT_GET = new InstructionKindTag(21);
    public static final InstructionKindTag PEROBJECT_SET = new InstructionKindTag(22);

    /* Around advice. */
    public static final InstructionKindTag AROUND_CONVERSION = new InstructionKindTag(23);
    public static final InstructionKindTag AROUND_CALLBACK = new InstructionKindTag(24);
    public static final InstructionKindTag AROUND_PROCEED = new InstructionKindTag(25);
    public static final InstructionKindTag CLOSURE_INIT = new InstructionKindTag(26);

    /* Not used in abc; used in ajc. */
    public static final InstructionKindTag INLINE_ACCESS_METHOD = new InstructionKindTag(27);

    /* Non-overhead instruction corresponding to user code in aspects. */
    public static final InstructionKindTag ASPECT_CODE = new InstructionKindTag(28);

    /* Unused in abc. */
    public static final InstructionKindTag CFLOW_GETTHREADSTACK = new InstructionKindTag(29);
    
    /* Per-cflow aspect instance management instructions. */
    public static final InstructionKindTag PERCFLOW_ENTRY = new InstructionKindTag(30);
    public static final InstructionKindTag PERCFLOW_EXIT = new InstructionKindTag(31);

    /* BCEL artifacts in ajc. */
    public static final InstructionKindTag BCEL = new InstructionKindTag(32);

    /* Dynamic residue on cflow management instructions. */
    public static final InstructionKindTag CFLOW_TEST = new InstructionKindTag(33);

    /* ThisJoinPoint management instructions. */
    public static final InstructionKindTag THISJOINPOINT = new InstructionKindTag(34);
    
    /* Dynamic residue on per-object aspect instance binding. */
    public static final InstructionKindTag PEROBJECT_ENTRY_TEST = new InstructionKindTag(35);
    
    /* Inlined advice body. Non-overhead, corresponds to ASPECT_CODE. */
    public static final InstructionKindTag INLINED_ADVICE = new InstructionKindTag(36);
    
    /* Cflow management instructions. */
    public static final InstructionKindTag GET_CFLOW_LOCAL = new InstructionKindTag(37);
    public static final InstructionKindTag GET_CFLOW_THREAD_LOCAL = new InstructionKindTag(38);

    public static final InstructionKindTag DECLARE_MESSAGE = new InstructionKindTag(39);
    
    /* Inlined proceed body. Non-overhead, corresponds to BASE_CODE or ASPECT_CODE. */
    public static final InstructionKindTag INLINED_PROCEED = new InstructionKindTag(40);
    
    public static final InstructionKindTag TEST1 = new InstructionKindTag(99);
    public static final InstructionKindTag TEST2 = new InstructionKindTag(98);
    public static final InstructionKindTag TEST3 = new InstructionKindTag(97);
    public static final InstructionKindTag TEST4 = new InstructionKindTag(96);
    public static final InstructionKindTag TEST5 = new InstructionKindTag(95);
    public static final InstructionKindTag TEST6 = new InstructionKindTag(94);
    public static final InstructionKindTag TEST7 = new InstructionKindTag(93);
    
    private InstructionKindTag(int i) {
        super(NAME, i);
    }

}
