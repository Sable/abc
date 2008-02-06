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

import abc.aspectj.ast.AJExtFactory;
import polyglot.ast.Ext;

/**
 * @author Eric Bodden
 * @author Pavel Avgustinov
 */
public interface EAJExtFactory extends AJExtFactory {

    /**
     * @return extensions for cast pointcuts
     */
    public Ext extPCCast();

    /**
     * @return extensions for throw pointcuts
     */
    public Ext extPCThrow();

    /**
     * @return extensions for pointcut local variables node
     */
    public Ext extPCLocalVars();

    /**
     * @return extensions for global pointcut declarations
     */
    public Ext extGlobalPointcutDecl();

    /**
     * @return extensions for EAJ advice declarations
     */
    public Ext extEAJAdviceDecl();

    /**
     * @return extensions for cflowdepth pointcuts 
     */
    public Ext extPCCflowDepth();

    /**
     * @return extensions for cflowbelowdepth pointcuts
     */
    public Ext extPCCflowBelowDepth();

    /**
     * @return extensions for let pointcuts
     */
    public Ext extPCLet();

    /**
     * @return extension for contains pointcuts
     */
    public Ext extPCContains();
    
	/**
	 * @return extension for arrayget pointcuts
	 */
    public Ext extPCArrayGet();
    
	/**
	 * @return extension for arrayset pointcuts
	 */
    public Ext extPCArraySet();

	/**
	 * @return extension for monitorenter pointcuts
	 */
	public Ext extPCMonitorEnter();

	/**
	 * @return extension for monitorexit pointcuts
	 */
	public Ext extPCMonitorExit();
    
	/**
	 * @return extension for maybe-shared pointcuts
	 */
	public Ext extPCMaybeShared();
}
