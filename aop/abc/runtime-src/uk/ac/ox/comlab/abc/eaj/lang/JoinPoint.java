/* *******************************************************************
 * Copyright (c) 2004 Ondrej Lhotak. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Ondrej Lhotak     initial implementation
 * ******************************************************************/

package uk.ac.ox.comlab.abc.eaj.lang;

import uk.ac.ox.comlab.abc.eaj.lang.reflect.BytecodeLocation;

public interface JoinPoint extends org.aspectj.lang.JoinPoint
{
    public BytecodeLocation getBytecodeLocation();
    public interface StaticPart extends org.aspectj.lang.JoinPoint.StaticPart {
        public BytecodeLocation getBytecodeLocation();
    }
}
