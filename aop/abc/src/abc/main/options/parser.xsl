<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:output method="text" indent="no"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/options">
/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ondrej Lhotak
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

/* THIS FILE IS AUTO-GENERATED FROM options.xml. DO NOT MODIFY. */

package abc.main.options;
import abc.main.*;
import java.util.*;
import java.io.*;

public class OptionsParser {
    private static OptionsParser instance = new OptionsParser();
    public static OptionsParser v() { return instance; }
    public static void reset() { instance = new OptionsParser(); }

    /** Parse a path.separator separated path into the separate directories. */
    private void parsePath(String path, Collection paths) {
        String[] jars = path.split(System.getProperty("path.separator"));
        for(int j = 0; j &lt; jars.length; j++) {
            // Do we need a sanity check here? !jars[j].equals("") or something like that?
            paths.add(jars[j]);
        }
    }

<xsl:apply-templates mode="fields" select="/options/section"/>
    public boolean parse(ArgList args) {
<xsl:apply-templates mode="parse" select="/options/section"/>
        return false;
    }
}
  </xsl:template>

<!--*************************************************************************-->
<!--* FIELDS TEMPLATES *******************************************************-->
<!--*************************************************************************-->

  <xsl:template mode="fields" match="* [@noparse]"/>

  <xsl:template mode="fields" match="section">
    <xsl:apply-templates mode="fields" select="boolopt|pathopt|intopt|stringopt|argfileopt"/>
  </xsl:template>

<!--* BOOLEAN_OPTION *******************************************************-->
  <xsl:template mode="fields" match="boolopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    private boolean <xsl:value-of select="$field"/><xsl:if test="default"> = <xsl:value-of select="default"/></xsl:if>;
    public boolean <xsl:value-of select="$field"/>() { return <xsl:value-of select="$field"/>; }
    public void set_<xsl:value-of select="$field"/>(boolean val) { <xsl:value-of select="$field"/> = val; }
  </xsl:template>

<!--* PATH_OPTION *******************************************************-->
  <xsl:template mode="fields" match="pathopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    private Collection <xsl:value-of select="$field"/> = new ArrayList();
    public Collection <xsl:value-of select="$field"/>() { return <xsl:value-of select="$field"/>; }
    public void set_<xsl:value-of select="$field"/>(Collection val) { <xsl:value-of select="$field"/> = val; }
  </xsl:template>

<!--* INT_OPTION *******************************************************-->
  <xsl:template mode="fields" match="intopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    private int <xsl:value-of select="$field"/><xsl:if test="default"> = <xsl:value-of select="default"/></xsl:if>;
    public int <xsl:value-of select="$field"/>() { return <xsl:value-of select="$field"/>; }
    public void set_<xsl:value-of select="$field"/>(int val) { <xsl:value-of select="$field"/> = val; }
  </xsl:template>

<!--* STRING_OPTION *******************************************************-->
  <xsl:template mode="fields" match="stringopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    private String <xsl:value-of select="$field"/><xsl:if test="default"> = "<xsl:value-of select="default"/>"</xsl:if>;
    public String <xsl:value-of select="$field"/>() { return <xsl:value-of select="$field"/>; }
    public void set_<xsl:value-of select="$field"/>(String val) { <xsl:value-of select="$field"/> = val; }
  </xsl:template>

  <xsl:template mode="fields" match="argfileopt"/>

<!--*************************************************************************-->
<!--* PARSE TEMPLATES *******************************************************-->
<!--*************************************************************************-->

  <xsl:template mode="parse" match="* [@noparse]"/>

  <xsl:template mode="parse" match="section">
    <xsl:apply-templates mode="parse" select="boolopt|pathopt|intopt|stringopt|argfileopt"/>
  </xsl:template>

