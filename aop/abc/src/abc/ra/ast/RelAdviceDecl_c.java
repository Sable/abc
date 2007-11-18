/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AdviceFormal;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;
import abc.ra.types.RelAspectType;
import abc.tm.ast.TMDecl;

/**
 * Declaration of a relational advice. This node type only exists for parsing
 * and type checking. It is rewritten into a {@link RelAdviceTMDecl_c} right
 * after parsing.
 * 
 * @author Eric Bodden
 */
public class RelAdviceDecl_c extends AdviceDecl_c implements RelAdviceDecl {

    /**
     * Creates a new relational advice declaration.
     * 
     * @param pos
     *            position of this AST node
     * @param flags
     *            modifier of the advice
     * @param spec
     *            advice spec
     * @param throwTypes
     *            list of throw types
     * @param pc
     *            pointcut for this advice
     * @param body
     *            advice body
     */
    @SuppressWarnings("unchecked")
    public RelAdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
            List throwTypes, Pointcut pc, Block body) {
        super(pos, flags, spec, throwTypes, pc, body);
    }

    /**
     * Typechecks this relational advice. The advice may only be contained in a
     * relational aspect. Also, it may not declare any advice formals that have
     * the same name as any of the relational aspect formals. If after
     * returning/throwing, the same is true for the return variable.
     * Further we check that this relational advice decl is contained in a relational aspect.
     */
    @SuppressWarnings("unchecked")
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        RelAspectType at = (RelAspectType) tc.context().currentClass();
        if (!at.relational()) {
            throw new SemanticException(
                    "Only relational aspects can contain relational advice.",
                    position());
        } else {
            Collection<Formal> allAdviceFormals = new ArrayList<Formal>(formals);
            AdviceFormal returnVal = spec.returnVal();
            if (returnVal != null)
                allAdviceFormals.add(returnVal);

            for (Iterator<Formal> adviceFormalIter = allAdviceFormals
                    .iterator(); adviceFormalIter.hasNext();) {
                Formal adviceFormal = (Formal) adviceFormalIter.next();
                for (Iterator<Formal> aspectFormalIter = at
                        .relationalAspectFormals().iterator(); aspectFormalIter
                        .hasNext();) {
                    Formal aspectFormal = (Formal) aspectFormalIter.next();
                    if (adviceFormal.name().equals(aspectFormal.name())) {
                        throw new SemanticException("Name of advice formal "
                                + adviceFormal.name()
                                + " clashes with relational aspect "
                                + "formal of same name.", adviceFormal
                                .position());
                    }
                }
            }
        }

        boolean isSurroundedByRelationalAspect = false;
        if (tc.context().currentClass() instanceof RelAspectType) {
            RelAspectType raType = (RelAspectType) tc.context().currentClass();
            if (raType.relational())
                isSurroundedByRelationalAspect = true;
        }
        if (!isSurroundedByRelationalAspect) {
            throw new SemanticException(
                    "Relational advice may only be declared within a relational aspect.",
                    position);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public TMDecl genTraceMatch(RelAspectDecl container, RANodeFactory nf,
            TypeSystem ts) {
        return new RelAdviceTMDecl_c(position, spec, pc, formals, throwTypes,
                body, container, this, nf, ts);
    }

}
