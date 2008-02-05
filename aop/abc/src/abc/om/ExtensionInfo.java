/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

/*
 * Created on May 13, 2005
 *
 */
package abc.om;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.NodeFactory;
import polyglot.frontend.AbstractPass;
import polyglot.frontend.CupParser;
import polyglot.frontend.FileSource;
import polyglot.frontend.GlobalBarrierPass;
import polyglot.frontend.Job;
import polyglot.frontend.Parser;
import polyglot.frontend.Pass;
import polyglot.frontend.VisitorPass;
import polyglot.lex.Lexer;
import polyglot.types.TypeSystem;
import polyglot.util.ErrorQueue;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.visit.AspectMethods;
import abc.aspectj.visit.AspectReflectionInspect;
import abc.aspectj.visit.AspectReflectionRewrite;
import abc.aspectj.visit.CleanAspectMembers;
import abc.om.ast.OpenModNodeFactory;
import abc.om.ast.OpenModNodeFactory_c;
import abc.om.parse.Grm;
import abc.om.parse.Lexer_c;
import abc.om.visit.CheckDeclareParents;
import abc.om.visit.CheckDuplicateClassInclude;
import abc.om.visit.CheckITDs;
import abc.om.visit.CheckModuleCycles;
import abc.om.visit.CheckModuleMembers;
import abc.om.visit.CheckModuleSigMembers;
import abc.om.visit.CollectModuleAspects;
import abc.om.visit.CollectModuleOpenClassMembers;
import abc.om.visit.CollectModules;
import abc.om.visit.ModuleStructure;
import abc.om.visit.NormalizeOpenClassMembers;
import abc.om.visit.OMComputeModulePrecedence;
import abc.om.visit.OMComputePrecedence;
import abc.om.visit.PrintVisitor;

/**
 * @author Neil Ongkingco
 *  
 */
public class ExtensionInfo extends abc.eaj.ExtensionInfo {
    private AJTypeSystem ts = null;
    private AbcExtension extension = null;
    
    public static final Pass.ID PRINT_OPENMOD_NODES = new Pass.ID(
            "print-openmod-nodes");

    public static final Pass.ID PRINTED_OPENMOD_NODES = new Pass.ID(
            "printed-openmod-nodes");

    public static final Pass.ID BEFORE_MODULE_COLLECT = new Pass.ID(
            "before-openmod-collect");

    public static final Pass.ID MODULE_COLLECT = new Pass.ID("openmod-collect");

    public static final Pass.ID AFTER_MODULE_COLLECT = 
        new Pass.ID("after-openmod-collect");

    public static final Pass.ID CHECK_MODULE_MEMBERS = 
        new Pass.ID("check-openmod-members");

    public static final Pass.ID CHECKED_MODULE_MEMBERS = new Pass.ID(
            "after-check-openmod-members");
    
    public static final Pass.ID CHECK_MODULE_SIG_MEMBERS = 
        new Pass.ID("check-openmod-sig-members");
    
    public static final Pass.ID CHECKED_MODULE_SIG_MEMBERS = 
        new Pass.ID("after-check-openmod-sig-members");

    public static final Pass.ID CHECK_MODULE_CYCLES = new Pass.ID(
            "check-openmod-cycles");

    public static final Pass.ID CHECKED_MODULE_CYCLES = new Pass.ID(
            "after-check-openmod-cycles");

    public static final Pass.ID CHECK_DUPLICATE_CLASS_INCLUDE = new Pass.ID(
            "check-duplicate-class-include");

    public static final Pass.ID AFTER_CHECK_DUPLICATE_CLASS_INCLUDE = new Pass.ID(
            "after-check-duplicate-class-include");

    public static final Pass.ID OM_COMPUTE_PRECEDENCE = new Pass.ID(
            "om-compute-precedence");

    public static final Pass.ID AFTER_OM_COMPUTE_PRECEDENCE = new Pass.ID(
            "after-om-compute-precedence");
    
    public static final Pass.ID OM_COMPUTE_MODULE_PRECEDENCE = new Pass.ID(
    		"om-compute-module-precedence");
    
