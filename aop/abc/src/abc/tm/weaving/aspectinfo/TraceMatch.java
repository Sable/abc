/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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

package abc.tm.weaving.aspectinfo;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

import abc.weaving.aspectinfo.*;
import abc.soot.util.UnUsedParams;

import abc.tm.weaving.matching.*;
import abc.tm.weaving.weaver.*;

import soot.*;

import java.util.*;

/** 
 * Represents a TraceMatch.
 *
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 */
public class TraceMatch
{
    protected String name;
    protected boolean per_thread;

    protected List formals;
    protected Map formal_name_to_type;
    protected List new_advice_body_formals;
    protected Map param_name_to_body_pos = null;
    protected int[] formal_pos_to_body_pos = new int[0];
    protected int non_around_formals;

    protected StateMachine state_machine;

    protected Map sym_to_vars;
    protected Map sym_to_advice_name;

    protected String synch_advice_name;
    protected String some_advice_name;
    protected String dummy_proceed_name;

    protected Aspect container;
 
    protected SootClass constraint = null;
    protected SootClass disjunct = null;
    protected SootClass labels = null;
    protected SootClass labels_thread_local = null;
    protected SootClass tm_weak_ref;
    protected Map primitive_to_box = null;
 
    protected CodeGenHelper helper;
    protected Position position;

    protected Collection unused_formals = null;

    public TraceMatch(String name, List formals, List new_advice_body_formals,
                        StateMachine state_machine, boolean per_thread,
                        Map sym_to_vars, Map sym_to_advice_name,
                        String synch_advice_name, String some_advice_name,
                        String dummy_proceed_name, Aspect container,
                        Position pos)
    {
        this.name = name;
        this.per_thread = per_thread;

        this.formals = formals;
        this.formal_name_to_type = new HashMap();
        this.new_advice_body_formals = new_advice_body_formals;

        this.state_machine = state_machine;
        this.sym_to_vars = sym_to_vars;

        this.sym_to_advice_name = sym_to_advice_name;
        this.synch_advice_name = synch_advice_name;
        this.some_advice_name = some_advice_name;
        this.dummy_proceed_name = dummy_proceed_name;

        this.container = container;
        this.position = pos;

        makeFormalMaps();
        makePrimitiveMaps();

        this.helper = new CodeGenHelper(this);
    }

    public boolean isPerThread()
    {
        return per_thread;
    }

    public boolean isAround()
    {
        return dummy_proceed_name != null;
    }

	public Position getPosition() {
		return position;
	}
	
    public SootClass getContainerClass() {
        return container.getInstanceClass().getSootClass();
    }

    public List getFormals() {
        return formals;
    }
    
    public List getFormalNames()
    {
        List formalNames = new LinkedList();
        for (Iterator formalIter = formals.iterator(); formalIter.hasNext(); )
        {
            Formal f = (Formal) formalIter.next();
            formalNames.add(f.getName());
        }
        return formalNames;
    }

    public List getNewAdviceBodyFormals()
    {
        return new_advice_body_formals;
    }

    protected void makeFormalMaps()
    {
        param_name_to_body_pos = new HashMap();

        for (int i = 0; i < new_advice_body_formals.size(); i++)
        {
            Formal f = (Formal) new_advice_body_formals.get(i);

            param_name_to_body_pos.put(f.getName(), new Integer(i));
        }

        Set thisjp_vars = new HashSet(param_name_to_body_pos.keySet());

        formal_pos_to_body_pos = new int[formals.size() + 3];

        for (int i = 0; i < formals.size(); i++) {
            Formal f = (Formal) formals.get(i);

            thisjp_vars.remove(f.getName());

            formal_name_to_type.put(f.getName(), f.getType().getSootType());
            formal_pos_to_body_pos[i] = getBodyParameterIndex(f.getName());
        }

        int offset = new_advice_body_formals.size() - thisjp_vars.size();

        for (int i = 0; i < 3; i++)
            formal_pos_to_body_pos[i + formals.size()] = i + offset;

        non_around_formals = formals.size() - offset;
    }

    public int getBodyParameterIndex(String name)
    {
        if (!param_name_to_body_pos.containsKey(name))
            return -1;

        return ((Integer) param_name_to_body_pos.get(name)).intValue();
    }

    public int getBodyParameterIndex(int formal_pos)
    {
        return formal_pos_to_body_pos[formal_pos];
    }

    public int nonAroundFormals()
    {
        return non_around_formals;
    }

    public StateMachine getState_machine() {
        return state_machine;
    }

    public Set getSymbols() {
        return sym_to_vars.keySet();
    }

