package jastaddmodules.translator.oomodules;

public class ReplaceDeclaration {
	protected ConcreteModule context;
	protected ModuleReference dest;
	protected ModuleReference src;
	
	public ReplaceDeclaration(ConcreteModule context, 
			ModuleReference dest, 
			ModuleReference src) {
		this.context = context;
		this.dest = dest;
		this.src = src;
	}

	public String toString() {
		String ret = "";
		
		ret += "replace " + dest + " with " + src;
		
		return ret;
	}
}
