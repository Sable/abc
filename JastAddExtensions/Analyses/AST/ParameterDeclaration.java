
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;


public class ParameterDeclaration extends ASTNode implements Cloneable,  SimpleSet,  Iterator,  Variable,  LocalDeclaration {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
        shouldMoveInto_Stmt_Stmt_values = null;
        isLiveBetween_Stmt_Stmt_values = null;
        isLiveAfter_Stmt_values = null;
        isLiveAtOrAfter_Stmt_values = null;
        isValueParmFor_Stmt_Stmt_values = null;
        isOutParmFor_Stmt_Stmt_values = null;
        mayDefBetween_Stmt_Stmt_values = null;
        accessedOutside_Stmt_Stmt_values = null;
        accessedBefore_Stmt_values = null;
        accessedAfter_Stmt_values = null;
        shouldMoveOutOf_Stmt_Stmt_values = null;
        shouldDuplicate_Stmt_Stmt_values = null;
        getBlock_computed = false;
        getBlock_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ParameterDeclaration node = (ParameterDeclaration)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.shouldMoveInto_Stmt_Stmt_values = null;
        node.isLiveBetween_Stmt_Stmt_values = null;
        node.isLiveAfter_Stmt_values = null;
        node.isLiveAtOrAfter_Stmt_values = null;
        node.isValueParmFor_Stmt_Stmt_values = null;
        node.isOutParmFor_Stmt_Stmt_values = null;
        node.mayDefBetween_Stmt_Stmt_values = null;
        node.accessedOutside_Stmt_Stmt_values = null;
        node.accessedBefore_Stmt_values = null;
        node.accessedAfter_Stmt_values = null;
        node.shouldMoveOutOf_Stmt_Stmt_values = null;
        node.shouldDuplicate_Stmt_Stmt_values = null;
        node.getBlock_computed = false;
        node.getBlock_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ParameterDeclaration node = (ParameterDeclaration)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ParameterDeclaration res = (ParameterDeclaration)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in DataStructures.jrag at line 92

  public SimpleSet add(Object o) {
    return new SimpleSetImpl().add(this).add(o);
  }

    // Declared in DataStructures.jrag at line 98

  private ParameterDeclaration iterElem;

    // Declared in DataStructures.jrag at line 99

  public Iterator iterator() { iterElem = this; return this; }

    // Declared in DataStructures.jrag at line 100

  public boolean hasNext() { return iterElem != null; }

    // Declared in DataStructures.jrag at line 101

  public Object next() { Object o = iterElem; iterElem = null; return o; }

    // Declared in DataStructures.jrag at line 102

  public void remove() { throw new UnsupportedOperationException(); }

    // Declared in NameCheck.jrag at line 324

  
  public void nameCheck() {
    SimpleSet decls = outerScope().lookupVariable(name());
    for(Iterator iter = decls.iterator(); iter.hasNext(); ) {
      Variable var = (Variable)iter.next();
      if(var instanceof VariableDeclaration) {
        VariableDeclaration decl = (VariableDeclaration)var;
	      if(decl.enclosingBodyDecl() == enclosingBodyDecl())
  	      error("duplicate declaration of local variable " + name());
      }
      else if(var instanceof ParameterDeclaration) {
        ParameterDeclaration decl = (ParameterDeclaration)var;
	      if(decl.enclosingBodyDecl() == enclosingBodyDecl())
          error("duplicate declaration of local variable " + name());
      }
    }

    // 8.4.1  
    if(!lookupVariable(name()).contains(this)) {
      error("duplicate declaration of parameter " + name());
    }
  }

    // Declared in NodeConstructors.jrag at line 2

  public ParameterDeclaration(Access type, String name) {
    this(new Modifiers(new List()), type, name);
  }

    // Declared in NodeConstructors.jrag at line 5

  public ParameterDeclaration(TypeDecl type, String name) {
    this(new Modifiers(new List()), type.createQualifiedAccess(), name);
  }

    // Declared in NodeConstructors.jrag at line 9


