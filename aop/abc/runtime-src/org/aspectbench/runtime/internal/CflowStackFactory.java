/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Damien Sereni
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

package org.aspectbench.runtime.internal;
import org.aspectbench.runtime.internal.cflowinternal.*;

/**
 * @author Damien Sereni
 */

public class CflowStackFactory {

	public static CflowStackInterface.Ref makeStackRef() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Ref();
		else
			return new CflowStackGlobal.CflowStackRef();
	}
	public static CflowStackInterface.Int makeStackInt() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Int();
		else
			return new CflowStackGlobal.CflowStackInt();
	}
	public static CflowStackInterface.Long makeStackLong() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Long();
		else
			return new CflowStackGlobal.CflowStackLong();
	}
	public static CflowStackInterface.Float makeStackFloat() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Float();
		else
			return new CflowStackGlobal.CflowStackFloat();
	}
	public static CflowStackInterface.Double makeStackDouble() {
		if (DecideThreadLocal.ok())
			return new CflowStackThreadLocal.Double();
		else
			return new CflowStackGlobal.CflowStackDouble();
	}
	
}
