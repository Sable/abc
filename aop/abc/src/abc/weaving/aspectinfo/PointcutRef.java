package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.*;
import abc.weaving.residues.Residue;

/** Handler for a pointcut reference. */
public class PointcutRef extends Pointcut {
    private Object decl_key;
    private Map/*<Object,PointcutDecl>*/ decl_map;
    private PointcutDecl decl;
    private List/*<ArgPattern>*/ args;

    /** Create an <code>args</code> pointcut.
     *  @param decl_key an object that can later be resolved into the pointcut declaration.
     *  @param decl_map a map from {@link java.lang.Object} to {@link abc.weaving.aspectinfo.PointcutDecl}.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public PointcutRef(Object decl_key, Map decl_map, List args,Position pos) {
	super(pos);
	this.decl_key = decl_key;
	this.decl_map = decl_map;
	this.args = args;
    }

    /** Create an <code>args</code> pointcut.
     *  @param decl the pointcut declaration.
     *  @param args a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public PointcutRef(PointcutDecl decl, List args,Position pos) {
	super(pos);
	this.decl = decl;
	this.args = args;
    }

    public PointcutDecl getDecl() {
	if (decl == null) {
	    decl = (PointcutDecl) decl_map.get(decl_key);
	    decl_key = null;
	    decl_map = null;
	}
	return decl;
    }

    /** Get the list of argument patterns.
     *  @return a list of {@link abc.weaving.aspectinfo.ArgPattern} objects
     */
    public List getArgs() {
	return args;
    }

    public String toString() {
	return getDecl().getName()+"(...)";
    }

    public Residue matchesAt(WeavingEnv env,
			     SootClass cls,
			     SootMethod method,
			     ShadowMatch sm) {
	throw new RuntimeException("named pointcuts not handled yet");
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv) {
	Iterator actualsIt=args.iterator();
	Iterator formalsIt=getDecl().getFormals().iterator();

	List/*<Formal>*/ newLocals=new LinkedList();
	List/*<CastPointcutVar>*/ newCasts=new LinkedList();

	Hashtable/*<String,Var>*/ declRenameEnv=new Hashtable();
	Hashtable/*<String,Abctype>*/ declTypeEnv=new Hashtable();

	while(actualsIt.hasNext() || formalsIt.hasNext()) {
	    ArgPattern actual = (ArgPattern) actualsIt.next();
	    Formal formal = (Formal) formalsIt.next();

	    Var param=actual.substituteForPointcutFormal
		(renameEnv,typeEnv,formal,newLocals,newCasts,getPosition());

	    declTypeEnv.put(formal.getName(),formal.getType());
	    declRenameEnv.put(formal.getName(),param);
	}

	Pointcut pc=getDecl().getPointcut()
	    .inline(declRenameEnv,declTypeEnv);

	Iterator castsIt=newCasts.iterator();
	while(castsIt.hasNext()) {
	    CastPointcutVar cpv=(CastPointcutVar) castsIt.next();
	    pc=new AndPointcut(pc,cpv,getPosition());
	}

	if(!newLocals.isEmpty()) {
	    pc=new LocalPointcutVars(pc,newLocals,getPosition());
	}

	return pc;

    }

}