    public static final Pass.ID AFTER_OM_COMPUTE_MODULE_PRECEDENCE = new Pass.ID(
    		"after-om-compute-module-precedence");
    
    public static final Pass.ID INIT_DUMMY_ASPECT = 
        new Pass.ID("init_dummy_aspect");
    
    public static final Pass.ID COLLECT_MODULE_ASPECTS = 
        new Pass.ID("collect_module_aspects");
    public static final Pass.ID AFTER_COLLECT_MODULE_ASPECTS = 
        new Pass.ID("after_collect_module_aspects");
    
    public static final Pass.ID COLLECT_OPEN_CLASS_MEMBERS = 
        new Pass.ID("collect_open_class_members");
    public static final Pass.ID AFTER_COLLECT_OPEN_CLASS_MEMBERS = 
        new Pass.ID("after_collect_open_class_members");

    public static final Pass.ID CHECK_DECLARE_PARENTS =
        new Pass.ID("check_declare_parents");
    public static final Pass.ID AFTER_CHECK_DECLARE_PARENTS =
        new Pass.ID("after_check_declare_parents");
    
    public static final Pass.ID CHECK_ITD = 
        new Pass.ID("check_itd");
    public static final Pass.ID AFTER_CHECK_ITD =
        new Pass.ID("after_check_itd");
    
    public static final Pass.ID NORMALIZE_OPEN_CLASS_MEMBERS =
        new Pass.ID("normalize_open_class_members");
    
    /* Module globals */
    public ModuleStructure moduleStruct;

    public ExtensionInfo(Collection jar_classes, Collection source_files, AbcExtension extension) {
        super(jar_classes, source_files);
        moduleStruct = new ModuleStructure(this);
        this.extension = extension;
    }

    public AbcExtension getAbcExtension() {
        return extension;
    }
    
    public String compilerName() {
        return "openmod";
    }

    protected NodeFactory createNodeFactory() {
        return new OpenModNodeFactory_c();
    }

    public Parser parser(Reader reader, FileSource source, ErrorQueue eq) {
        Lexer lexer = new Lexer_c(reader, source.path(), eq);
        Grm grm = new Grm(lexer, ts, nf, eq);

        return new CupParser(grm, source, eq);
    }
    

    protected void passes_patterns_and_parents(List l, Job job) {
        super.passes_patterns_and_parents(l, job);

        List newList = new LinkedList();
        
        newList.add(new GlobalBarrierPass(BEFORE_MODULE_COLLECT, job));
        newList.add(new VisitorPass(MODULE_COLLECT, job, new CollectModules(job, ts,
                (OpenModNodeFactory) nf, this)));
        newList.add(new GlobalBarrierPass(AFTER_MODULE_COLLECT, job));

        newList.add(new VisitorPass(CHECK_MODULE_MEMBERS, job,
                        new CheckModuleMembers(job, ts,
                                (OpenModNodeFactory) nf, this)));
        newList.add(new GlobalBarrierPass(CHECKED_MODULE_MEMBERS, job));

        newList.add(new VisitorPass(CHECK_MODULE_CYCLES, job, new CheckModuleCycles(
                job, ts, (OpenModNodeFactory) nf, this)));
        newList.add(new GlobalBarrierPass(CHECKED_MODULE_CYCLES, job));

        newList.add(new VisitorPass(CHECK_DUPLICATE_CLASS_INCLUDE, job,
                new CheckDuplicateClassInclude(job, ts,
                        (OpenModNodeFactory) nf, this)));
        newList.add(new GlobalBarrierPass(AFTER_CHECK_DUPLICATE_CLASS_INCLUDE, job));
        
        newList.add(new VisitorPass(COLLECT_OPEN_CLASS_MEMBERS, job,
                new CollectModuleOpenClassMembers(job,
                        ts, 
                        (OpenModNodeFactory) nf, 
                        this)
                        ));
        newList.add(new GlobalBarrierPass(AFTER_COLLECT_OPEN_CLASS_MEMBERS, job));
        
        newList.add(new NormalizeOpenClassMembers(NORMALIZE_OPEN_CLASS_MEMBERS,job,this));
        
        newList.add(new VisitorPass(CHECK_DECLARE_PARENTS, job,
                new CheckDeclareParents(job,ts,
                        (OpenModNodeFactory)nf, this)
                        ));
        newList.add(new GlobalBarrierPass(AFTER_CHECK_DECLARE_PARENTS, job));
        
        //TODO: Find a way to make this extensible. This is not so great
        //Find the ParentDeclarer, then insert the passes before it.
        int i = 0;
        for (i = 0; i < l.size(); i++) {
            AbstractPass currPass = (AbstractPass) l.get(i);
            if (currPass.id() == DECLARE_PARENTS) {
                break;
            }
        }
        l.addAll(i, newList);
        
    }

