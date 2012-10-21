package abc.ja.jpi.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import polyglot.util.Position;

import abc.ja.jpi.jrag.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.aspectinfo.MethodPattern;

public class PointcutCombination {
	
	/***
	 * This method implement the polymorphism semantics part
	 * @param currentAdvice
	 * @param exhibitsDecls
	 * @return
	 */
	public static Pointcut combinePointcutsPolymorphism(CJPAdviceDecl currentAdvice, Collection<ExhibitBodyDecl> exhibitsDecls){
		return combinePointcuts(currentAdvice, exhibitsDecls, false);
	}

	/***
	 * This method implement the overriden semantics part
	 * @param currentAdvice
	 * @param exhibitsDecls
	 * @return
	 */
	public static Pointcut combinePointcutsOverriden(CJPAdviceDecl currentAdvice, Collection<CJPAdviceDecl> CJPAdviceDecls,Collection<ExhibitBodyDecl> exhibitsDecls){
		Pointcut pointcut = null;
		Pointcut tempPointcut = null;
		if (currentAdvice.isFinal()){
			return new EmptyPointcut(new Position("", -1));
		}
		for (CJPAdviceDecl cjpAdvice : CJPAdviceDecls) {
			if(cjpAdvice.isSubType(currentAdvice)){
				if(pointcut == null){
					pointcut = combinePointcuts(cjpAdvice, exhibitsDecls, true);
				}
				else{
					tempPointcut = combinePointcuts(cjpAdvice, exhibitsDecls, true);
					pointcut = OrPointcut.construct(pointcut, tempPointcut, tempPointcut.getPosition());
				}
			}
		}		
		return pointcut!=null ? pointcut : new EmptyPointcut(new Position("", -1));
	}

	/***
	 * The method that do the job to combine the pointcuts accordingly to the
	 * polymorphism and override semantics.
	 * @param currentAdvice
	 * @param exhibitsDecls
	 * @param overriden
	 * @return
	 */
	public static Pointcut combinePointcuts(CJPAdviceDecl currentAdvice, Collection<ExhibitBodyDecl> exhibitsDecls, boolean overriden){
		JPITypeDecl jpiType = (JPITypeDecl)((JPITypeAccess)currentAdvice.getName()).decl(currentAdvice.getAdviceSpec().getParameterTypeList());		
		Collection<ExhibitBodyDecl> exhibitDecls = collectExhibitDecls(jpiType, exhibitsDecls);
		Pointcut pointcut = null;
		for (ExhibitBodyDecl exhibitDecl : exhibitDecls) {
			if(pointcut == null){
				pointcut = extractAndTransformPointcuts(currentAdvice,exhibitDecl, overriden);
			}
			else{
				pointcut = OrPointcut.construct(pointcut, extractAndTransformPointcuts(currentAdvice,exhibitDecl, overriden), exhibitDecl.pos());
			}
		}
		return pointcut!=null ? pointcut : new EmptyPointcut(new Position("", -1));
	}
	
	public static Pointcut makeWithinPointcut(Collection<ClassDecl> allSealedClasses) {
		Pointcut pointcut = null;
		for (ClassDecl klass :  allSealedClasses) {
			if (pointcut == null) {
				pointcut = new Within(getPattern(klass, klass.getParent()), klass.pos());
			} else {
				pointcut = OrPointcut.construct(pointcut, new Within(getPattern(klass, klass.getParent()), klass.pos()), klass.pos());
			}
		}
		return pointcut!=null ? pointcut : new EmptyPointcut(new Position("", -1));
	}
	
	
	public static Pointcut makeScope(JPITypeDecl jpiType, Collection<ExhibitBodyDecl> exhibitsDecls, boolean overriden){
		Collection<ExhibitBodyDecl> exhibitDecls = collectExhibitDecls(jpiType, exhibitsDecls);
		Pointcut pointcut=null;
		for (ExhibitBodyDecl exhibitDecl : exhibitDecls) {			
			if(pointcut == null){
				pointcut = new Within(getPattern(exhibitDecl.getHostType(), exhibitDecl.getParent()), exhibitDecl.getPointcut().pos());
			}
			else{
				pointcut = OrPointcut.construct(pointcut, new Within(getPattern(exhibitDecl.getHostType(), exhibitDecl.getParent()), exhibitDecl.getPointcut().pos()), exhibitDecl.getPointcut().pos());
			}
		}
		return pointcut!=null ? pointcut : new EmptyPointcut(new Position("", -1));
	}
	
	

	
	/***
	 * Collect all the exhibits definitions related with the given jpi type declaration.
	 * @param jpiType
	 * @param exhibitsDecls
	 * @return
	 */
	public static Collection<ExhibitBodyDecl> collectExhibitDecls(JPITypeDecl jpiType, Collection<ExhibitBodyDecl> exhibitsDecls){
		JPITypeDecl jpiTypeTemp;
		HashSet<ExhibitBodyDecl> set = new HashSet<ExhibitBodyDecl>();
		for(ExhibitBodyDecl exhibitDecl : exhibitsDecls){
			jpiTypeTemp = (JPITypeDecl)((JPITypeAccess)exhibitDecl.getJPIName()).decls(exhibitDecl.getParameterTypeList());
			if (jpiTypeTemp.isSubType(jpiType) || (jpiTypeTemp == jpiType)){
				set.add(exhibitDecl);
			}
		}
		return set;
	}
	
