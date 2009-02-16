package abc.impact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import soot.SootClass;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.DeclareParents;
import abc.weaving.aspectinfo.GlobalAspectInfo;

public class ImpactGlobalAspectInfo extends GlobalAspectInfo {

	private HashMap<SootClass, SootClass> typeToDirectSuperclass;
	private HashMap<SootClass, List<SootClass>> typeToDirectInterfacesimpl;
	
	public void buildAspectHierarchy()
	{

		super.buildAspectHierarchy();
		
		// Store old super class and implemented interfaces information of all 
		//  types being changed parents by "decalre parents".
		{
			// System.out.println("store hierachy");
			typeToDirectSuperclass = new HashMap<SootClass, SootClass>();
			typeToDirectInterfacesimpl = new HashMap<SootClass, List<SootClass>>();
			
			for (Iterator itdDeclareParentsIt = getDeclareParents().iterator(); itdDeclareParentsIt
					.hasNext();) {
				DeclareParents itdDeclareParents = (DeclareParents) itdDeclareParentsIt
						.next();
	
				// System.out.println(itdDeclareParents);
				// foreach classes being injected parents
				for (Iterator itdClassIt = itdDeclareParents.getClasses().iterator(); itdClassIt
						.hasNext();) {
					SootClass itdClass = ((AbcClass)itdClassIt.next()).getSootClass();
					
					if (! typeToDirectSuperclass.containsKey(itdClass)) {
						typeToDirectSuperclass.put(itdClass, itdClass.getSuperclass());
					}
					
					if (! typeToDirectInterfacesimpl.containsKey(itdClass)) {
						List<SootClass> oldInterfacesList = new ArrayList<SootClass>();
						if (itdClass.getInterfaceCount() > 0) oldInterfacesList.addAll(itdClass.getInterfaces());
						typeToDirectInterfacesimpl.put(itdClass, Collections.unmodifiableList(oldInterfacesList));
					}
				}
			}
			
			// System.out.println("old supers" + typeToDirectSuperclass);
			// System.out.println("old impls" + typeToDirectInterfacesimpl);
		}
	}
	
	/**
	 * get super class of sc before "declare parents" is processed;
	 * however, situation like the following is dealt abc:
	 *    declare parents E extends C; //old super class of E is B
	 *    declare parents E extends D; //still consider old super class of E is B
	 * if strictly follow the "everyting except me rule"
	 *    declare parents E extends C; //old super class of E is D
	 *    declare parents E extends D; //old super class of E is C
	 * however, we make an exception for this situation
	 *    declare parents E extends C; //say it cause no impact
	 *    declare parents E extends D; //old super class of E is B
	 * Fortunately, it is dealt by abc:
	 * firstly, abc throws a warning regarding "E extends C";
	 * secondly, abc's aspectinfo records 
	 *     declare parents: [E] extends D;
     *     declare parents: [] extends C; //E is removed, so it caused no impact naturally
	 * @param sc
	 * @return null if old super class of sc is not stored;
	 *         or old super class of sc;
	 */
	public SootClass getOldDirSuperclassOf(SootClass sc)
	{

		return typeToDirectSuperclass.get(sc);
	}
	
	/**
	 * get implemented interfaces of sc before "declare parents" is processed
	 * @param sc
	 * @return null if old interfaces implemented by sc is not stored;
	 *         or empty list if no old interfaces is implemented by sc;
	 *         or list of (SootClass) old interfaces implemented by sc
	 */
	public List<SootClass> getOldDirInterfacesimplOf (SootClass sc)
	{
		return Collections.unmodifiableList(typeToDirectInterfacesimpl.get(sc));
	}

}
