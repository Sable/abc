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
package abc.tm.weaving.weaver.tmanalysis;

import java.util.HashMap;
import java.util.Map;

import soot.SootMethod;

public class Statistics {
    
    public static class Record {
        private Record() {
            this("<unnamed>");
        };
        
        private Record(String s) {
            this.name = s;
        }
        
        public int statusAbortedHitFinal = 0;

        public int statusAbortedMaxNumIterations = 0;

        public int statusAbortedMaxNumConfigs = 0;

        public int statusAbortedMaxSizeConfig = 0;

        public int statusFinished = 0;

        public int statusFinishedHitFinal = 0;

        public int statusStarted = 0;
        
        public int statusAbortedHitFinalOnSyntheticUnit = 0;

        public String name;

        
        /**
         * {@inheritDoc}
         */
        public String toString() {
            String res = "";
            res += "\nXXXXX;"+name+";statusAbortedHitFinal;" + statusAbortedHitFinal;
            res += "\nXXXXX;"+name+";statusAbortedMaxNumIterations;" + statusAbortedMaxNumIterations;
            res += "\nXXXXX;"+name+";statusAbortedMaxNumConfigs;" + statusAbortedMaxNumConfigs;
            res += "\nXXXXX;"+name+";statusAbortedMaxSizeConfig;" + statusAbortedMaxSizeConfig;
            res += "\nXXXXX;"+name+";statusAbortedHitFinalOnSyntheticUnit;" + statusAbortedHitFinalOnSyntheticUnit;
            res += "\nXXXXX;"+name+";statusFinished;" + statusFinished;
            res += "\nXXXXX;"+name+";statusFinishedHitFinal;" + statusFinishedHitFinal;
            res += "\nXXXXX;"+name+";statusStarted;" + statusStarted;            
            res += "\n";            
            return res;
        }
    }

    public int shadowsUnnecessaryShadows = 0; 

    public int shadowsRemovedUnnecessaryShadows = 0; 
    
    public int shadowsMovedCodeMotion = 0; 
    
    public int shadowsOnlyExecuteOnce = 0;
    
    public long totalIntraProceduralAnalysisTime = -1;
    
    //temp structures
    
    public int statusAbortedHitFinal = 0;

    public int statusAbortedMaxNumIterations = 0;

    public int statusAbortedMaxNumConfigs = 0;

    public int statusAbortedMaxSizeConfig = 0;

    public int statusFinished = 0;

    public int statusFinishedHitFinal = 0;

    public int statusStarted = 0;
    
    public int statusAbortedHitFinalOnSyntheticUnit = 0;

    public SootMethod currMethod;
    
    public Class currAnalysis;
    
    protected Map<SootMethod,Record> methodToRecord = new HashMap<SootMethod, Record>(); 
    
    protected Map<Class,Record> analysisToRecord = new HashMap<Class, Record>();
    
    protected Record globalRecord = new Record("global");

    public int maxNumVisitedOnSuccessfulRun = -1;

    public int maxNumConfigsOnSuccessfulRun = -1;

    public int maxSizeConfigOnSuccessfulRun = -1;

