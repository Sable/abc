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

import abc.main.Debug;

public class Timer {
	
	private long stamp = -1;
	private long acc = 0;
	private String name; 
	private boolean running = false;		

	public Timer(String name) {
		this.name=name;
	}
	
	public synchronized void startOrResume() {
		if(running) {
			new RuntimeException("Timer already running!").printStackTrace();								
		} else {
			if(Debug.v().timerTrace) {
				System.err.println(name+"...");
			}
			running = true;
			stamp = System.currentTimeMillis();
		}
	}
	
	public synchronized void stop() {
		if(!running) {
			new RuntimeException("Timer not running!").printStackTrace();								
		} else {
			running = false;
			acc += System.currentTimeMillis()-stamp;
			if(Debug.v().timerTrace) {
				System.err.println(name+" : "+value()+"ms");
			}
		}
	}
	
	public synchronized long value() {
		return acc;		
	}
	
	public String getName() {
		return name;
	}

	public synchronized String toString() {
		if(running) {
			return "running";								
		} else if(stamp==-1) {
			return "did not run";
		} else {
			return Long.toString(value());
		}
	}
	
}