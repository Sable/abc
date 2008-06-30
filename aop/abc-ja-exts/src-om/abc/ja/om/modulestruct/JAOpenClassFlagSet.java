/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Neil Ongkingco
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

package abc.ja.om.modulestruct;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class JAOpenClassFlagSet {
	
	HashMap<OCFType, JAOpenClassFlag> flags;
	
	public JAOpenClassFlagSet() {
		this.flags = new HashMap<OCFType, JAOpenClassFlag>();
	}
	
	public void addFlag(JAOpenClassFlag flag) {
		if (flag instanceof JAOpenClassFlagField) {
			flags.put(OCFType.FIELD, flag);
		} else if (flag instanceof JAOpenClassFlagMethod) {
			flags.put(OCFType.METHOD, flag);
		} else if (flag instanceof JAOpenClassFlagParent) {
			flags.put(OCFType.PARENT, flag);
		}
	}
	
	public boolean isAllowed(OCFType type, JAOpenClassContext context) {
		JAOpenClassFlag flag = flags.get(type);
		if (flag == null) {
			return false;
		}
		return flag.isAllowed(context);
	}
	
	public static enum OCFType {
		FIELD,
		METHOD,
		PARENT
    };
	
	public String toString() {
		String ret = "";
		boolean first = true;
		for (JAOpenClassFlag flag : flags.values()) {
			if (!first) {
				ret += ", ";
			}
			ret += flag.toString();
			first = false;
		}
		return ret;
	}
	
}