    public void commitdataSet() {
        assert currMethod!=null;
        assert currAnalysis!=null;
        
        Record methodRecord = methodToRecord.get(currMethod);
        if(methodRecord==null) {
            methodRecord = new Record();
            methodToRecord.put(currMethod, methodRecord);            
        }
        methodRecord.statusAbortedHitFinal += statusAbortedHitFinal;
        methodRecord.statusAbortedMaxNumConfigs += statusAbortedMaxNumConfigs;
        methodRecord.statusAbortedMaxSizeConfig += statusAbortedMaxSizeConfig;
        methodRecord.statusAbortedMaxNumIterations += statusAbortedMaxNumIterations;
        methodRecord.statusAbortedHitFinalOnSyntheticUnit += statusAbortedHitFinalOnSyntheticUnit;
        methodRecord.statusFinished += statusFinished;
        methodRecord.statusFinishedHitFinal += statusFinishedHitFinal;
        methodRecord.statusStarted += statusStarted;
        methodRecord.name = currMethod.getName();
        
        Record analysisRecord = analysisToRecord.get(currAnalysis);
        if(analysisRecord==null) {
            analysisRecord = new Record();
            analysisToRecord.put(currAnalysis, analysisRecord);            
        }
        analysisRecord.statusAbortedHitFinal += statusAbortedHitFinal;
        analysisRecord.statusAbortedMaxNumConfigs += statusAbortedMaxNumConfigs;
        analysisRecord.statusAbortedMaxSizeConfig += statusAbortedMaxSizeConfig;
        analysisRecord.statusAbortedMaxNumIterations += statusAbortedMaxNumIterations;
        analysisRecord.statusAbortedHitFinalOnSyntheticUnit += statusAbortedHitFinalOnSyntheticUnit;
        analysisRecord.statusFinished += statusFinished;
        analysisRecord.statusFinishedHitFinal += statusFinishedHitFinal;
        analysisRecord.statusStarted += statusStarted;
        analysisRecord.name = currAnalysis.getSimpleName();
        
        globalRecord.statusAbortedHitFinal += statusAbortedHitFinal;
        globalRecord.statusAbortedMaxNumConfigs += statusAbortedMaxNumConfigs;
        globalRecord.statusAbortedMaxSizeConfig += statusAbortedMaxSizeConfig;
        globalRecord.statusAbortedMaxNumIterations += statusAbortedMaxNumIterations;
        globalRecord.statusAbortedHitFinalOnSyntheticUnit += statusAbortedHitFinalOnSyntheticUnit;
        globalRecord.statusFinished += statusFinished;
        globalRecord.statusFinishedHitFinal += statusFinishedHitFinal;
        globalRecord.statusStarted += statusStarted;
        
        statusAbortedHitFinal = 0;
        statusAbortedMaxNumConfigs = 0;
        statusAbortedMaxSizeConfig = 0;
        statusAbortedMaxNumIterations = 0;
        statusAbortedHitFinalOnSyntheticUnit = 0;
        statusFinished = 0;
        statusFinishedHitFinal = 0;
        statusStarted = 0;
        currAnalysis = null;
        currMethod = null;
    }
    
    public void dump() {
        
        System.err.println("==============================================================================");
        System.err.println("=============================   Global Information   =========================");
        System.err.println("==============================================================================");
        System.err.println(globalRecord);
        
        System.err.println("XXXXX;totalIntraProceduralAnalysisTime;"+totalIntraProceduralAnalysisTime);
        System.err.println("XXXXX;shadowsUnnecessaryShadows;"+shadowsUnnecessaryShadows); 
        System.err.println("XXXXX;shadowsRemovedUnnecessaryShadows;"+shadowsRemovedUnnecessaryShadows); 
        System.err.println("XXXXX;shadowsMovedCodeMotion;"+shadowsMovedCodeMotion);
        System.err.println("XXXXX;shadowsOnlyExecuteOnce;"+shadowsOnlyExecuteOnce);
        System.err.println("XXXXX;maxNumVisitedOnSuccessfulRun;"+maxNumVisitedOnSuccessfulRun);
        System.err.println("XXXXX;maxNumConfigsOnSuccessfulRun;"+maxNumConfigsOnSuccessfulRun);
        System.err.println("XXXXX;maxSizeConfigOnSuccessfulRun;"+maxSizeConfigOnSuccessfulRun);

//        {
//        System.err.println("==============================================================================");
//        System.err.println("=============================   Over all methods     =========================");
//        System.err.println("==============================================================================");
//        
//        Record summary = new Record();
//        for (SootMethod method : methodToRecord.keySet()) {
//            Record r = methodToRecord.get(method);
//            System.err.println(method);
//            System.err.println(r);
//        }        
//        System.err.println(summary);
//        }

        {
        System.err.println("==============================================================================");
        System.err.println("=============================   Over all analyses    =========================");
        System.err.println("==============================================================================");
        
        for (Class analysis : analysisToRecord.keySet()) {
            Record r = analysisToRecord.get(analysis);
            System.err.println(r);
        }        
        }
    }
    
    
    
    
    //singleton pattern
	
	protected static Statistics instance;

    private Statistics() { }

    public static Statistics v() {
		if(instance==null) {
			instance = new Statistics();
		}
		return instance;		
	}
	
	/**
	 * Frees the singleton object. 
	 */
	public static void reset() {
		instance = null;
	}

}