	/***
	 * The purpose for this method is first extract the pointcut expression
	 * from the exhibit declaration and then modify properly with the syntax
	 * of jpi inheritance and with the name of the current advice declaration.
	 * @param currentAdvice
	 * @param exhibitDecl
	 * @return
	 */
	public static Pointcut extractAndTransformPointcuts(CJPAdviceDecl currentAdvice, ExhibitBodyDecl exhibitDecl, boolean overriden){
		Pointcut pointcut = transformBindingsToLocalVariables(extractPointcuts(exhibitDecl), exhibitDecl, currentAdvice, overriden);
		return reducePointcutScope(pointcut, exhibitDecl);
	}
	
	private static Pointcut reducePointcutScope(Pointcut pointcut, ExhibitBodyDecl exhibitDecl) {
		Position pos = exhibitDecl.pos();
		Pointcut within = new Within(getPattern(exhibitDecl.getHostType(), exhibitDecl.getParent()), pos);
		return AndPointcut.construct(pointcut, within, pos);		
	}

	/***
	 * This method apply the correct semantics to convert the bindings into local variables
	 * @param pointcut
	 * @param currentAdvice 
	 * @param overriden 
	 * @return
	 */
	private static Pointcut transformBindingsToLocalVariables(Pointcut pointcut, ExhibitBodyDecl exhibitDecl, CJPAdviceDecl currentAdvice, boolean overriden) {
		if(overriden){
		  return transformBindingsToLocalVariablesOverriden(pointcut, exhibitDecl);
		}
		return transformBindingsToLocalVariablesPolymorphism(pointcut, exhibitDecl, currentAdvice);  
	}
	
	/***
	 * This method convert the bindings that not are present in the current jpi declaration
	 * in local variables.  The bindings that matches with the current jpi declaration are
	 * renamed accordingly that they appears in the advice declaration.
	 * @param pointcut
	 * @param exhibitDecl
	 * @param currentAdvice 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Pointcut transformBindingsToLocalVariablesPolymorphism(Pointcut pointcut, ExhibitBodyDecl exhibitDecl, CJPAdviceDecl currentAdvice) {
		JPITypeDecl jpiDecl = (JPITypeDecl)exhibitDecl.getJPIName().type();
		JPIParameterPositionHashSet set = new JPIParameterPositionHashSet();
		Hashtable/*<String,Var>*/ renameEnv=new Hashtable();
		Hashtable/*<String,Abctype>*/ typeEnv=new Hashtable();
		JPIParameterPosition tempPosition;		
		if(jpiDecl.equals(currentAdvice.getName().type())){
			//for(int i=0; i< currentAdvice.getAdviceSpec().getNumParameter(); i++){
			//BugFix: it's not possible iterate over adviceSpec because
			//the system add arguments related with the static part
			for(int i=0; i< exhibitDecl.getNumParameter(); i++){
				Formal oldVar = exhibitDecl.getParameter(i).formal();
				renameEnv.put(oldVar.getName(),new Var(currentAdvice.getAdviceSpec().getParameter(i).name(), oldVar.getPosition()));
				typeEnv.put(oldVar.getName(), oldVar.getType());				
			}
			return pointcut.inline(renameEnv, typeEnv, null, 0);
		}
		
