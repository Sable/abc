/* Abc - The AspectBench Compiler
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
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

import com.sun.rsasign.i;

import polyglot.util.Position;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>args</code> condition pointcut. */
public class Args extends DynamicValuePointcut {
    private List/*<ArgPattern>*/ args;

    /** Create an <code>args</code> pointcut.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public Args(List args,Position pos) {
	super(pos);
	this.args = args;
    }

    /** Get the list of argument patterns.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public List getArgs() {
	return args;
    }

    public String toString() {
	StringBuffer out=new StringBuffer("args(");
	Iterator it=args.iterator();
	while(it.hasNext()) {
	    out.append(it.next());
	    if(it.hasNext()) out.append(",");
	}
	out.append(")");
	    
	return out.toString();
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {

	Iterator it=args.iterator();
	List newargs=new LinkedList();
	while(it.hasNext()) {
	    ArgPattern arg=(ArgPattern) it.next();
	    // Ought to delegate this really, but this is easier
	    if(arg instanceof ArgVar) {
		ArgVar argvar=(ArgVar) arg;
		newargs.add(new ArgVar(argvar.getVar().rename(renameEnv),
				       argvar.getPosition()));
	    } else newargs.add(arg);
	}
	return new Args(newargs,getPosition());
	
    }

    public Residue matchesAt(WeavingEnv we,SootClass cls,SootMethod method,ShadowMatch sm)
	throws SemanticException
    {
    	if(abc.main.Debug.v().showArgsMatching)
	    System.out.println("args="+args+"sm="+sm+" of type "+sm.getClass());
	Residue ret=AlwaysMatch.v;
	ListIterator formalsIt=args.listIterator();
	List actuals=sm.getArgsContextValues();
	if(abc.main.Debug.v().showArgsMatching)
	    System.out.println("actuals are "+actuals);
	ListIterator actualsIt=actuals.listIterator();
	int fillerpos=-1;
	while(formalsIt.hasNext() && actualsIt.hasNext()) {
	    ArgPattern formal=(ArgPattern) formalsIt.next();
	    if(abc.main.Debug.v().showArgsMatching)
		System.out.println("formal is "+formal);
	    if(formal instanceof ArgFill) {
		if(abc.main.Debug.v().showArgsMatching) 
		    System.out.println("filler at position "+(formalsIt.nextIndex()-1)
				       +" ("+formal.getPosition()+")");
		fillerpos=formalsIt.nextIndex();  // The position _after_ the filler
		while(formalsIt.hasNext()) formalsIt.next();
		while(actualsIt.hasNext()) actualsIt.next();
		break;
	    }  
	    ContextValue actual=(ContextValue) actualsIt.next();

	    if(abc.main.Debug.v().showArgsMatching) 
		System.out.println("matching "+formal+" with "+actual);
	    ret=AndResidue.construct(ret,formal.matchesAt(we,actual));

	}
	if(fillerpos==-1) {
	    // we stopped because one list or the other ended, 
	    // and there were no ArgFills
	    if(actualsIt.hasNext() || 
	       (formalsIt.hasNext() &&
		// If there is one more formal left, it's ok as long as it is
		// an ArgFill. Note that we rely on the short-circuiting and
		// the left-to-right evaluation order 
		// and that Iterator.next() affects the result of Iterator.hasNext()
		!(formalsIt.next() instanceof ArgFill && !formalsIt.hasNext())))
		return null; // the list lengths don't match up
	    else return ret;
	}
	if(abc.main.Debug.v().showArgsMatching)
	    System.out.println("actuals length is "+actuals.size()+" formals length is "+args.size());
	// There was an ArgFill
	if(actuals.size()<args.size()-1) // There aren't enough actuals for the formals minus the ArgFill
	    return null;
	    
	while(formalsIt.hasPrevious() && actualsIt.hasPrevious()) {
	    ArgPattern formal=(ArgPattern) formalsIt.previous();
	    if(formal instanceof ArgFill) {
		/* this is now checked in the frontend:
		    if(formalsIt.nextIndex()+1!=fillerpos)
		    throw new SemanticException
			("Two fillers in args pattern",formal.getPosition());  */

		return ret; // all done!
	    }
	    ContextValue actual=(ContextValue) actualsIt.previous();
	    
	    if(abc.main.Debug.v().showArgsMatching) 
		System.out.println("matching "+formal+" with "+actual);
	    ret=AndResidue.construct(ret,formal.matchesAt(we,actual));
	}
	if(formalsIt.hasPrevious() && formalsIt.previous() instanceof ArgFill) return ret;
	// This shouldn't happen because we should find the filler before either the formals or the
	// actuals run out.
	throw new InternalCompilerError
	    ("Internal error: reached the end of a args pattern list unexpectedly - "
	     +"pattern was "+args+", method was "+method);
    }

    public void registerSetupAdvice
	(Aspect aspect,Hashtable/*<String,AbcType>*/ typeMap) {}

    public void getFreeVars(Set/*<Var>*/ result) {
	Iterator it=args.iterator();
	while(it.hasNext()) 
	    ((ArgPattern) (it.next())).getFreeVars(result);
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof Args) {
			List/*<ArgPattern>*/ otherargs = ((Args)otherpc).getArgs();
			// Walk through the lists manually b/c want to compute renamings
			
			Iterator it1 = args.iterator();
			Iterator it2 = otherargs.iterator();
			while (it1.hasNext() && it2.hasNext()) {
				ArgPattern pat1 = (ArgPattern)it1.next();
				ArgPattern pat2 = (ArgPattern)it2.next();
				if (!pat1.equivalent(pat2, renaming)) return false;
			}
			if (it1.hasNext() || it2.hasNext()) return false;
			return true;
		} else return false;
	}

}
