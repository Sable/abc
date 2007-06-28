/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.matching;

import polyglot.util.Position;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnNamePosTag;
import soot.tagkit.SourceLnPosTag;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;

/** Application of advice at a standard statement joinpoint
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class StmtAdviceApplication extends AdviceApplication {
    protected Stmt stmt;
    
    public StmtAdviceApplication(AbstractAdviceDecl advice,
				 Residue residue,
				 Stmt stmt) {
	super(advice,residue);
	this.stmt=stmt;
    }

    public void debugInfo(String prefix,StringBuffer sb) {
    	sb.append(prefix+"stmt: "+stmt+"\n");
    	Position statementPos = statementPosition();
    	if(statementPos!=null) {
    		sb.append(prefix+"position: "+statementPos+"\n");
    	}
    	super.debugInfo(prefix,sb);
    }

    public Position statementPosition() {
    	if(stmt.hasTag("SourceLnPosTag")) {
    		SourceLnPosTag tag = (SourceLnPosTag) stmt.getTag("SourceLnPosTag");
    		String fileName = "";
    		if(tag instanceof SourceLnNamePosTag) {
				SourceLnNamePosTag nameTag = (SourceLnNamePosTag) tag;
    			fileName = nameTag.getFileName();
    		}    		
    		return new Position(fileName,tag.startLn(),tag.startPos(),tag.endLn(),tag.endPos());
    	} else if(stmt.hasTag("LineNumberTag")) {
    		LineNumberTag tag = (LineNumberTag) stmt.getTag("LineNumberTag");
    		return new Position("",tag.getLineNumber());
    	} else {
    		return null;
    	}
    }
    
    public String toString() {
    	return "stmt : "+stmt;
    }
    
    public AdviceApplication inline( ConstructorInliningMap cim ) {
        StmtAdviceApplication ret = new StmtAdviceApplication(advice, getResidue().inline(cim), cim.map(stmt));
        ret.shadowmatch = shadowmatch.inline(cim);
        return ret;
    }
}
    
				      