		//collect the arguments position in the current exhibit's jpi
		for(int i=0; i<jpiDecl.getNumSuperArgumentName(); i++){
			for(int j=0; j<jpiDecl.getNumParameter();j++){
				ParameterDeclaration pd = jpiDecl.getParameter(j);
				if (pd.getID().equals(((VarAccess)jpiDecl.getSuperArgumentName(i)).getID())){
					set.add(new JPIParameterPosition(j, i, pd));
				}
			}
		}

		for(int i=0; i< currentAdvice.getAdviceSpec().getNumParameter(); i++){
			tempPosition = set.getByParentPosition(i);
			Formal oldVar = exhibitDecl.getParameter(tempPosition.getPosition()).formal();
			renameEnv.put(oldVar.getName(),new Var(currentAdvice.getAdviceSpec().getParameter(tempPosition.getParentPosition()).name(), oldVar.getPosition()));
			typeEnv.put(oldVar.getName(), oldVar.getType());				
		}

		LinkedList formals = new LinkedList();
		for(int i=0; i<exhibitDecl.getNumParameter(); i++){
			if(!set.contains(i)){
				formals.add((Formal)exhibitDecl.getParameter(i).formal());				
			}
			else{
				tempPosition = set.getByPosition(i);
				if(!tempPosition.getAccessed()){
					formals.add((Formal)exhibitDecl.getParameter(i).formal());									
				}
			}
		}		
		pointcut = pointcut.inline(renameEnv, typeEnv, null, 0);
		return formals.isEmpty() ? pointcut : new LocalPointcutVars(pointcut,formals,pointcut.getPosition());
	}

	/***
	 * This method convert all the bindings present in the pointcut into local variables
	 * args(a) --> args(A)
	 * @param pointcut
	 * @param exhibitDecl
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })	
	private static Pointcut transformBindingsToLocalVariablesOverriden(Pointcut pointcut, ExhibitBodyDecl exhibitDecl){
		LinkedList formals = new LinkedList();
		for(ParameterDeclaration p : exhibitDecl.getParameterList()){
			formals.add(p.formal());
		}
		return new LocalPointcutVars(pointcut,formals,pointcut.getPosition());
	}

	/***
	 * This method add the within pointcut to the original pointcut expression
	 * @param exhibitDecl
	 * @return
	 */
	public static Pointcut extractPointcuts(ExhibitBodyDecl exhibitDecl){
		if (exhibitDecl instanceof GenericExhibitBodyDecl){
			replaceTypeVariableForTypeBound(exhibitDecl.getPointcut(), exhibitDecl);
		}
		return exhibitDecl.getPointcut().pointcut();
	}
	
	
	public static void replaceTypeVariableForTypeBound(ASTNode<ASTNode> node, ExhibitBodyDecl exhibitDecl){
		if (node instanceof SimpleNamePattern){
			SimpleNamePattern pattern = (SimpleNamePattern)node;
			SimpleSet set = ((GenericExhibitBodyDecl)exhibitDecl).localLookupType(pattern.getPattern());
			if (!set.isEmpty()){
				TypeAccess access = (TypeAccess)((TypeVariable)set.iterator().next()).getTypeBound(0);
				int index = pattern.getParent().getIndexOfChild(pattern);
				SubtypeNamePattern stnp = new SubtypeNamePattern(new ExplicitTypeNamePattern(access));
				ASTNode parent = pattern.getParent();
				parent.setChild(stnp, index);
			}			
		}
		for(int i=0; i<node.getNumChild(); i++){
			replaceTypeVariableForTypeBound(node.getChild(i), exhibitDecl);
		}
	}
	
	/***
	 * Helper to construct a correct ClassNamePattern.  This is useful to construct
	 * the within pointcut.
	 * @param hostType
	 * @param parentNode
	 * @return
	 */
	private static ClassnamePattern getPattern(TypeDecl hostType, ASTNode parentNode) {
		  ExplicitTypeNamePattern pattern;
		  String name = hostType.getID();
		  String packageName = "";
		  //FIXME: what happen with nested classes?
		  String[] path = hostType.fullName().split("\\.");
		  for(int i=0;i<path.length-1;i++){
			  if (packageName==""){
				  packageName = path[i];
			  }
			  else{
				  packageName = packageName + "." + path[i];
			  }
		  }
		  TypeAccess hostAccess = new TypeAccess(packageName,name);
		  pattern = new ExplicitTypeNamePattern(hostAccess);
		  new ExplicitTypeNamePattern(new Dot());
		  hostAccess.setParent(pattern);
		  pattern.setParent(parentNode);	  	  
		  return pattern.classnamePattern();
	  }
	
	
	public static Pointcut synthesizedPointcutsFromJPIWithoutAdviceDecl(Collection<ExhibitBodyDecl> exhibitBodyDeclarations, List<ParameterDeclaration> adviceParameterList, Position pos) {
		Iterator<ExhibitBodyDecl> exhibitIterator = exhibitBodyDeclarations.iterator();
		if (exhibitBodyDeclarations.size() == 0){
			return new EmptyPointcut(pos);
		}
		return runSynthesizedPointcutsFromJPIWithoutAdviceDecl(exhibitIterator,exhibitBodyDeclarations.size(),adviceParameterList,pos);
	}

	private static Pointcut runSynthesizedPointcutsFromJPIWithoutAdviceDecl(Iterator<ExhibitBodyDecl> exhibitIterator, int size, List<ParameterDeclaration> adviceParameterList, Position pos){
		if (size == 1){
			return makeExhibitBodyDeclPointcut(exhibitIterator, adviceParameterList, pos);
	  	}
	  	if (size == 2){
		  	Pointcut left = makeExhibitBodyDeclPointcut(exhibitIterator, adviceParameterList, pos);
		 	Pointcut right = makeExhibitBodyDeclPointcut(exhibitIterator, adviceParameterList, pos);
		  	return OrPointcut.construct(left,right,pos);
	  	}
	  	Pointcut parent = makeExhibitBodyDeclPointcut(exhibitIterator, adviceParameterList, pos);
	  	size = size - 1;
	  	return OrPointcut.construct(parent, runSynthesizedPointcutsFromJPIWithoutAdviceDecl(exhibitIterator,size,adviceParameterList,pos), pos);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Pointcut makeExhibitBodyDeclPointcut(Iterator<ExhibitBodyDecl> exhibitIterator, List<ParameterDeclaration> adviceParameterList, Position pos) {
	  	Pointcut currentPointcut;
	  	ExhibitBodyDecl tempExhibitBodyDecl = exhibitIterator.next();
	  
	  	Hashtable/*<String,Var>*/ renameEnv=new Hashtable();
	  	Hashtable/*<String,Abctype>*/ typeEnv=new Hashtable();
	  
	  	for(int i=0; i<adviceParameterList.getNumChild(); i++){
			Formal oldVar = tempExhibitBodyDecl.getParameter(i).formal();
		  	renameEnv.put(oldVar.getName(),new Var(adviceParameterList.getChild(i).name(), oldVar.getPosition()));
		  	typeEnv.put(oldVar.getName(), oldVar.getType());
	  	}
	  	currentPointcut = tempExhibitBodyDecl.getPointcut().pointcut();
	  	currentPointcut = currentPointcut.inline(renameEnv, typeEnv, null, 0);
	  	return AndPointcut.construct(currentPointcut, 
			  					   new Within(getPattern(tempExhibitBodyDecl.getHostType(), tempExhibitBodyDecl.getParent()), pos), pos);
	}

	
	public static void replaceTypeVariableForTypeBound(ASTNode<ASTNode> node, JPITypeDecl jpiDecl){
		if (node instanceof SimpleNamePattern){
			SimpleNamePattern pattern = (SimpleNamePattern)node;
			SimpleSet set = ((GenericGlobalJPITypeDecl)jpiDecl).localLookupType(pattern.getPattern());
			if (!set.isEmpty()){
				TypeAccess access = (TypeAccess)((TypeVariable)set.iterator().next()).getTypeBound(0);
				int index = pattern.getParent().getIndexOfChild(pattern);
				SubtypeNamePattern stnp = new SubtypeNamePattern(new ExplicitTypeNamePattern(access));
				ASTNode parent = pattern.getParent();
				parent.setChild(stnp, index);
			}			
		}
		for(int i=0; i<node.getNumChild(); i++){
			replaceTypeVariableForTypeBound(node.getChild(i), jpiDecl);
		}
	}
	
}
