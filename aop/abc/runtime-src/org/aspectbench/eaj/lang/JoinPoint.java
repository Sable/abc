/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectbench.eaj.lang;

/**
 * <p>Provides reflective access to both the state available at a join point and
 * static information about it.  This information is available from the body
 * of advice using the special form <code>thisJoinPoint</code>.  The primary
 * use of this reflective information is for tracing and logging applications.
 * </p>
 *
 * <pre>
 * aspect Logging {
 *     before(): within(com.bigboxco..*) && execution(public * *(..)) {
 *         System.err.println("entering: " + thisJoinPoint);
 *         System.err.println("  w/args: " + thisJoinPoint.getArgs());
 *         System.err.println("      at: " + thisJoinPoint.getSourceLocation());
 *     }
 * }
 * </pre>
 */
public interface JoinPoint extends org.aspectj.lang.JoinPoint {
    public interface StaticPart extends org.aspectj.lang.JoinPoint.StaticPart {
        public int getOffset();
    }
}
