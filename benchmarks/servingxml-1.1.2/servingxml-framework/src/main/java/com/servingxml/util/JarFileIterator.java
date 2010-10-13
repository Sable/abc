/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package com.servingxml.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class JarFileIterator {

  public static interface Command {
    public void doEntry(URL url);
  }

  public static void toEveryJarFile(String entryKey, Command command) {

    Set<URL> jarUrlSet = new HashSet<URL>();

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    while (classLoader != null) {
      if (classLoader instanceof URLClassLoader) {
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        if (urls != null) {
          for (int i=0; i < urls.length; ++i) {
            // If it's a jar, get Class-Path entries from manifest
            URL base = urls[i];
            InputStream is = null;
            try {
              is = base.openStream();
              JarInputStream jis;
              try {
                  jis = new JarInputStream(is,true);
              } catch (java.io.IOException ioe) {
                  jis = null;
              }
              if (jis != null) {
                Manifest manifest = jis.getManifest();
                if (manifest != null) {
                  Attributes attributes = manifest.getMainAttributes();
                  if (attributes != null) {
                    addToUrlSet(base,attributes,entryKey,jarUrlSet); 

                    String classPath = attributes.getValue(java.util.jar.Attributes.Name.CLASS_PATH);

                    //System.out.println("class path = " + classPath);
                    if (classPath != null) {
                      StringTokenizer st = new StringTokenizer(classPath, " ");
                      while (st.hasMoreTokens()) {
                        String classPathEntry = st.nextToken();
                        URL jarurl = new URL(base,classPathEntry); 
                        //System.out.println("base = " + base + ", classPathEntry = " + classPathEntry + ", jarurl = " + jarurl.toString());

                        InputStream is2 = null;
                        try {
                          is2 = jarurl.openStream();
                          JarInputStream jis2 = new JarInputStream(is2,true);
                          Manifest manifest2 = jis2.getManifest();
                          if (manifest2 != null) {
                            Attributes attributes2 = manifest2.getMainAttributes();
                            if (attributes2 != null) {
                              addToUrlSet(jarurl,attributes2,entryKey,jarUrlSet); 
                            }
                          }
                        } catch (Exception e) {
                        } finally {
                          try {
                            if (is2 != null) {
                              is2.close();
                            }
                          } catch (IOException e) {
                          }
                        }
                      }
                    }
                  }

                }
              }
            } catch (Exception e) {
            } finally {
              try {
                if (is != null) {
                  is.close();
                }
              } catch (IOException e) {
              }
            }

          }
        }
      }
      classLoader = classLoader.getParent();
    }

    Iterator<URL> iter = jarUrlSet.iterator();
    while (iter.hasNext()) {
      URL url = iter.next();
      command.doEntry(url);
    }
  }

  static void addToUrlSet(URL base, Attributes attributes, String entryKey, Set<URL> urlSet) 
  throws MalformedURLException {
    String entry = attributes.getValue(entryKey);
    if (entry != null) {
      if (!entry.startsWith("/")) {
        entry = "/" + entry;
      }
      String systemId = "jar:" + base.toString() + "!" + entry;

      //System.out.println("jar entry system id2 = " + systemId);

      URL url = new URL(systemId);
      urlSet.add(url);
    }
  }
}

