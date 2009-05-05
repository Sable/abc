/* abc - The AspectBench Compiler
 * Copyright (C) 2009 Eric Bodden
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
 
package abc.da.weaving.weaver.tracing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;

import abc.da.HasDAInfo;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.aspectinfo.TracePattern;
import abc.main.Main;
import abc.main.options.OptionsParser;

public class RuntimeTraceReader {
	
	public List<Event> readTrace(String fileName) throws IOException {
		List<Event> eventStream = new ArrayList<Event>();
		
		FileInputStream inputStream = new FileInputStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		int eventNum = 0;
		while((line=reader.readLine())!=null) {
			String[] splitted = line.split(";");
			String qualifiedAdviceName = splitted[0];
			List<Integer> objectNumbers = new ArrayList<Integer>();
			for(int i=1;i<splitted.length-1;i++) {
				objectNumbers.add(Integer.parseInt(splitted[i]));
			}
			int shadowId = Integer.parseInt(splitted[splitted.length-1]);
			
			String aspectName = qualifiedAdviceName.substring(0, qualifiedAdviceName.lastIndexOf('.'));
			String adviceName = qualifiedAdviceName.substring(qualifiedAdviceName.lastIndexOf('.')+1,qualifiedAdviceName.length());
			
			DAInfo daInfo = ((HasDAInfo)Main.v().getAbcExtension()).getDependentAdviceInfo();
			TracePattern theTracePattern = null;
			String theSymbol = null;
			List<String> variableOrder = null;
			outer:
			for (TracePattern tp : daInfo.getTracePatterns()) {
				if(tp.getContainer().getName().equals(aspectName)) {
					Set<String> symbols = tp.getSymbols();
					for (String symbol : symbols) {
						SootMethod symbolAdviceMethod = tp.getSymbolAdviceMethod(symbol);
						if(symbolAdviceMethod.getName().equals(adviceName)) {
							theSymbol = symbol;
							theTracePattern = tp;
							variableOrder = tp.getVariableOrder(symbol);
							break outer;
						}
					}
					
				}
				
			}
			
			if(theTracePattern==null) {
				throw new RuntimeException("No matching dependency declaration found.");
			}
			Map<String,Integer> variableBinding = new HashMap<String, Integer>();
			int i = 0;
			for(Integer objectId: objectNumbers) {
				variableBinding.put(variableOrder.get(i), objectId);
				i++;
			}
			Event event = new Event(eventNum,theTracePattern, theSymbol, variableBinding, shadowId);
			eventNum ++;
			eventStream.add(event);
		}
		return eventStream;
	}
	
	public static void main(String[] args) throws IOException {
		String traceFile = OptionsParser.v().runtime_trace();
		List<Event> eventStream = new RuntimeTraceReader().readTrace(traceFile);
		for (Event event : eventStream) {
			System.err.println(event);
		}
	}
	

}
