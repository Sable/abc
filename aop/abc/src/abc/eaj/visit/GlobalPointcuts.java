package abc.eaj.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.visit.*;

import abc.eaj.ast.*;
import abc.eaj.extension.*;
import abc.eaj.types.*;

/**
 * @author Julian Tibble
 */
public class GlobalPointcuts extends ContextVisitor
{
    public final static int COLLECT = 1;
    public final static int CONJOIN = 2;

    // This visitor must maintain state in between jobs.
    // The mapping from aspect patterns to global
    // pointcuts is therefore kept in a static variable
    //
    // The visitor is also run in two stages COLLECT,
    // and CONJOIN. Since these are separated by a
    // global barrier pass, we can use a static counter
    // to determine when the mapping should be
    // re-initialised.
    static HashMap /*ClassnamePatternExpr,Pointcut*/ globalpcs = new HashMap();
    static int unmatchedCollectPasses = 0;


    EAJNodeFactory nodeFactory;
    int pass;

    public GlobalPointcuts(int pass, Job job, EAJTypeSystem ts, EAJNodeFactory nf)
    {
        super(job, ts, nf);
        this.nodeFactory = nf;
        this.pass = pass;
    }

    /**
     * callback to allow a GlobalPoincutDecl to register itself
     */
    public void addGlobalPointcut(ClassnamePatternExpr pattern,
                                  Pointcut pointcut)
    {
        if (globalpcs.containsKey(pattern)) {
            Pointcut current = (Pointcut) globalpcs.get(pattern);
            globalpcs.put(pattern, conjoinPointcuts(pointcut, current));
        } else {
            globalpcs.put(pattern, pointcut);
        }
    }

    public Pointcut conjoinPointcuts(Pointcut a, Pointcut b)
    {
        return nodeFactory.PCBinary(b.position(), a, PCBinary.COND_AND, b);
    }


    // Methods implementing ContextVisitor interface
 
    // maintain the static state
    public void finish()
    {
        switch (pass) {
            case COLLECT:
                unmatchedCollectPasses++;
                break;
            case CONJOIN:
                unmatchedCollectPasses--;
        }

        if (unmatchedCollectPasses == 0)
            globalpcs = new HashMap();
    }

    public NodeVisitor enter(Node parent, Node n)
    {
        if (pass == COLLECT && n instanceof GlobalPointcutDecl) {
            ((GlobalPointcutDecl) n).registerGlobalPointcut(this, context(),
                                                            nodeFactory);
        }
        return super.enter(parent, n);
    }

    public Node leave(Node parent, Node old, Node n, NodeVisitor v)
    {
        n = super.leave(parent, old, n, v);

        if (pass == CONJOIN && n instanceof EAJAdviceDecl) {
            EAJAdviceDecl adviceDecl = (EAJAdviceDecl) n;
            PCNode aspect = PCStructure.v().getClass(context().currentClass());

            return applyMatchingGlobals(aspect, adviceDecl);
        }

        return n;
    }

    protected EAJAdviceDecl applyMatchingGlobals(PCNode aspect,
                                                 EAJAdviceDecl ad)
    {
        Iterator i = globalpcs.keySet().iterator();

        while (i.hasNext()) {
            ClassnamePatternExpr pattern = (ClassnamePatternExpr) i.next();

            if (pattern.matches(PatternMatcher.v(), aspect)) {
                Pointcut global = (Pointcut) globalpcs.get(pattern);
                ad = ad.conjoinPointcutWith(this, global);
            }
        }

        return ad;
    }
}
