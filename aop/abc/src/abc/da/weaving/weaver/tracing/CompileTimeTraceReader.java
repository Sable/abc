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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import abc.util.Pair;

public class CompileTimeTraceReader {

	public Pair<Map<Integer, Set<Integer>>, Map<Integer, Set<Integer>>> readTrace(String fileName) throws IOException {
		Map<Integer, Set<Integer>> forwardMap = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> backwardMap = new HashMap<Integer, Set<Integer>>();
		
		FileInputStream inputStream = new FileInputStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while((line=reader.readLine())!=null) {
			if(line.startsWith("#")) {
				//comment
				continue;
			}
			
			String[] split = line.split(";");

			Map<Integer, Set<Integer>> map;
			if(split[1].equals("fw")) {
				//forward-info
				map = forwardMap;
			} else {
				map = backwardMap;
			}
			
			Integer shadowNum = Integer.parseInt(split[0]);
			if(map.containsKey(shadowNum)) {
				throw new RuntimeException("duplicate entry for shadow: "+line);
			}
			
			String[] stateStrings = split[2].split(",");
			Set<Integer> states = new HashSet<Integer>();
			for (String string : stateStrings) {
				states.add(Integer.parseInt(string));
			}			
			map.put(shadowNum, states);			
		}
		
		return new Pair<Map<Integer,Set<Integer>>, Map<Integer,Set<Integer>>>(forwardMap,backwardMap);
	}

}
