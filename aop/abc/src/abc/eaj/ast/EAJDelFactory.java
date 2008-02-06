/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Eric Bodden
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

package abc.eaj.ast;

import abc.aspectj.ast.AJDelFactory;
import polyglot.ast.JL;

/**
 * @author Eric Bodden
 * @author Pavel Avgustinov
 */
public interface EAJDelFactory extends AJDelFactory {

    /**
     * @return delegates for cast pointcuts
     */
    public JL delPCCast();

    /**
     * @return delegates for cast pointcuts
     */
    public JL delPCThrow();
    
    /**
     * @return delegates for pointcut local variables node
     */
    public JL delPCLocalVars();
    
    /**
     * @return delegates for global pointcut declarations
     */
    public JL delGlobalPointcutDecl();

    /**
     * @return delegates for EAJ advice declarations
     */
    public JL delEAJAdviceDecl();

    /**
     * @return delegates for cflowdepth pointcuts
     */
    public JL delPCCflowDepth();

    /**
     * @return delegates for cflowbelowdepth pointcuts
     */
    public JL delPCCflowBelowDepth();

    /**
     * @return delegates for let pointcuts
     */
    public JL delPCLet();

    /**
     * @return delegates for contains pointcuts
     */
    public JL delPCContains();
    
	/**
     * @return delegates for arrayget pointcuts
	 */
    public JL delPCArrayGet();
    
	/**
     * @return delegates for arrayset pointcuts
	 */
    public JL delPCArraySet();

	/**
     * @return delegates for monitorenter pointcuts
	 */
	public JL delPCMonitorEnter();

	/**
     * @return delegates for monitorexit pointcuts
	 */
	public JL delPCMonitorExit();

	/**
     * @return delegates for maybe-shared pointcuts
	 */
	public JL delPCMaybeShared();
}
