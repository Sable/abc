package abc.eaj.visit;

import java.util.*;

import polyglot.ast.*;
import polyglot.frontend.*;
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.visit.PatternMatcher;

import abc.eaj.ast.*;
import abc.eaj.types.*;

public class GlobalPointcuts extends ContextVisitor
{
    public final static int COLLECT = 1;
    public final static int CONJOIN = 2;

    // This visitor must maintain state in between jobs.
    // The mapping from aspect names to applicable global
    // pointcuts is therefore kept in a static variable
    //
    // The visitor is also run in two stages COLLECT,
    // and CONJOIN. Since these are separated by a
    // global barrier pass, we can use a static counter
    // to determine when the mapping should be
    // re-initialised.
    static HashMap /*String,Pointcut*/ matchingpcs = new HashMap();
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
    public void addGlobalPointcut(NamePattern pattern, Pointcut pointcut)
    {
        Iterator i = PatternMatcher.v().getMatches(pattern).iterator();

        while (i.hasNext()) {
            String name = i.next().toString().intern();

            if (matchingpcs.containsKey(name)) {
                Pointcut current = (Pointcut) matchingpcs.get(name);
                matchingpcs.put(name, conjoinPointcuts(pointcut, current));
            } else {
                matchingpcs.put(name, pointcut);
            }
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
            matchingpcs = new HashMap();
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
            EAJAdviceDecl ad = (EAJAdviceDecl) n;
            String aspect = context().currentClass().fullName().intern();

            if (matchingpcs.containsKey(aspect)) {
                Pointcut global = (Pointcut) matchingpcs.get(aspect);
                return ad.conjoinPointcutWith(this, global);
            }
        }

        return n;
    }
}
