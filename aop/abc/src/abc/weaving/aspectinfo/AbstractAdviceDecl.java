/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.aspectinfo;

import java.util.*;
import soot.util.Chain;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** The base class for any kind of 'advice' declaration
 *  This can include both real advice declared directly in the aspect
 *  and other kinds of advice needed by abc for implementation reasons.
 *  @author Ganesh Sittampalam
 */
public abstract class AbstractAdviceDecl extends Syntax implements Cloneable {
    private static int nextId = 1;
    public int sourceId;
    
    protected AdviceSpec spec;
    protected Pointcut pc=null;

    private Pointcut origpc;
    private List formals;

    // What aspect is this advice being applied from?
    // This is important for resolving abstract pointcuts
    private Aspect aspct;

    // What aspect was this advice originally defined in?
    // This is important for dealing with advice precedence
    private Aspect defined_aspct;

    protected AbstractAdviceDecl(Aspect aspct,
                                 AdviceSpec spec,
                                 Pointcut pc,
                                 List/*<Formal>*/ formals,
                                 Position pos) {
        this(aspct,spec,pc,formals,pos,false);
        if(abc.main.Debug.v.debugPointcutNormalization)
            System.out.println("made unnormalized decl");
    }

    protected AbstractAdviceDecl(Aspect aspct,AdviceSpec spec,Pointcut pc,
                                 List/*<Formal>*/ formals,Position pos,
                                 boolean normalized) {
        super(pos);
        sourceId = nextId++;
        this.aspct=aspct;
        this.defined_aspct=aspct;
        this.spec=spec;

        if(normalized) this.pc=pc; else this.origpc=pc;
        this.formals=formals;
    }

    public AdviceSpec getAdviceSpec() {
        return spec;
    }

    /** Every advice declaration is associated with a particular aspect.
     *  This method returns the aspect.
     *  @author Ganesh Sittampalam
     */
    public Aspect getAspect() {
        return aspct;
    }

    /** Get the aspect an advice declaration was originally defined in.
     *  @author Ganesh Sittampalam
     */
    public Aspect getDefiningAspect() {
        return defined_aspct;
    }

    protected Object clone() {
        try {
            return super.clone();
        } catch(CloneNotSupportedException e) {
            throw new InternalCompilerError("AbstractAdviceDecl should be cloneable",e);
        }
    }

    /** Make an exact copy of this advice declaration, but change the aspect to the given one.
     *  This is needed to implement aspect inheritance, because that defines that advice defined
     *  in a base aspect is treated as occurring once in each derived aspect.
     *  @author Ganesh Sittampalam
     */
    public AbstractAdviceDecl makeCopyInAspect(Aspect newaspct) {
        AbstractAdviceDecl n=(AbstractAdviceDecl) this.clone();
        n.aspct=newaspct;
        return n;
    }

    /** Pointcuts come in normalized and unnormalized versions.
     *  (See {@link Pointcut.normalize}). An advice declaration can be constructed with
     *  the unnormalized version, in which case it is necessary to call this method
     *  before trying to use it for anything pointcut related. It is an error to call
     *  this method if the pointcut has already been normalized.
     *  @author Ganesh Sittampalam
     */
    public void preprocess() {
        if(pc!=null) {
	    if(abc.main.Debug.v().debugPointcutNormalization) 
		System.out.println("Already normalized");
	    return;
	    // throw new InternalCompilerError
	    // ("Trying to call preprocess on an already normalized advice decl "+this);
	}
        if(abc.main.Debug.v.debugPointcutNormalization) System.out.println("normalizing");
        //        System.err.println(origpc);
        pc=Pointcut.normalize(origpc,formals,getAspect());
        if(abc.main.Debug.v.debugPointcutNormalization) System.out.println("done");
    }

    public Pointcut getPointcut() {
        if(pc==null) throw new InternalCompilerError
                         ("Must call preprocess on advice decls before using them");
        return pc;
    }

    public List/*<Formal>*/ getFormals() {
        return formals;
    }

    public abstract void debugInfo(String prefix,StringBuffer sb);

    public abstract WeavingEnv getWeavingEnv();

    public abstract WeavingContext makeWeavingContext();

    public void resetForReweaving() {
    	applcount = 0;
    };

    // All this JoinPoint stuff ought to move to a residue or something.
    public boolean hasJoinPoint() {
        return false;
    }

    public boolean hasJoinPointStaticPart() {
        return false;
    }

    public boolean hasEnclosingJoinPoint() {
        return false;
    }

    public Residue preResidue(ShadowMatch sm) {
        return AlwaysMatch.v();
    }

    public Residue postResidue(ShadowMatch sm) {
        return AlwaysMatch.v();
    }

    /** Produce a chain containing the statements to execute this piece of advice.
     *  If execution reaches the beginning of the chain, then the advice definitely
     *  applies.
     *  @param adviceappl The advice application structure.
     *  @param localgen   A local variable generator for the method body being woven into
     *  @param wc         The weaving context
     *  @author Ganesh Sittampalam
     */
     // document why we need adviceappl. Should we switch to the insertAfter model
     // everything else uses?
    public abstract Chain makeAdviceExecutionStmts
        (AdviceApplication adviceappl,LocalGeneratorEx localgen,WeavingContext wc);

    private int applcount=0; // the number of times this AdviceDecl matches
                             //   (i.e. the number of static join points)
    /** Increment the number of times this advice is applied.
     */
    public void incrApplCount() {
        applcount++;
    }

    /** Returns a warning if this AdviceDecl does not match any static
     *  joinpoints, otherwise returns null.
     */
    public String getApplWarning() {
        return applcount == 0 ? "Advice declaration doesn't apply anywhere"
                              : null;
    }

    /**
     * Returns how often this advice was applied during weaving.
     * @return the number of times this advice was applied during
     * the last weaving process
     */
    public int getApplCount() {
    	return applcount;
    }
    

    /** Return a string describing the current piece of advice, for use in
     *  error messages
     */
    public String errorInfo() {
        return "aspect "+getAspect().getName().replace('$','.')
            +" ("+getPosition().file()
            +", line "+getPosition().line()+")";
    }

    /** Report any errors or warnings for this advice application. */
    public void reportMessages(AdviceApplication aa) {
        // Default is to not report anything
    }
}
