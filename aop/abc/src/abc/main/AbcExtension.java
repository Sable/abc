/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.main;

import abc.weaving.matching.*;

import soot.Scene;
import soot.SootClass;

import java.util.*;

/*
 * @author Julian Tibble
 *
 */
public class AbcExtension
{
    final public String versions()
    {
        StringBuffer versions = new StringBuffer();
        collectVersions(versions);
        return versions.toString();
    }

    protected void collectVersions(StringBuffer versions)
    {
        versions.append("abc version " +
                        new abc.aspectj.Version().toString() +
                        "\n");
    }

    public abc.aspectj.ExtensionInfo
            makeExtensionInfo(Collection jar_classes,
                              Collection aspect_sources)
    {
        return new abc.aspectj.ExtensionInfo(jar_classes, aspect_sources);
    }

    final public Iterator shadowTypes()
    {
        return listShadowTypes().iterator();
    }

    protected List /*<ShadowType>*/ listShadowTypes()
    {
        List /*<ShadowType*/ shadowTypes = new LinkedList();

        shadowTypes.add(new ConstructorCallShadowType());
        shadowTypes.add(new ExecutionShadowType());
        shadowTypes.add(new GetFieldShadowType());
        shadowTypes.add(new HandlerShadowType());

        // the next two lines show the preferred method of doing this
        // i.e. without creating the extra *ShadowType class
        // FIXME: make all of the join point matching classes like this
        shadowTypes.add(ClassInitializationShadowMatch.shadowType());
        shadowTypes.add(InterfaceInitializationShadowMatch.shadowType());

        shadowTypes.add(new MethodCallShadowType());
        shadowTypes.add(new PreinitializationShadowType());
        shadowTypes.add(new SetFieldShadowType());

        return shadowTypes;
    }

    public void addBasicClassesToSoot()
    {
        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.runtime.internal.CFlowStack",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.runtime.reflect.Factory",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.aspectj.lang.JoinPoint",
                                SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.aspectj.lang.JoinPoint$StaticPart",
                                SootClass.HIERARCHY);
        Scene.v().addBasicClass("org.aspectj.lang.SoftException",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.aspectj.lang.NoAspectBoundException",
                                SootClass.SIGNATURES);
        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.runtime.internal.CFlowCounter",
                                SootClass.SIGNATURES);
    }

    public abc.weaving.weaver.Weaver makeWeaver()
    {
         return new abc.weaving.weaver.Weaver(
                         "uk.ac.ox.comlab.abc.runtime.reflect.Factory");
    }
}
