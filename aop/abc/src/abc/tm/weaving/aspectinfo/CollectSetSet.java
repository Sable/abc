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
    
    public CollectSetSet retainSingletonsAndSubsetsOf(Collection vars) {
    	return this;
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

        return collectsets.equals(other.collectsets);
    }

    public String toString()
    {
        if (universal)
            return "[- universal set -]";

        return collectsets.toString();
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

        assert weakx.union(weakcross).minimise().equals(weakx);

        System.out.println("all assertions passed");
    }
}
