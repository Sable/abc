/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Julian Tibble
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

package abc.tm.weaving.aspectinfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import abc.tm.weaving.weaver.TestCodeGen;


public class CollectSetSet
{
    private HashSet collectsets = new HashSet();
    private boolean universal = false;

    public CollectSetSet() { }

    public CollectSetSet(Collection symbol_vars)
    {
        Iterator i = symbol_vars.iterator();

        while (i.hasNext()) {
            HashSet set = new HashSet();
            set.add(i.next());
            collectsets.add(set);
        }
    }

    static public CollectSetSet universalSet()
    {
        CollectSetSet universalset = new CollectSetSet();
        universalset.universal = true;
        return universalset;
    }

    public CollectSetSet cross(CollectSetSet other)
    {
        if (universal)
            return other;
        if (other.universal)
            return this;

        CollectSetSet result = new CollectSetSet();
        Iterator i = collectsets.iterator();

        while (i.hasNext()) {
            HashSet iset = (HashSet) i.next();
            Iterator j = other.collectsets.iterator();

            while (j.hasNext()) {
                HashSet jset = (HashSet) j.next();

                HashSet union = new HashSet();
                union.addAll(iset);
                union.addAll(jset);

                result.collectsets.add(union);
            }
        }
        return result;
    }

    public CollectSetSet union(CollectSetSet other)
    {
        if (universal)
            return this;
        if (other.universal)
            return other;

        CollectSetSet result = new CollectSetSet();
        result.collectsets.addAll(collectsets);
        result.collectsets.addAll(other.collectsets);

        return result;
    }

    public CollectSetSet minimise()
    {
        if (universal)
            return this;

        CollectSetSet minimised = new CollectSetSet();
        minimised.collectsets.addAll(collectsets);

        Iterator i = minimised.collectsets.iterator();
        while (i.hasNext()) {
            HashSet iset = (HashSet) i.next();
            Iterator j = collectsets.iterator();
            while (j.hasNext()) {
                HashSet jset = (HashSet) j.next();
                if (iset.containsAll(jset) && iset != jset) {
                    i.remove();
                    break;
                }
            }
        }
        return minimised;
    }

    public boolean hasVar(String var)
    {
        if (universal)
            return true;

        Iterator i = collectsets.iterator();
        while (i.hasNext()) {
            HashSet set = (HashSet) i.next();
            if (set.contains(var))
                return true;
        }
        return false;
    }

    public boolean hasSingleton(String var)
    {
        if (universal)
            return true;

        HashSet singleton = new HashSet();
        singleton.add(var);
        return collectsets.contains(singleton);
    }
    
    public CollectSetSet retainSingletonsAndSubsetsOf(Collection vars)
    {
        if (universal)
            return this;

        CollectSetSet result = new CollectSetSet();
        Iterator i = collectsets.iterator();

        while (i.hasNext()) {
            HashSet set = (HashSet) i.next();
            if (set.size() == 1 || vars.containsAll(set))
                result.collectsets.add(set);
        }
    	return result;
    }

    public void genCollectTests(TestCodeGen codegen)
    {
        genCollectTests(codegen, collectsets);
    }

    private void genCollectTests(TestCodeGen codegen, HashSet collectsets)
    {
        if (collectsets.isEmpty()) {
            codegen.genNoCollect();
            return;
        }
        if (collectsets.contains(new HashSet())) {
            codegen.genCollect();
            return;
        }

        String testvar = getMostFrequentVar(collectsets);
        HashSet containsvar = new HashSet();
        HashSet rest = new HashSet();
        partition(collectsets, testvar, containsvar, rest);

        Object falsebranch = codegen.getNewBranch();
        codegen.genTest(testvar, falsebranch);
        genCollectTests(codegen, containsvar);
        codegen.insertBranch(falsebranch);
        genCollectTests(codegen, rest);
    }

    private String getMostFrequentVar(HashSet collectsets)
    {
        HashSet allvars = new HashSet();
        Iterator i = collectsets.iterator();
        while (i.hasNext())
            allvars.addAll((HashSet) i.next());

        String max_var = null;
        int max_count = 0;
        i = allvars.iterator();
        while (i.hasNext()) {
            String var = (String) i.next();
            int count = 0;
            Iterator j = collectsets.iterator();
            while (j.hasNext()) {
                HashSet set = (HashSet) j.next();
                if (set.contains(var)) count++;
            }
            if (count > max_count) {
                max_count = count;
                max_var = var;
            }
        }

        return max_var;
    }

