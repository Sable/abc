package test;

public class Dependency {
	protected final ASTNode box;
	//protected final Attribute attr;
  protected final int offset;
	protected final Object args;
	
  /*
	public Dependency(ASTNode box, Attribute attr) {
		this.box = box;
		this.attr = attr;
		this.args = null;
    this.offset = -1;
	}
	
	public Dependency(ASTNode box, Attribute attr, Object args) {
		this.box = box;
		this.attr = attr;
		this.args = args;
    this.offset = -1;
	}
  */

	public Dependency(ASTNode box, int offset) {
		this.box = box;
		//this.attr = null;
		this.args = null;
    this.offset = offset;
	}
	public Dependency(ASTNode box, int offset, Object args) {
		this.box = box;
    //this.attr = null;
		this.offset = offset;
		this.args = args;
	}


	public Object eval() {
    return box.eval(offset, args);
    /*
		switch(attr) {
		case Width:
			return box.compute_width();
		case Height:
			return box.compute_height();
		case X:
			return box.compute_getX();
		case Y:
			return box.compute_getY();
		case Root:
			return ((BoundingBox)box).compute_getRoot();
		case Parent:
			return box.compute_getParent();
		case Child:
			if(box instanceof HBox) {
				return ((HBox)box).compute_getChild((Integer)args);
			} else {
				return ((VBox)box).compute_getChild((Integer)args);
			}
		case NumChild:
			if(box instanceof HBox) {
				return ((HBox)box).compute_getNumChild();
			} else {
				return ((VBox)box).compute_getNumChild();
			}
		}
		throw new RuntimeException("unknown attribute "+attr);
    */
	}
	
	public String toString() {
		return box+"."+offset;
	}
}
