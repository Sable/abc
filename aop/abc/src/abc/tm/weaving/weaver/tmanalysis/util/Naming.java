/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
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
package abc.tm.weaving.weaver.tmanalysis.util;

/**
 * Utiility methods for name conversions.
 *
 * @author Eric Bodden
 */
public class Naming {

	/**
	 * Creates a unique and fully qualified ID for a tracematch symbol based on the tracematch
	 * ID and the name of the symbol.
	 * @param tm_id an ID unique to the tracematch
	 * @param symbolName a symbol name unique for the given tracematch
	 * @return a unique, fully qualified ID
	 */
	public static String uniqueSymbolID(String tm_id, String symbolName) {
		return (tm_id + '$' + symbolName).intern();
	}

	/**
	 * Creates a unique ID for a shadow based on the parameters.
	 * @param uniqueSymbolId the unique symbol ID of a symbol
	 * @param shadowId the unique ID of the shadow in source
	 * @return
	 */
	public static String uniqueShadowID(String uniqueSymbolId, int shadowId) {
		assert uniqueSymbolId.indexOf('$')>-1;
		return (uniqueSymbolId + "@" + shadowId).intern();
	}
	
	/**
	 * Creates a unique ID for a shadow based on the parameters.
	 * @param tm_id the unique name of the tracenatch that owns this shadow
	 * @param symbolName the unique symbol for which the shadow was generated
	 * @param shadowId the unique ID of the shadow in source
	 * @return
	 */
	public static String uniqueShadowID(String tm_id, String symbolName, int shadowId) {
		return (uniqueSymbolID(tm_id, symbolName) + "@" + shadowId).intern();
	}

	/**
	 * TODO comment
	 * @param uniqueShadowId
	 * @return
	 */
	public static int getShadowIdFromUniqueShadowId(String uniqueShadowId) {
		assert uniqueShadowId.indexOf('$')>-1;
		assert uniqueShadowId.indexOf('@')>-1;
		return Integer.parseInt(uniqueShadowId.substring(uniqueShadowId.lastIndexOf('@')+1));
	}
	
	/**
	 * TODO comment
	 * @param uniqueSymbolOrShadowId
	 * @return
	 */
	public static String getTracematchName(String uniqueSymbolOrShadowId) {
		return uniqueSymbolOrShadowId.substring(0, uniqueSymbolOrShadowId.lastIndexOf('$')).intern();
	}

	/**
	 * TODO comment
	 * @param uniqueSymbolOrShadowId
	 * @return
	 */
	public static String getSymbolShortName(String uniqueSymbolOrShadowId) {
		String tail = uniqueSymbolOrShadowId.substring(uniqueSymbolOrShadowId.lastIndexOf('$') + 1);
		if(tail.indexOf('@')>-1) {
			return tail.substring(0, tail.indexOf('@')).intern();
		} else {
			return tail.intern();
		}
	}

}
