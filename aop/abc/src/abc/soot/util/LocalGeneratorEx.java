/*
 * Created on May 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package abc.soot.util;

import java.util.Iterator;

import soot.Body;
import soot.Local;
import soot.javaToJimple.LocalGenerator;

/**
 * @author Sascha Kuzins
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LocalGeneratorEx extends LocalGenerator {
	
	/**
	 * @param b
	 */
	public LocalGeneratorEx(Body b) {
		super(b);
		this.body=b;
	}
	private Body body;

        /**
         *  Generate a local with a given type, using a suggested name.
         *  If that name is already in use, then try using name0, name1 etc
	 *  @param type The type we want
         *  @param name The suggested name
         */
	public Local generateLocal(soot.Type type, String suggestedName){
		int i=0;
		String name=suggestedName;
		while (bodyContainsLocal(name)) {
			name=suggestedName + (++i);
		}
		return createLocal(name, type);
	}

	// The following functions are copied from the base class
	 private soot.Local createLocal(String name, soot.Type sootType) {
		 if (sootType instanceof soot.CharType) {
			 sootType = soot.IntType.v();
		 }
		 soot.Local sootLocal = soot.jimple.Jimple.v().newLocal(name, sootType);
		 body.getLocals().add(sootLocal);
		 return sootLocal;
	 }
	private boolean bodyContainsLocal(String name){
		Iterator it = body.getLocals().iterator();
		while (it.hasNext()){
			if (((soot.Local)it.next()).getName().equals(name)) return true;
		}
		return false;
	}
}
