/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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

package abc.ja.tmwpopt;

import java.util.List;

import abc.da.HasDAInfo;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;

/**
 * Abc extension for the flow-sensitive optimization of tracematches.
 * This is a backend-only extension. It wires the JastAdd-based implementation
 * of tracematches in abc.ja.tm together with the optimizations in abc.tmwpopt.
 * @author Eric Bodden
 */
public class AbcExtension extends abc.ja.tm.AbcExtension implements HasDAInfo
{

	/*
	 * The extension used for whole-program optimizations. Have to make sure that it uses our GlobalAspectInfo
	 * data structure because that is where the tracematches get added by the frontend.
	 */
	protected abc.tmwpopt.AbcExtension tmwpoptExtension = new abc.tmwpopt.AbcExtension() {
		@Override
		protected GlobalAspectInfo createGlobalAspectInfo() {
			return AbcExtension.this.getGlobalAspectInfo();
		}
	};

    protected void collectVersions(StringBuffer versions)
    {
        tmwpoptExtension.collectVersions(versions);
    }
    
	/**
	 * Returns the {@link DAInfo} of abc.da.
	 */
	public DAInfo getDependentAdviceInfo() {
		return tmwpoptExtension.getDependentAdviceInfo();
	}
	
	@Override
	public Weaver createWeaver() {
		return tmwpoptExtension.createWeaver();
	}
	
    /**
     * Registers the reweaving passes of the dependent-advice abc extension.
     */
    @Override
    protected void createReweavingPasses(List<ReweavingPass> passes) {
    	super.createReweavingPasses(passes);
    	tmwpoptExtension.createReweavingPasses(passes);
    }
   
}
