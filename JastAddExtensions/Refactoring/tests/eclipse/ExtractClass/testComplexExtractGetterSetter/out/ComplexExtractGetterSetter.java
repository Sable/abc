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

public class ComplexExtractGetterSetter {
	protected ComplexExtractGetterSetterParameter parameterObject = new ComplexExtractGetterSetterParameter(0, 5, 0, 5); //call modified

	public void foo(){
		parameterObject.setTest3(parameterObject.getTest3() + 1);
		parameterObject.setTest(5+7);
		System.out.println(parameterObject.getTest()+" "+parameterObject.getTest4());
	}
}