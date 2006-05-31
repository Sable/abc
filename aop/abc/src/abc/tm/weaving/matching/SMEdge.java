/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Pavel Avgustinov
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

package abc.tm.weaving.matching;

/** 
 * An edge in the state machine. Has source and target nodes and edge label.
 * 
 * Null labels mean epsilon transitions.
 *
 *  @author Pavel Avgustinov
 */

public class SMEdge implements Cloneable {

	public final static String SKIP_LABEL = ""; 
	
    protected SMNode source, target;
    // TODO: Is SymbolDecl really the type to choose?
    //-> Yeah, String is not very extensible, really. (Eric)
    protected String label;
    
    public SMEdge(SMNode from, SMNode to, String l) {
        source = from;
        target = to;
        label = l;
    }
    
    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @return Returns the source.
     */
    public SMNode getSource() {
        return source;
    }
    /**
     * @param source The source to set.
     */
    public void setSource(SMNode source) {
        this.source = source;
    }
    /**
     * @return Returns the target.
     */
    public SMNode getTarget() {
        return target;
    }
    /**
     * @param target The target to set.
     */
    public void setTarget(SMNode target) {
        this.target = target;
    }
    
    /**
     * Flips this edge, reversing source and target.
     */
    public void flip() {
    		this.source.removeOutEdge(this);
    		this.target.removeInEdge(this);
    		SMNode tmp = this.source;
    		this.source = this.target;
    		this.target = tmp;
    		this.source.addOutgoingEdge(this);
    		this.target.addIncomingEdge(this);
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof SMEdge)) return false;
        SMEdge edge = (SMEdge) o;
        return (edge.getSource() == this.source && edge.getTarget() == this.target &&
                edge.getLabel() == this.label);
    }
    
    /**
     * Tells whether this edge is a skip edge.
     * @return <code>true</code> if this edge is a skip edge
     */
    public boolean isSkipEdge() {
    	boolean isSkipEdge = getLabel().equals(SKIP_LABEL);
    	//assert that if this is a skip edge, then it is on fact a loop
    	assert !isSkipEdge || getSource() == getTarget();
    	return isSkipEdge;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
}