  public ParameterDeclaration(Modifiers m, Access a, String i) {
    this(m, a, i, new List());
  }

    // Declared in PrettyPrint.jadd at line 257


  public void toString(StringBuffer s) {
    getModifiers().toString(s);
    getTypeAccess().toString(s);
    s.append(" " + name());
    for(int i = 0; i < getNumEmptyBracket(); i++) {
      s.append("[]");
    }
  }

    // Declared in GuardedControlFlow.jrag at line 37

	
	public boolean between(Stmt begin, Stmt end) {
		return false;
	}

    // Declared in ParameterClassification.jrag at line 17

	
	public ParameterDeclaration asParameterDeclaration() {
		return (ParameterDeclaration)fullCopy();
	}

    // Declared in ParameterClassification.jrag at line 25

	
	public VariableDeclaration asVariableDeclaration() {
		return new VariableDeclaration((Access)getTypeAccess().fullCopy(), getID());
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 83

    public ParameterDeclaration() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(new List(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 83
    public ParameterDeclaration(Modifiers p0, Access p1, String p2, List p3) {
        setChild(p0, 0);
        setChild(p1, 1);
        setID(p2);
        setChild(p3, 2);
    }

    // Declared in java.ast at line 20


  protected int numChildren() {
    return 3;
  }

    // Declared in java.ast at line 23

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 83
    public void setModifiers(Modifiers node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Modifiers getModifiers() {
        return (Modifiers)getChild(0);
    }

    // Declared in java.ast at line 9


    public Modifiers getModifiersNoTransform() {
        return (Modifiers)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 83
    public void setTypeAccess(Access node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Access getTypeAccess() {
        return (Access)getChild(1);
    }

    // Declared in java.ast at line 9


    public Access getTypeAccessNoTransform() {
        return (Access)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 83
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 83
    public void setEmptyBracketList(List list) {
        setChild(list, 2);
    }

    // Declared in java.ast at line 6


    private int getNumEmptyBracket = 0;

    // Declared in java.ast at line 7

    public int getNumEmptyBracket() {
        return getEmptyBracketList().getNumChild();
    }

    // Declared in java.ast at line 11


    public EmptyBracket getEmptyBracket(int i) {
        return (EmptyBracket)getEmptyBracketList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addEmptyBracket(EmptyBracket node) {
        List list = getEmptyBracketList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setEmptyBracket(EmptyBracket node, int i) {
        List list = getEmptyBracketList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getEmptyBracketList() {
        return (List)getChild(2);
    }

    // Declared in java.ast at line 28


    public List getEmptyBracketListNoTransform() {
        return (List)getChildNoTransform(2);
    }

    // Declared in DataStructures.jrag at line 90
    public int size() {
        int size_value = size_compute();
        return size_value;
    }

    private int size_compute() {  return  1;  }

    // Declared in DataStructures.jrag at line 91
    public boolean isEmpty() {
        boolean isEmpty_value = isEmpty_compute();
        return isEmpty_value;
    }

    private boolean isEmpty_compute() {  return  false;  }

    // Declared in DataStructures.jrag at line 95
    public boolean contains(Object o) {
        boolean contains_Object_value = contains_compute(o);
        return contains_Object_value;
    }

    private boolean contains_compute(Object o) {  return  this == o;  }

    // Declared in PrettyPrint.jadd at line 948
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 245
    public TypeDecl type() {
        if(type_computed)
            return type_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        type_value = type_compute();
        if(isFinal && num == boundariesCrossed)
            type_computed = true;
        return type_value;
    }

    private TypeDecl type_compute()  {
    TypeDecl type = getTypeAccess().type();
    for(int i = 0; i < getNumEmptyBracket(); i++)
      type = type.arrayType();
    return type;
  }

    // Declared in VariableDeclaration.jrag at line 59
    public boolean isClassVariable() {
        boolean isClassVariable_value = isClassVariable_compute();
        return isClassVariable_value;
    }

    private boolean isClassVariable_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 60
    public boolean isInstanceVariable() {
        boolean isInstanceVariable_value = isInstanceVariable_compute();
        return isInstanceVariable_value;
    }

    private boolean isInstanceVariable_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 64
    public boolean isLocalVariable() {
        boolean isLocalVariable_value = isLocalVariable_compute();
        return isLocalVariable_value;
    }

    private boolean isLocalVariable_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 78
    public boolean isFinal() {
        boolean isFinal_value = isFinal_compute();
        return isFinal_value;
    }

    private boolean isFinal_compute() {  return  getModifiers().isFinal();  }

    // Declared in VariableDeclaration.jrag at line 79
    public boolean isBlank() {
        boolean isBlank_value = isBlank_compute();
        return isBlank_value;
    }

    private boolean isBlank_compute() {  return  true;  }

    // Declared in VariableDeclaration.jrag at line 80
    public boolean isStatic() {
        boolean isStatic_value = isStatic_compute();
        return isStatic_value;
    }

    private boolean isStatic_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 82
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    // Declared in VariableDeclaration.jrag at line 84
    public boolean hasInit() {
        boolean hasInit_value = hasInit_compute();
        return hasInit_value;
    }

    private boolean hasInit_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 85
    public Expr getInit() {
        Expr getInit_value = getInit_compute();
        return getInit_value;
    }

    private Expr getInit_compute()  { throw new UnsupportedOperationException(); }

    // Declared in VariableDeclaration.jrag at line 86
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute()  { throw new UnsupportedOperationException(); }

    protected java.util.Map shouldMoveInto_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 87
    public boolean shouldMoveInto(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(shouldMoveInto_Stmt_Stmt_values == null) shouldMoveInto_Stmt_Stmt_values = new java.util.HashMap(4);
        if(shouldMoveInto_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)shouldMoveInto_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean shouldMoveInto_Stmt_Stmt_value = shouldMoveInto_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            shouldMoveInto_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(shouldMoveInto_Stmt_Stmt_value));
        return shouldMoveInto_Stmt_Stmt_value;
    }

    private boolean shouldMoveInto_compute(Stmt begin, Stmt end) {  return  false;  }

    protected java.util.Set isLiveBetween_Stmt_Stmt_visited;
    protected java.util.Set isLiveBetween_Stmt_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set isLiveBetween_Stmt_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map isLiveBetween_Stmt_Stmt_values = new java.util.HashMap(4);
    public boolean isLiveBetween(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(isLiveBetween_Stmt_Stmt_visited == null) isLiveBetween_Stmt_Stmt_visited = new java.util.HashSet(4);
if(isLiveBetween_Stmt_Stmt_values == null) isLiveBetween_Stmt_Stmt_values = new java.util.HashMap(4);
        if(isLiveBetween_Stmt_Stmt_computed.contains(_parameters))
            return ((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
        if (!isLiveBetween_Stmt_Stmt_initialized.contains(_parameters)) {
            isLiveBetween_Stmt_Stmt_initialized.add(_parameters);
            isLiveBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isLiveBetween_Stmt_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_isLiveBetween_Stmt_Stmt_value;
            do {
                CHANGE = false;
                new_isLiveBetween_Stmt_Stmt_value = isLiveBetween_compute(begin, end);
                if (new_isLiveBetween_Stmt_Stmt_value!=((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                isLiveBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveBetween_Stmt_Stmt_value));
            } while (CHANGE);
            isLiveBetween_Stmt_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            isLiveBetween_Stmt_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            isLiveBetween_compute(begin, end);
            RESET_CYCLE = false;
            isLiveBetween_Stmt_Stmt_computed.remove(_parameters);
            isLiveBetween_Stmt_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_isLiveBetween_Stmt_Stmt_value;
        }
        if(!isLiveBetween_Stmt_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                isLiveBetween_Stmt_Stmt_computed.remove(_parameters);
                isLiveBetween_Stmt_Stmt_initialized.remove(_parameters);
                return ((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
            }
            isLiveBetween_Stmt_Stmt_visited.add(_parameters);
            boolean new_isLiveBetween_Stmt_Stmt_value = isLiveBetween_compute(begin, end);
            if (new_isLiveBetween_Stmt_Stmt_value!=((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            isLiveBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveBetween_Stmt_Stmt_value));
            isLiveBetween_Stmt_Stmt_visited.remove(_parameters);
            return new_isLiveBetween_Stmt_Stmt_value;
        }
        return ((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean isLiveBetween_compute(Stmt begin, Stmt end)  {
		if(begin.mayUse(this)) return true;
		if(begin.mayDef(this)) return false;
		if(begin == end) return false;
		for(Iterator i=begin.gsucc(begin, end).iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(isLiveBetween(next, end))
				return true;
		}
		return false;
	}

    protected java.util.Map isLiveAfter_Stmt_values;
    // Declared in Liveness.jrag at line 15
    public boolean isLiveAfter(Stmt stmt) {
        Object _parameters = stmt;
if(isLiveAfter_Stmt_values == null) isLiveAfter_Stmt_values = new java.util.HashMap(4);
        if(isLiveAfter_Stmt_values.containsKey(_parameters))
            return ((Boolean)isLiveAfter_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isLiveAfter_Stmt_value = isLiveAfter_compute(stmt);
        if(isFinal && num == boundariesCrossed)
            isLiveAfter_Stmt_values.put(_parameters, Boolean.valueOf(isLiveAfter_Stmt_value));
        return isLiveAfter_Stmt_value;
    }

    private boolean isLiveAfter_compute(Stmt stmt)  {
		for(Iterator i=stmt.following().iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(isLiveAtOrAfter(next))
				return true;
		}
		return false;
	}

    protected java.util.Map isLiveAtOrAfter_Stmt_values;
    // Declared in Liveness.jrag at line 24
    public boolean isLiveAtOrAfter(Stmt stmt) {
        Object _parameters = stmt;
if(isLiveAtOrAfter_Stmt_values == null) isLiveAtOrAfter_Stmt_values = new java.util.HashMap(4);
        if(isLiveAtOrAfter_Stmt_values.containsKey(_parameters))
            return ((Boolean)isLiveAtOrAfter_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isLiveAtOrAfter_Stmt_value = isLiveAtOrAfter_compute(stmt);
        if(isFinal && num == boundariesCrossed)
            isLiveAtOrAfter_Stmt_values.put(_parameters, Boolean.valueOf(isLiveAtOrAfter_Stmt_value));
        return isLiveAtOrAfter_Stmt_value;
    }

    private boolean isLiveAtOrAfter_compute(Stmt stmt)  {
		if(stmt.mayUse(this)) return true;
		if(stmt.mayDef(this)) return false;
		for(Iterator i=stmt.succ().iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(isLiveAtOrAfter(stmt))
				return true;
		}
		return false;
	}

    protected java.util.Map isValueParmFor_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 33
    public boolean isValueParmFor(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(isValueParmFor_Stmt_Stmt_values == null) isValueParmFor_Stmt_Stmt_values = new java.util.HashMap(4);
        if(isValueParmFor_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)isValueParmFor_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isValueParmFor_Stmt_Stmt_value = isValueParmFor_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            isValueParmFor_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(isValueParmFor_Stmt_Stmt_value));
        return isValueParmFor_Stmt_Stmt_value;
    }

    private boolean isValueParmFor_compute(Stmt begin, Stmt end)  {
		return isLiveBetween(begin, end); // && !isLiveAfter(end);
	}

    protected java.util.Map isOutParmFor_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 37
    public boolean isOutParmFor(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(isOutParmFor_Stmt_Stmt_values == null) isOutParmFor_Stmt_Stmt_values = new java.util.HashMap(4);
        if(isOutParmFor_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)isOutParmFor_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isOutParmFor_Stmt_Stmt_value = isOutParmFor_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            isOutParmFor_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(isOutParmFor_Stmt_Stmt_value));
        return isOutParmFor_Stmt_Stmt_value;
    }

    private boolean isOutParmFor_compute(Stmt begin, Stmt end)  {
		return isLiveAfter(end) && mayDefBetween(begin, end);
	}

    protected java.util.Set mayDefBetween_Stmt_Stmt_visited;
    protected java.util.Set mayDefBetween_Stmt_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set mayDefBetween_Stmt_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map mayDefBetween_Stmt_Stmt_values = new java.util.HashMap(4);
    public boolean mayDefBetween(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(mayDefBetween_Stmt_Stmt_visited == null) mayDefBetween_Stmt_Stmt_visited = new java.util.HashSet(4);
if(mayDefBetween_Stmt_Stmt_values == null) mayDefBetween_Stmt_Stmt_values = new java.util.HashMap(4);
        if(mayDefBetween_Stmt_Stmt_computed.contains(_parameters))
            return ((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
        if (!mayDefBetween_Stmt_Stmt_initialized.contains(_parameters)) {
            mayDefBetween_Stmt_Stmt_initialized.add(_parameters);
            mayDefBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            mayDefBetween_Stmt_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_mayDefBetween_Stmt_Stmt_value;
            do {
                CHANGE = false;
                new_mayDefBetween_Stmt_Stmt_value = mayDefBetween_compute(begin, end);
                if (new_mayDefBetween_Stmt_Stmt_value!=((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                mayDefBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_mayDefBetween_Stmt_Stmt_value));
            } while (CHANGE);
            mayDefBetween_Stmt_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            mayDefBetween_Stmt_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            mayDefBetween_compute(begin, end);
            RESET_CYCLE = false;
            mayDefBetween_Stmt_Stmt_computed.remove(_parameters);
            mayDefBetween_Stmt_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_mayDefBetween_Stmt_Stmt_value;
        }
        if(!mayDefBetween_Stmt_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                mayDefBetween_Stmt_Stmt_computed.remove(_parameters);
                mayDefBetween_Stmt_Stmt_initialized.remove(_parameters);
                return ((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
            }
            mayDefBetween_Stmt_Stmt_visited.add(_parameters);
            boolean new_mayDefBetween_Stmt_Stmt_value = mayDefBetween_compute(begin, end);
            if (new_mayDefBetween_Stmt_Stmt_value!=((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            mayDefBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_mayDefBetween_Stmt_Stmt_value));
            mayDefBetween_Stmt_Stmt_visited.remove(_parameters);
            return new_mayDefBetween_Stmt_Stmt_value;
        }
        return ((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean mayDefBetween_compute(Stmt begin, Stmt end)  {
		if(begin.mayDef(this)) return true;
		if(begin == end) return false;
		for(Iterator i=begin.gsucc(begin, end).iterator();i.hasNext();)
			if(mayDefBetween((Stmt)i.next(), end))
				return true;
		return false;
	}

    protected java.util.Map accessedOutside_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 50
    public boolean accessedOutside(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(accessedOutside_Stmt_Stmt_values == null) accessedOutside_Stmt_Stmt_values = new java.util.HashMap(4);
        if(accessedOutside_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)accessedOutside_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean accessedOutside_Stmt_Stmt_value = accessedOutside_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            accessedOutside_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(accessedOutside_Stmt_Stmt_value));
        return accessedOutside_Stmt_Stmt_value;
    }

    private boolean accessedOutside_compute(Stmt begin, Stmt end) {  return  
		accessedBefore(begin) || accessedAfter(end);  }

    protected java.util.Set accessedBefore_Stmt_visited;
    protected java.util.Set accessedBefore_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set accessedBefore_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map accessedBefore_Stmt_values = new java.util.HashMap(4);
    public boolean accessedBefore(Stmt stmt) {
        Object _parameters = stmt;
if(accessedBefore_Stmt_visited == null) accessedBefore_Stmt_visited = new java.util.HashSet(4);
if(accessedBefore_Stmt_values == null) accessedBefore_Stmt_values = new java.util.HashMap(4);
        if(accessedBefore_Stmt_computed.contains(_parameters))
            return ((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue();
        if (!accessedBefore_Stmt_initialized.contains(_parameters)) {
            accessedBefore_Stmt_initialized.add(_parameters);
            accessedBefore_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            accessedBefore_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_accessedBefore_Stmt_value;
            do {
                CHANGE = false;
                new_accessedBefore_Stmt_value = accessedBefore_compute(stmt);
                if (new_accessedBefore_Stmt_value!=((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                accessedBefore_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedBefore_Stmt_value));
            } while (CHANGE);
            accessedBefore_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            accessedBefore_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            accessedBefore_compute(stmt);
            RESET_CYCLE = false;
            accessedBefore_Stmt_computed.remove(_parameters);
            accessedBefore_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_accessedBefore_Stmt_value;
        }
        if(!accessedBefore_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                accessedBefore_Stmt_computed.remove(_parameters);
                accessedBefore_Stmt_initialized.remove(_parameters);
                return ((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue();
            }
            accessedBefore_Stmt_visited.add(_parameters);
            boolean new_accessedBefore_Stmt_value = accessedBefore_compute(stmt);
            if (new_accessedBefore_Stmt_value!=((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            accessedBefore_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedBefore_Stmt_value));
            accessedBefore_Stmt_visited.remove(_parameters);
            return new_accessedBefore_Stmt_value;
        }
        return ((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean accessedBefore_compute(Stmt stmt)  {
		for(Iterator i=stmt.pred().iterator();i.hasNext();) {
			Stmt before = (Stmt)i.next();
			if(!before.between(getBlock(), -1, Integer.MAX_VALUE))
				continue;
			if(before.mayAccess(this))
				return true;
			if(accessedBefore(before))
				return true;
		}
		return false;
	}

    protected java.util.Set accessedAfter_Stmt_visited;
    protected java.util.Set accessedAfter_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set accessedAfter_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map accessedAfter_Stmt_values = new java.util.HashMap(4);
    public boolean accessedAfter(Stmt stmt) {
        Object _parameters = stmt;
if(accessedAfter_Stmt_visited == null) accessedAfter_Stmt_visited = new java.util.HashSet(4);
if(accessedAfter_Stmt_values == null) accessedAfter_Stmt_values = new java.util.HashMap(4);
        if(accessedAfter_Stmt_computed.contains(_parameters))
            return ((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue();
        if (!accessedAfter_Stmt_initialized.contains(_parameters)) {
            accessedAfter_Stmt_initialized.add(_parameters);
            accessedAfter_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            accessedAfter_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_accessedAfter_Stmt_value;
            do {
                CHANGE = false;
                new_accessedAfter_Stmt_value = accessedAfter_compute(stmt);
                if (new_accessedAfter_Stmt_value!=((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                accessedAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedAfter_Stmt_value));
            } while (CHANGE);
            accessedAfter_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            accessedAfter_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            accessedAfter_compute(stmt);
            RESET_CYCLE = false;
            accessedAfter_Stmt_computed.remove(_parameters);
            accessedAfter_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_accessedAfter_Stmt_value;
        }
        if(!accessedAfter_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                accessedAfter_Stmt_computed.remove(_parameters);
                accessedAfter_Stmt_initialized.remove(_parameters);
                return ((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue();
            }
            accessedAfter_Stmt_visited.add(_parameters);
            boolean new_accessedAfter_Stmt_value = accessedAfter_compute(stmt);
            if (new_accessedAfter_Stmt_value!=((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            accessedAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedAfter_Stmt_value));
            accessedAfter_Stmt_visited.remove(_parameters);
            return new_accessedAfter_Stmt_value;
        }
        return ((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean accessedAfter_compute(Stmt stmt)  {
		for(Iterator i=stmt.succ().iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(!next.between(getBlock(), -1, Integer.MAX_VALUE))
				continue;
			if(next.mayAccess(this))
				return true;
			if(accessedAfter(next))
				return true;
		}
		return false;
	}

    protected java.util.Map shouldMoveOutOf_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 79
    public boolean shouldMoveOutOf(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(shouldMoveOutOf_Stmt_Stmt_values == null) shouldMoveOutOf_Stmt_Stmt_values = new java.util.HashMap(4);
        if(shouldMoveOutOf_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)shouldMoveOutOf_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean shouldMoveOutOf_Stmt_Stmt_value = shouldMoveOutOf_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            shouldMoveOutOf_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(shouldMoveOutOf_Stmt_Stmt_value));
        return shouldMoveOutOf_Stmt_Stmt_value;
    }

    private boolean shouldMoveOutOf_compute(Stmt begin, Stmt end) {  return 
		between(begin, end)	&& accessedAfter(end);  }

    protected java.util.Map shouldDuplicate_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 89
    public boolean shouldDuplicate(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(shouldDuplicate_Stmt_Stmt_values == null) shouldDuplicate_Stmt_Stmt_values = new java.util.HashMap(4);
        if(shouldDuplicate_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)shouldDuplicate_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean shouldDuplicate_Stmt_Stmt_value = shouldDuplicate_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            shouldDuplicate_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(shouldDuplicate_Stmt_Stmt_value));
        return shouldDuplicate_Stmt_Stmt_value;
    }

    private boolean shouldDuplicate_compute(Stmt begin, Stmt end)  {
		return shouldMoveInto(begin, end) && accessedOutside(begin, end);
	}

    // Declared in LookupVariable.jrag at line 13
    public SimpleSet lookupVariable(String name) {
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        return lookupVariable_String_value;
    }

    // Declared in NameCheck.jrag at line 276
    public VariableScope outerScope() {
        VariableScope outerScope_value = getParent().Define_VariableScope_outerScope(this, null);
        return outerScope_value;
    }

    // Declared in NameCheck.jrag at line 290
    public BodyDecl enclosingBodyDecl() {
        BodyDecl enclosingBodyDecl_value = getParent().Define_BodyDecl_enclosingBodyDecl(this, null);
        return enclosingBodyDecl_value;
    }

    // Declared in TypeAnalysis.jrag at line 578
    public TypeDecl hostType() {
        TypeDecl hostType_value = getParent().Define_TypeDecl_hostType(this, null);
        return hostType_value;
    }

    // Declared in VariableDeclaration.jrag at line 61
    public boolean isMethodParameter() {
        boolean isMethodParameter_value = getParent().Define_boolean_isMethodParameter(this, null);
        return isMethodParameter_value;
    }

    // Declared in VariableDeclaration.jrag at line 62
    public boolean isConstructorParameter() {
        boolean isConstructorParameter_value = getParent().Define_boolean_isConstructorParameter(this, null);
        return isConstructorParameter_value;
    }

    // Declared in VariableDeclaration.jrag at line 63
    public boolean isExceptionHandlerParameter() {
        boolean isExceptionHandlerParameter_value = getParent().Define_boolean_isExceptionHandlerParameter(this, null);
        return isExceptionHandlerParameter_value;
    }

    // Declared in ASTUtil.jrag at line 7
    public Program programRoot() {
        Program programRoot_value = getParent().Define_Program_programRoot(this, null);
        return programRoot_value;
    }

    // Declared in ASTUtil.jrag at line 15
    public SimpleSet lookupType(String name) {
        SimpleSet lookupType_String_value = getParent().Define_SimpleSet_lookupType(this, null, name);
        return lookupType_String_value;
    }

    protected boolean getBlock_computed = false;
    protected Block getBlock_value;
    // Declared in Domination.jrag at line 65
    public Block getBlock() {
        if(getBlock_computed)
            return getBlock_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        getBlock_value = getParent().Define_Block_getBlock(this, null);
        if(isFinal && num == boundariesCrossed)
            getBlock_computed = true;
        return getBlock_value;
    }

    // Declared in Modifiers.jrag at line 273
    public boolean Define_boolean_mayBeFinal(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeFinal(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