<!--* BOOLEAN_OPTION *******************************************************-->
  <xsl:template mode="parse" match="boolopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    <xsl:for-each select="alias">
      if(args.top().equals("-<xsl:value-of select="."/>")
      || args.top().equals("-<xsl:value-of select="."/>:on")
      || args.top().equals("-<xsl:value-of select="."/>:true")
      || args.top().equals("-<xsl:value-of select="."/>:yes")
      ) {
        <xsl:value-of select="$field"/> = true;
        args.shift();
        return true;
      }
      if(args.top().equals("-<xsl:value-of select="."/>:off")
      || args.top().equals("-<xsl:value-of select="."/>:false")
      || args.top().equals("-<xsl:value-of select="."/>:no")
      ) {
        <xsl:value-of select="$field"/> = false;
        args.shift();
        return true;
      }
    </xsl:for-each>
  </xsl:template>

<!--* PATH_OPTION *******************************************************-->
  <xsl:template mode="parse" match="pathopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    <xsl:for-each select="alias">
      if(args.top().equals("-<xsl:value-of select="."/>")) {
        parsePath(args.argTo(), <xsl:value-of select="$field"/>);
        args.shift();
        return true;
      }
    </xsl:for-each>
  </xsl:template>

<!--* INT_OPTION *******************************************************-->
  <xsl:template mode="parse" match="intopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    <xsl:for-each select="alias">
      if(args.top().equals("-<xsl:value-of select="."/>")) {
        try {
          Integer num = Integer.decode(args.argTo());
          <xsl:value-of select="$field"/> = num.intValue();
          args.shift();
          return true;
        } catch(NumberFormatException e) {
        }
      } else if(args.top().startsWith("-<xsl:value-of select="."/>")) {
        try {
          String i = args.top().substring("-<xsl:value-of select="."/>".length());
          Integer num = Integer.decode(i);
          <xsl:value-of select="$field"/> = num.intValue();
          args.shift();
          return true;
        } catch(NumberFormatException e) {
        }
      }
    </xsl:for-each>
  </xsl:template>

<!--* STRING_OPTION *******************************************************-->
  <xsl:template mode="parse" match="stringopt">
    <xsl:variable name="field" select="translate(alias[last()],'-. ','___')"/>
    <xsl:for-each select="alias">
      if(args.top().equals("-<xsl:value-of select="."/>")) {
        <xsl:value-of select="$field"/> = args.argTo();
        args.shift();
        return true;
      } else if(args.top().startsWith("-<xsl:value-of select="."/>:")) {
        <xsl:value-of select="$field"/> = args.argTo().substring("-<xsl:value-of select="."/>".length());
        args.shift();
        return true;
      }
    </xsl:for-each>
  </xsl:template>

<!--* ARGFILE_OPTION *******************************************************-->
  <xsl:template mode="parse" match="argfileopt">
    if(args.top().startsWith("@")) {
        String fileName = args.top().substring(1);
        args.shift();
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch(IOException e) {
            throw new IllegalArgumentException("Couldn't open argfile "+fileName);
        }
        LinkedList newArgs = new LinkedList();
        try {
            while(true) {
                String s = br.readLine();
                if(s == null) break;
                newArgs.addFirst(s);
            }
        } catch(IOException e) {
            throw new IllegalArgumentException("Error reading from argfile "+fileName);
        }
        Iterator argIt = newArgs.iterator();
        while(argIt.hasNext()) args.push((String) argIt.next());
        return true;
    }
    <xsl:for-each select="alias">
    if(args.top().equals("-<xsl:value-of select="."/>")) {
        String fileName = args.argTo();
        args.shift();
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        } catch(IOException e) {
            throw new IllegalArgumentException("Couldn't open argfile "+fileName);
        }
        LinkedList newArgs = new LinkedList();
        try {
            while(true) {
                String s = br.readLine();
                if(s == null) break;
                newArgs.addFirst(s);
            }
        } catch(IOException e) {
            throw new IllegalArgumentException("Error reading from argfile "+fileName);
        }
        Iterator argIt = newArgs.iterator();
        while(argIt.hasNext()) args.push((String) argIt.next());
        return true;
    }
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>