    protected void passes_precedence_relation(List l, Job job) {
        l.add(new OMComputePrecedence(OM_COMPUTE_PRECEDENCE, job, this));
        l.add(new GlobalBarrierPass(AFTER_OM_COMPUTE_PRECEDENCE, job));

        super.passes_precedence_relation(l, job);
        
        l.add(new OMComputeModulePrecedence(OM_COMPUTE_MODULE_PRECEDENCE, job, this));
        l.add(new GlobalBarrierPass(AFTER_OM_COMPUTE_MODULE_PRECEDENCE, job));

    }
    
    protected void passes_aspectj_transforms(List l, Job job) {
        //copy the implementation of the aspectj transforms
        //as checkmodulesigmembers needed to be added after harvestaspectinfo
        //but before cleanaspectmembers.

        //Place CheckITDs here, before AspectMethods push the itds into the
        //classes
        l.add(new VisitorPass(CHECK_ITD, job,
                new CheckITDs(job, ts, (OpenModNodeFactory)nf, this)));
        l.add(new GlobalBarrierPass(AFTER_CHECK_ITD,job));
        
    	l.add(new VisitorPass(ASPECT_REFLECTION_INSPECT,job, new AspectReflectionInspect()));
    	l.add(new VisitorPass(ASPECT_REFLECTION_REWRITE,job, new AspectReflectionRewrite(nf,ts)));

        l.add(new VisitorPass(ASPECT_METHODS,job, new AspectMethods(job,nf,ts)));
        l.add(new GlobalBarrierPass(ASPECT_PREPARE,job));
        
        l.add(new VisitorPass(HARVEST_ASPECT_INFO, job, new AspectInfoHarvester(job, ts, nf)));
        
        //Openmod passes
        //placed here so that it can use GlobalAspectInfo for getting the Aspect
        //associated with a module
        l.add(new VisitorPass(COLLECT_MODULE_ASPECTS, job, 
                new CollectModuleAspects(job, ts, (OpenModNodeFactory)nf, this)));
        l.add(new GlobalBarrierPass(AFTER_COLLECT_MODULE_ASPECTS, job));
        
        //check sig members needed to be placed before clean members so that the
        //pointcut declarations are still in the tree
        //this also needed to be placed this late so that the cflow initializations
        //have already been associated with cflow pointcuts
        l.add(new VisitorPass(CHECK_MODULE_SIG_MEMBERS, job,
                new CheckModuleSigMembers(job, ts,
                        (OpenModNodeFactory) nf, this)));
        l.add(new GlobalBarrierPass(CHECKED_MODULE_SIG_MEMBERS, job));
        //end openmod passes
        
        l.add(new VisitorPass(PRINT_OPENMOD_NODES, job, new PrintVisitor(job,
                ts, (OpenModNodeFactory) nf, this)));
        l.add(new GlobalBarrierPass(PRINTED_OPENMOD_NODES, job));
        
        l.add(new VisitorPass(CLEAN_MEMBERS, job, new CleanAspectMembers(nf,ts)));
    }
    
    protected void passes_parse_and_clean(List l, Job job) {
        super.passes_parse_and_clean(l, job);
    }
    
    //store the type system created, for use in creating the AspectType to be
    //used in the dummy aspect
    protected TypeSystem createTypeSystem() {
        this.ts = (AJTypeSystem) super.createTypeSystem(); 
        return ts;
    }
    
    public AJTypeSystem getTypeSystem() {
        return ts;
    }
    
}
