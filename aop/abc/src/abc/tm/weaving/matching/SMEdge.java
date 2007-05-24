/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Pavel Avgustinov
 * Copyright (C) 2006 Eric Bodden
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
 *  @author Eric Bodden
 */

public class SMEdge implements Cloneable {

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
     * <b>Be careful when calling this, as it might change the hash code of the object!</b>
     * @param label The label to set.
     */
    public void setLabel(String label) {
    	//the label "" was once used for skip loops
    	//but should now not occur any more
    	assert label==null || !label.equals(""); 
        this.label = label;
    }
    /**
     * @return Returns the source.
     */
    public SMNode getSource() {
        return source;
    }
    /**
     * <b>Be careful when calling this, as it might change the hash code of the object!</b>
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
     * <b>Be careful when calling this, as it might change the hash code of the object!</b>
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
    
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((source == null) ? 0 : source.hashCode());
		result = PRIME * result + ((target == null) ? 0 : target.hashCode());
		result = PRIME * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final SMEdge other = (SMEdge) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
    
    /**
     * Tells whether this edge is a skip edge.
     * @return <code>false</code>, since skip loops are represented by a subclass of this class
     */
    public boolean isSkipEdge() {
    	return false;
    }
    
    /** 
     * {@inheritDoc}
     */
    protected Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
    	return getLabel();
    }
}