    public List getVariableOrder(String symbol)
    {
        return (List) sym_to_vars.get(symbol);
    }

    public String getName() {
        return name;
    }

    /**
     * Get the name of the package that contains the aspect which
     * contains this tracematch.
     *
     * (includes the trailing full-stop)
     */
    public String getPackage()
    {
        String jvm_name = container.getInstanceClass().getJvmName();

        int package_length = jvm_name.lastIndexOf('.') + 1;

        return jvm_name.substring(0, package_length);
    }

    public SootMethod getBodyMethod()
    {
        String body_name = getName() + "$body";

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(body_name);

        return sm;
    }

    public SootMethod getRealBodyMethod()
    {
        String body_name = getName() + "$body_real";

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(body_name);

        return sm;
    }

    public SootMethod getSymbolAdviceMethod(String symbol)
    {
        String advice_name = (String) sym_to_advice_name.get(symbol);

        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(advice_name);

        return sm;
    }

    public SootMethod getSynchAdviceMethod()
    {
        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(synch_advice_name);

        return sm;
    }

    public SootMethod getSomeAdviceMethod()
    {
        SootClass sc = container.getInstanceClass().getSootClass();
        SootMethod sm = sc.getMethodByName(some_advice_name);

        return sm;
    }

    public String getDummyProceedName()
    {
        return dummy_proceed_name;
    }

    public void setConstraintClass(SootClass constraint)
    {
        this.constraint = constraint;
        helper.setConstraintClass(constraint);
    }

    public SootClass getConstraintClass()
    {
        return constraint;
    }

    public void setDisjunctClass(SootClass disjunct)
    {
        this.disjunct = disjunct;
        helper.setDisjunctClass(disjunct);
    }

    public SootClass getDisjunctClass()
    {
        return disjunct;
    }

    public void setLabelsClass(SootClass labels)
    {
        this.labels = labels;
    }

    public SootClass getLabelsClass()
    {
        if (isPerThread())
            return labels;
        else
            return getContainerClass();
    }

    public void setLabelsThreadLocalClass(SootClass labels_thread_local)
    {
        this.labels_thread_local = labels_thread_local;
    }

    public SootClass getLabelsThreadLocalClass()
    {
        return labels_thread_local;
    }

    public CodeGenHelper getCodeGenHelper()
    {
        return helper;
    }

    public void findUnusedFormals()
    {
        unused_formals =
            UnUsedParams.unusedFormals(getBodyMethod(), getFormalNames());
    }

    public Collection getUnusedFormals()
    {
        return unused_formals;
    }

    public boolean isPrimitive(String name)
    {
        Type type = bindingType(name);
        
        return (type instanceof RefLikeType);
    }

    public Type bindingType(String name)
    {
        return (Type) formal_name_to_type.get(name);
    }

    public SootClass weakBindingClass(String name)
    {
        Type type = bindingType(name);

        if (type instanceof RefLikeType)
            return tm_weak_ref;
        else
            return (SootClass) primitive_to_box.get(type);
    }

    public Type weakBindingConstructorArgType(String name)
    {
        Type type = bindingType(name);

        if (type instanceof RefLikeType)
            return Scene.v().getRefType("java.lang.Object");
        else
            return type;
    }

    protected void makePrimitiveMaps()
    {
        primitive_to_box = new HashMap();

        primitive_to_box.put(BooleanType.v(),
                             Scene.v().getSootClass("java.lang.Boolean"));
        primitive_to_box.put(ByteType.v(),
                             Scene.v().getSootClass("java.lang.Byte"));
        primitive_to_box.put(CharType.v(),
                             Scene.v().getSootClass("java.lang.Character"));
        primitive_to_box.put(DoubleType.v(),
                             Scene.v().getSootClass("java.lang.Double"));
        primitive_to_box.put(FloatType.v(),
                             Scene.v().getSootClass("java.lang.Float"));
        primitive_to_box.put(IntType.v(),
                             Scene.v().getSootClass("java.lang.Integer"));
        primitive_to_box.put(LongType.v(),
                             Scene.v().getSootClass("java.lang.Long"));
        primitive_to_box.put(ShortType.v(),
                             Scene.v().getSootClass("java.lang.Short"));


        tm_weak_ref = Scene.v().getSootClass(
                        "org.aspectbench.tm.runtime.internal.MyWeakRef");
    }

    protected SootMethod getConstructor(SootClass constructed, Type param)
    {
        String init_name = SootMethod.constructorName;
        List type_list = new LinkedList();
        type_list.add(param);

        return constructed.getMethod(init_name, type_list);
    }
}
