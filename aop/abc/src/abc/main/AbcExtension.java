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

/**
 * This class should be sub-classed to extend the behaviour of abc
 * <p>
 * A sub-class, with overriden methods to effect some new behaviour,
 * can be loaded at runtime by using the "-ext" switch to abc.
 *
 * @author Julian Tibble
 */
public class AbcExtension
{
    /**
     * Constructs a version string for all loaded extensions
     */
    final public String versions()
    {
        StringBuffer versions = new StringBuffer();
        collectVersions(versions);
        return versions.toString();
    }

    /*
     * Override this method to add the version information
     * for this extension, calling the same method in the
     * super-class to ensure that all extensions are
     * reported.
     */
    protected void collectVersions(StringBuffer versions)
    {
        versions.append("abc version " +
                        new abc.aspectj.Version().toString() +
                        "\n");
    }

    /*
     * Creates an instance of the <code>ExtensionInfo</code> structure
     * used for extending the Polyglot-based frontend.
     */
    public abc.aspectj.ExtensionInfo
            makeExtensionInfo(Collection jar_classes,
                              Collection aspect_sources)
    {
        return new abc.aspectj.ExtensionInfo(jar_classes, aspect_sources);
    }

    /**
     * Get all the shadow joinpoints that are matched
     * in this extension of AspectJ
     */
    final public Iterator /*<ShadowType>*/ shadowTypes()
    {
        return listShadowTypes().iterator();
    }

    /**
     * Override this method to add new joinpoints to the abc.
     * calling the same method in the super-class to ensure
     * the standard joinpoints needed are loaded too.
     */
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

    /**
     * FIXME : write something here
     */
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

    /**
     * Create an instance of the Weaver, which is parameterized by
     * the name of the factory used to create AspectJ objects at
     * the runtime of the compiled program.
     */
    public abc.weaving.weaver.Weaver makeWeaver()
    {
         return new abc.weaving.weaver.Weaver(
                         "uk.ac.ox.comlab.abc.runtime.reflect.Factory");
    }
}
