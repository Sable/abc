package abc.weaving.aspectinfo;

import polyglot.util.Position;

/** A pointcut variable. */
public class Var extends Syntax {
    private String name;
    private int index;

    public Var(String name, Position pos) {
	this(name, -1, pos);
    }

    public Var(String name, int index, Position pos) {
	super(pos);
	this.name = name;
	this.index = index;
    }

    public String getName() {
	return name;
    }

    public int getIndex() {
	return index;
    }
}
