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


package org.aspectbench.eaj.runtime.reflect;

import org.aspectbench.eaj.lang.*;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;

public class JoinPointImpl extends org.aspectbench.runtime.reflect.JoinPointImpl implements JoinPoint {
    public JoinPointImpl(org.aspectj.lang.JoinPoint.StaticPart staticPart, Object _this, Object target, Object[] args) {
        super(staticPart, _this, target, args);
    }
    static class StaticPartImpl extends org.aspectbench.runtime.reflect.JoinPointImpl.StaticPartImpl implements JoinPoint.StaticPart, JoinPoint {
        int offset;
        public StaticPartImpl(String kind, Signature signature, SourceLocation sourceLocation, int offset) {
            super(kind, signature, sourceLocation);
            this.offset = offset;
        }

        public int getOffset() { return offset; }
    }
}
