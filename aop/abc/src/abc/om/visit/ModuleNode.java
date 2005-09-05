/*
 * Created on May 15, 2005
 *
 */
package abc.om.visit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.types.reflect.ClassPathLoader;
import polyglot.util.Position;
import soot.SootClass;

import abc.aspectj.ast.CPEName_c;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.NamePattern;
import abc.aspectj.ast.PointcutDecl;
import abc.aspectj.ast.SimpleNamePattern_c;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.aspectj.visit.PatternMatcher;
import abc.om.AbcExtension;
import abc.om.ast.SigMember;
import abc.om.ast.SigMemberMethodDecl;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.ClassnamePattern;
import abc.weaving.aspectinfo.NotPointcut;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Within;

/**
 * Internal representation of module hierarchy.
 * 
 * @author Neil Ongkingco
 *  
 */
public abstract class ModuleNode {
    /* enum ModuleType */
    public static final int TYPE_NULL = 0;

    public static final int TYPE_MODULE = 1;

    public static final int TYPE_ASPECT = 2;

    public static final int TYPE_CLASS = 3;

    protected String name;

    protected ModuleNode parent;

    public String name() {
        return name;
    }

    public abstract boolean isAspect();

    public abstract boolean isModule();

    public abstract boolean isClass();

    public abstract int type();

    public void setParent(ModuleNode n) {
        this.parent = n;
    }

    public ModuleNode getParent() {
        return this.parent;
    }

}