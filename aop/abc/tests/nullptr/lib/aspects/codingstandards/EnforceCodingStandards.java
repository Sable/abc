package lib.aspects.codingstandards;


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * EnforceCodingStandards is an aspect that enforces design invariant coding
 * standards.
 *
 * Copyright (C) 2002  R. Dale Asberry
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * <!-- Description: -->
 *  The purpose of this aspect is to enforce coding standards.
 *
 *  @author R. Dale Asberry
 *
 * <!-- CHANGE HISTORY:
 *  22-Sep-2002 R. Dale Asberry : Created class.
 * -->
 */

public aspect EnforceCodingStandards
{
	
	/**
	 * <!-- Description: -->
	 *  This pointcut is intended to match all method calls that return any subclass 
	 *  of Object except for methods with a "void" return type.
	 *
	 *  The first pointcut matches all calls. 
	 * 
	 *  The second pointcut prevents matching those that have a void return type.
	 * 
	 *  @author R. Dale Asberry
	 * 
	 * <!-- CHANGE HISTORY:
	 *  22-Sep-2002 R. Dale Asberry : Created pointcut.
	 * -->
	 */
	
	pointcut methodsThatReturnObjects():
		call(* *.*(..)) && !call(void *.*(..));
	
	/**
	 * <!-- Description: -->
	 *  This advice is intended to intercept all method calls that have an Object
	 *  (or subclass) return type, except those returning "void".  It will log an 
	 *  error message if the return value is null.
	 * 
	 *  @author R. Dale Asberry
	 * 
	 * <!-- CHANGE HISTORY:
	 *  22-Sep-2002 R. Dale Asberry : Created advice.
	 * -->
	 */
	
	Object around(): methodsThatReturnObjects()
	{
		Object lRetVal = proceed();
		if(lRetVal == null)
		{
			System.err.println(
							"Detected null return value after calling " + 
							thisJoinPoint.getSignature().toShortString() + 
							" in file " + thisJoinPoint.getSourceLocation().getFileName() +
							" at line " + thisJoinPoint.getSourceLocation().getLine()
							);
		}
		return lRetVal;
	}
}