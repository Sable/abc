package abc.ja.jpi.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import polyglot.util.Position;

import abc.ja.jpi.jrag.ASTNode;
import abc.ja.jpi.jrag.Access;
import abc.ja.jpi.jrag.ParameterDeclaration;
import abc.ja.jpi.jrag.TypeDecl;
import abc.ja.jpi.jrag.CJPAdviceDecl;
import abc.ja.jpi.jrag.ExhibitBodyDecl;
import abc.ja.jpi.jrag.ExplicitTypeNamePattern;
import abc.ja.jpi.jrag.JPITypeDecl;
import abc.ja.jpi.jrag.TypeAccess;
import abc.ja.jpi.jrag.VarAccess;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.ClassnamePattern;
import abc.weaving.aspectinfo.EmptyPointcut;
import abc.weaving.aspectinfo.Formal;
import abc.weaving.aspectinfo.LocalPointcutVars;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Var;
import abc.weaving.aspectinfo.Within;

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
		Collection<ExhibitBodyDecl> exhibitDecls = collectExhibitDecls(currentAdvice, exhibitsDecls);
		Pointcut pointcut = null;
		for (ExhibitBodyDecl exhibitDecl : exhibitDecls) {
			if(pointcut == null){
				pointcut = extractAndTransformPointcuts(currentAdvice,exhibitDecl, overriden);
			}
			else{
				pointcut = OrPointcut.construct(pointcut, extractAndTransformPointcuts(currentAdvice,exhibitDecl, overriden), exhibitDecl.pos());
			}
		}
		return pointcut;
	}
	

	
	/***
	 * Collect all the exhibits definitions that are defined for the jpi contained
	 * in currentAdvice and for its subtypes.
	 * @param currentAdvice
	 * @param exhibitsDecls
	 * @return
	 */
	public static Collection<ExhibitBodyDecl> collectExhibitDecls(CJPAdviceDecl currentAdvice, Collection<ExhibitBodyDecl> exhibitsDecls){
		JPITypeDecl jpiType, jpiTypeTemp;
		jpiType = (JPITypeDecl)currentAdvice.getName().type();
		HashSet<ExhibitBodyDecl> set = new HashSet<ExhibitBodyDecl>();
		for(ExhibitBodyDecl exhibitDecl : exhibitsDecls){
			jpiTypeTemp = (JPITypeDecl)exhibitDecl.getJPIName().type();
			if (jpiTypeTemp.isSubType(jpiType) || jpiTypeTemp.equals(jpiType)){
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
			for(int i=0; i< currentAdvice.getAdviceSpec().getNumParameter(); i++){
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
		return exhibitDecl.getPointcut().pointcut();
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
		  hostAccess.setParent(pattern);
		  pattern.setParent(parentNode);	  	  
		  return pattern.classnamePattern();
	  }
	
	
	
}
