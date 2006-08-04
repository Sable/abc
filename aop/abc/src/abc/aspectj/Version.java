/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.aspectj;

import java.io.*;
import java.util.Properties;

/**
 * Version information for aspectj extension
 * @author Aske Simon Christensen
 * @author Ganesh Sittampalam
 */
public class Version extends polyglot.main.Version {
    public String name() { return "aspectj"; }

    public int major() { return 1; }
    public int minor() { return 2; }
    public int patch_level() { return 1; }

    protected String properties_file() {
	return "/abc/main/version.properties";
    }

    /** A 'main' method provided so we can run this class directly
     *  to find out the current version. This is useful for the packaging
     *  scripts, for example.
     */
    public static void main(String[] args) {
	System.out.println(new Version().toString());
    }

    public String prerelease() {
	InputStream propfile=getClass().getResourceAsStream(properties_file());
	if(propfile!=null) {
	    Properties props=new Properties();
	    try {
		props.load(propfile);
		return props.getProperty("prerelease","DEV");
	    } catch(IOException e) {
		return "DEV";
	    }
	} else return "DEV";
    }

    public String toString() {
	String s=super.toString();
	String prerel=prerelease();
	if(!prerel.equals("")) s+="."+prerel;
	return s;
    }
}