    private void partition(HashSet collectsets, String testvar,
                           HashSet containsvar, HashSet rest)
    {
        Iterator i = collectsets.iterator();
        while (i.hasNext()) {
            HashSet orig = (HashSet) i.next();
            if (orig.contains(testvar)) {
                HashSet newset = new HashSet();
                newset.addAll(orig);
                newset.remove(testvar);
                containsvar.add(newset);
            } else {
                rest.add(orig);
            }
        }
    }

    public int hashCode()
    {
        return collectsets.hashCode();
    }

    public boolean equals(Object otherobj)
    {
        if (!(otherobj instanceof CollectSetSet))
            return false;
        CollectSetSet other = (CollectSetSet) otherobj;

        if (universal ^ other.universal)
                return false;

        if (universal)
            return true;

        return this.minimise().collectsets.equals(other.minimise().collectsets);
    }

    public String toString()
    {
        if (universal)
            return "[- universal set -]";

        return collectsets.toString();
    }
    
    public boolean isEmpty() {
    	return collectsets.isEmpty();
    }

    // Tests
    public static void main(String[] args)
    {
        // pretending we have a symbol that binds x, and another
        // that binds y and z...
        Collection x = new HashSet();
        x.add("x");
        Collection xy = new HashSet();
        xy.add("x");
        xy.add("y");
        Collection yz = new HashSet();
        yz.add("y");
        yz.add("z");
        Collection xz = new HashSet();
        xz.add("x");
        xz.add("z");
 
        // tests...
        CollectSetSet empty = new CollectSetSet();
        CollectSetSet weakx = new CollectSetSet(x);
        CollectSetSet weakxy = new CollectSetSet(xy);
        CollectSetSet weakyz = new CollectSetSet(yz);
        CollectSetSet universal = CollectSetSet.universalSet();

        CollectSetSet weakunion = weakx.union(weakyz);
        CollectSetSet weakcross = weakx.cross(weakyz);

        System.out.println(empty);
        System.out.println(weakx);
        System.out.println(weakyz);
        System.out.println(weakunion);
        System.out.println(weakcross);
        System.out.println(weakx.union(weakcross));
        System.out.println(weakx.union(weakcross).minimise());
        System.out.println(weakxy.cross(weakx));
        System.out.println(weakxy.cross(weakyz));
        System.out.println(weakxy.cross(weakyz)
                                 .retainSingletonsAndSubsetsOf(xz));
        System.out.println(universal);

        assert empty.equals(empty);
        assert !universal.equals(weakx);
        assert weakx.equals(weakx);
        assert universal.equals(universal);

        assert !empty.hasVar("x");
        assert !empty.hasSingleton("x");
        assert weakx.hasVar("x");
        assert weakx.hasSingleton("x");
        assert weakyz.hasVar("y");
        assert weakyz.hasSingleton("y");
        assert weakyz.hasVar("z");
        assert weakyz.hasSingleton("z");
        assert universal.hasVar("x");
        assert universal.hasSingleton("x");

        assert universal.union(weakx) == universal;
        assert weakx.union(universal) == universal;
        assert universal.cross(weakx) == weakx;
        assert weakx.cross(universal) == weakx;

        assert weakunion.hasSingleton("x");
        assert weakunion.hasSingleton("y");
        assert !weakcross.hasSingleton("x");
        assert !weakcross.hasSingleton("y");
        assert weakcross.hasVar("x");
        assert weakcross.hasVar("y");
        assert weakcross.hasVar("z");

        assert weakxy.cross(weakyz).retainSingletonsAndSubsetsOf(xz)
                                   .equals(weakxy.cross(weakyz));
        assert !weakxy.cross(weakyz).retainSingletonsAndSubsetsOf(x)
                                    .hasVar("x");
        assert !weakxy.cross(weakyz).retainSingletonsAndSubsetsOf(x)
                                    .hasVar("z");
        assert weakxy.cross(weakyz).retainSingletonsAndSubsetsOf(x)
                                   .hasVar("y");

        assert weakx.union(weakcross).minimise().equals(weakx);

        System.out.println("all assertions passed");

        weakx.cross(weakyz).printCollectTests();
    }

    public void printCollectTests()
    {
        this.genCollectTests(
            new TestCodeGen() {
                private int i = 0;
                public Object getNewBranch() {
                    return new Integer(i++);
                }
                public void insertBranch(Object label) {
                    System.out.println("Label " + label + ":");
                }
                public void genTest(String var, Object label) {
                    System.out.println("  if " + var + " is alive goto "
                                       + label);
                }
                public void genCollect() {
                    System.out.println("  Collect this disjunct");
                }
                public void genNoCollect() {
                    System.out.println("  Do not collect this disjunct");
                }
            }
        );
    }
}
