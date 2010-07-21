/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package p;

public class ComplexExtract {
	protected ComplexExtractParameter parameterObject = new ComplexExtractParameter(0, 5, 0, 5); //call modified

	public void foo(){
		parameterObject.test3++;
		parameterObject.test= 5+7;
		System.out.println(parameterObject.test+" "+parameterObject.test4);
	}
}