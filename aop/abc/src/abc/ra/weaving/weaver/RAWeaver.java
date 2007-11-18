/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra.weaving.weaver;

import java.util.Iterator;

import abc.ra.weaving.aspectinfo.RelationalAspect;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.TMWeaver;
import abc.weaving.aspectinfo.Aspect;

/**
 * Extended weaver which triggers code geenration for relational aspects in the backend.
 * @author Eric Bodden
 */
public class RAWeaver extends TMWeaver {
	
	/** 
	 * {@inheritDoc}
	 */
	public void weaveGenerateAspectMethods() {
		super.weaveGenerateAspectMethods();
		
        Iterator it = ((TMGlobalAspectInfo)abc.main.Main.v().getAbcExtension().getGlobalAspectInfo()).getAspects().iterator();
        while(it.hasNext()) {
            Aspect aspect = (Aspect)it.next();
            if(aspect instanceof RelationalAspect) {
            	RelationalAspect relAspect = (RelationalAspect) aspect;
            	relAspect.codeGen();
            }
        }
	
	}

}
