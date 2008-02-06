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

package abc.da;

import java.util.Collection;
import java.util.List;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.da.weaving.aspectinfo.DAGlobalAspectInfo;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceFlowInsensitiveAnalysis;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceQuickCheck;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.ReweavingAnalysis;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * Abc extension for dependent advice.
 * @author Eric Bodden
 */
public class AbcExtension extends abc.eaj.AbcExtension
{
    protected static final ID DEPENDENT_ADVICE_QUICK_CHECK = new ReweavingPass.ID("quick-check for dependent-advice");
	protected static final ID DEPENDENT_ADVICE_FLOW_INSENSITIVE_ANALYSIS = new ReweavingPass.ID("flow-insensitive analysis for dependent-advice");
	
	protected boolean foundDependencyKeyword = false;
	protected boolean forceEnableQuickCheck = false;
	protected boolean forceEnableFlowInsensitiveOptimizations = false;

	protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with DA " +
                        new abc.da.Version().toString() +
                        "\n");
    }

    public ExtensionInfo makeExtensionInfo(Collection jar_classes, Collection aspect_sources) {
        return new ExtensionInfo(jar_classes, aspect_sources);
    }

    /* (non-Javadoc)
     * @see abc.main.AbcExtension#initLexerKeywords(abc.aspectj.parse.AbcLexer)
     */
    public void initLexerKeywords(AbcLexer lexer) {
        // Add the base keywords
        super.initLexerKeywords(lexer);

        lexer.addAspectJKeyword("dependency", new LexerAction_c(abc.da.parse.sym.DEPENDENCY) {
        	@Override
        	public int getToken(AbcLexer lexer) {
        		//inform extension that this keyword was read
        		foundDependencyKeyword = true;
        		return super.getToken(lexer);
        	}
        });
        lexer.addAspectJKeyword("dependent", new LexerAction_c(abc.da.parse.sym.DEPENDENT) {
        	@Override
        	public int getToken(AbcLexer lexer) {
        		//inform extension that this keyword was read
        		foundDependencyKeyword = true;
        		return super.getToken(lexer);
        	}
        });
        lexer.addAspectJKeyword("strong", new LexerAction_c(abc.da.parse.sym.STRONG,lexer.pointcut_state()));
        lexer.addAspectJKeyword("weak", new LexerAction_c(abc.da.parse.sym.WEAK,lexer.pointcut_state()));
    }
    protected void createReweavingPasses(List passes) {
    	super.createReweavingPasses(passes);
    	
		passes.add(new ReweavingPass(DEPENDENT_ADVICE_QUICK_CHECK,createQuickCheck()));
		passes.add(new ReweavingPass(DEPENDENT_ADVICE_FLOW_INSENSITIVE_ANALYSIS,createFlowInsensitiveAnalysis()));
    }
    
	protected ReweavingAnalysis createQuickCheck() {
		return new DependentAdviceQuickCheck();
	}

	protected ReweavingAnalysis createFlowInsensitiveAnalysis() {
		return new DependentAdviceFlowInsensitiveAnalysis();
	}

	@Override
    protected GlobalAspectInfo createGlobalAspectInfo() {
    	return new DAGlobalAspectInfo();
    }

	public boolean foundDependencyKeyword() {
		return foundDependencyKeyword;
	}

	public boolean forceEnableQuickCheck() {
		return forceEnableQuickCheck;
	}

	public boolean forceEnableFlowInsensitiveOptimizations() {
		return forceEnableFlowInsensitiveOptimizations;
	}
}
