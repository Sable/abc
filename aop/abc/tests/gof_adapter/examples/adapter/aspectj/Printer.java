package examples.adapter.aspectj;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.patterns.
 *
 * Contributor(s):   
 */

/**
 * Defines the target inteface with a general print method. Acts as the 
 * <i>Target</i> in the pattern context.
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.0, 05/13/02
 */
 
public interface Printer {
	
    /**
     * Prints the argument string. In the pattern context, this is the
     * <i>request()</i> method on the <i>Target</i>. Implemented by
     * PrinterScreenAdapter which acts as <i>Adapter</i>.
     *
     * @param s the string to print
     * @see PrinterScreenAdapter 
     */
     
	public void print(String s);
}