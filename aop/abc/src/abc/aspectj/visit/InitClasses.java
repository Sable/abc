/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.aspectj.visit;

import java.util.Iterator;

import polyglot.frontend.Pass;
import polyglot.types.ClassType;
import polyglot.types.Resolver;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.InternalCompilerError;
import abc.aspectj.ExtensionInfo;
import abc.weaving.aspectinfo.AbcFactory;

/** Loads all classes specified as classfile input and
 *  registers them as weavable classes.
 *  @author Aske Simon Christensen
 */
public class InitClasses extends OncePass {
    private ExtensionInfo ext;
    private TypeSystem ts;

    public InitClasses(Pass.ID id, ExtensionInfo ext, TypeSystem ts) {
        super(id);
        this.ext = ext;
        this.ts = ts;
    }

    public void once() {
        try {
            Resolver res = ts.loadedResolver();

            AbcFactory.init(res);

            // Fetch all the weavable classes and put them in the right places
            Iterator<String> wcni = ext.jar_classes.iterator();
            while (wcni.hasNext()) {
                String wcn = wcni.next();
                ClassType ct = (ClassType)res.find(wcn);
                if (ct == null) {
                    throw new InternalCompilerError("Class type of jar class was null");
                }
                ext.hierarchy.insertClassAndSuperclasses(ct, true);
                AbcFactory.registerName(ct, wcn);
                abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().addWeavableClass(AbcFactory.AbcClass(ct));
            }

            abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().initPrecedenceRelation(ext.prec_rel);

            ext.pattern_matcher = PatternMatcher.create(ext.hierarchy);
        } catch (SemanticException e) {
            throw new InternalCompilerError("Class from jar not found by Polyglot",e);
        }
    }
}
