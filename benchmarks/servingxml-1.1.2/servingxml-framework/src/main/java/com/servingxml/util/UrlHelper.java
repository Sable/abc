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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.net.URISyntaxException;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class UrlHelper {
  private static String getFilename(URL url) {
    String systemId = url.toString();
    String jarFilename = systemId;
    int pos = systemId.lastIndexOf("/");
    if (pos != -1) {
      if (++pos < systemId.length()) {
        jarFilename = systemId.substring(pos);
      } else {
        jarFilename = "";
      }
    }
    return jarFilename;
  }

  public static URL createUrl(String systemId) {

    try {
      URL url = null;
      try {
        url = new URL(systemId);
      } catch (MalformedURLException e) {
        //  Cribbed from saxon
        String dir = System.getProperty("user.dir");
        if (!(dir.endsWith("/") || systemId.startsWith("/"))) {
          dir = dir + "/";
        }

        URL currentDirectoryUrl = new File(dir).toURI().toURL();

        //System.out.println("dir = " + dir + ", systemId = " + systemId + ", currentDirURL = " + currentDirectoryUrl);
        url = new URL(currentDirectoryUrl,systemId);
      }
      return url;
    } catch (MalformedURLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  //  Partly based on code in Apache Axis for generating class path
  public static URL makeJarUrl(String jarFilename, String jarEntry) 
  throws MalformedURLException {
    URL url = locateJarFile(jarFilename);

    if (!jarEntry.startsWith("/")) {
      jarEntry = "/" + jarEntry;
    }

    URL jarUrl = null;
    if (url != null) {
      String systemId = "jar:" + url.toString() + "!" + jarEntry;
      jarUrl = new URL(systemId);
    }

    return jarUrl;
  }

  //  Partly based on code in Apache Axis for generating class path
  public static URL locateJarFile(String jarFilename) 
  throws MalformedURLException {
    URL url = null;

    String mycp = System.getProperty("java.class.path");
    String userDir = System.getProperty("user.dir");

    //System.out.println("MY CLASSPATH = " + mycp + ", MY USER DIR" + userDir);

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    boolean done = false;
    while (classLoader != null && !done) {
      if (classLoader instanceof URLClassLoader) {
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        if (urls != null) {
          for (int i=0; i < urls.length && !done; ++i) {
            String fn = getFilename(urls[i]);
            if (fn.equals(jarFilename)) {
              url = urls[i];
              done = true;
            } else {
              // If it's a jar, get Class-Path entries from manifest
              File file = new File(urls[i].getFile());
              if (file.isFile()) {
                FileInputStream fis = null;
                try {
                  fis = new FileInputStream(file);

                  if (isJar(fis)) {
                    JarFile jar = new JarFile(file);
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                      Attributes attributes = manifest.getMainAttributes();
                      if (attributes != null) {
                        String s = attributes.getValue(java.util.jar.Attributes.Name.CLASS_PATH);
                        URL base = urls[i];

                        if (s != null) {
                          StringTokenizer st = new StringTokenizer(s, " ");
                          while (st.hasMoreTokens() && !done) {
                            String t = st.nextToken();
                            URL jarurl = new URL(base,t);
                            String fn2 = getFilename(jarurl);
                            if (fn2.equals(jarFilename)) {
                              url = jarurl;
                              done = true;
                            }
                          }
                        }
                      }
                    }
                  }
                } catch (IOException ioe) {
                  if (fis != null) {
                    try {
                      fis.close();
                    } catch (IOException ioe2) {
                    }
                  }
                }
              }
            }
          }
        }
      }
      classLoader = classLoader.getParent();
    }

    return url;
  }

  // If exception or no entries, not a jar
  private static boolean isJar(InputStream is) {
    boolean jar = false;
    try {
      JarInputStream jis = new JarInputStream(is);
      if (jis.getNextEntry() != null) {
        jar = true;
      }
    } catch (IOException ioe) {
    }

    return jar;
  }

  public static URL createUrl(String systemId, String base) {
    try {
      URL absoluteUrl;
      if (systemId == null) {
        throw new ServingXmlException("Relative URI not supplied");
      } 
      systemId = escapeSpaces(systemId);
      if (base == null || base.length() == 0) {
        URI absoluteUri = new URI(systemId);
        if (!absoluteUri.isAbsolute()) {
          String expandedBase = tryToExpand(base);
          if (!expandedBase.equals(base)) { // prevent infinite recursion
            absoluteUrl = createUrl(systemId, expandedBase);
          } else {
            absoluteUrl = absoluteUri.toURL();
          }
        } else {
          absoluteUrl = absoluteUri.toURL();
        }
      } else {
        base = escapeSpaces(base);
        URI baseUri = new URI(base);
        new URI(systemId);   // validate only
        URI absoluteUri = baseUri.resolve(systemId);
        absoluteUrl = absoluteUri.toURL();
      }
      
      return absoluteUrl;
    } catch (IllegalArgumentException err0) {
      throw new ServingXmlException("Cannot resolve URI against base " + base + " " + systemId);
    } catch (URISyntaxException e) {
      throw new ServingXmlException("Cannot create URI " + systemId);
    } catch (MalformedURLException e) {
      throw new ServingXmlException("Cannot create URI " + systemId);
    }
  }

  /**
   * Replace spaces by %20
   */

  public static String escapeSpaces(String s) {
    if (s != null && s.length() > 0) {
      int i = s.indexOf(' ');
      if (i >= 0) {
        StringBuilder buf = new StringBuilder();
        if (i > 0) {
          buf.append(s.substring(0,i));
        }
        for (; i < s.length(); ++i) {
          char c = s.charAt(i);
          if (c == ' ') {
            buf.append("%20");
          } else {
            buf.append(c);
          }
        }
        s = buf.toString();
      }
    }
    return s;
  }

  public static String tryToExpand(String systemId) {
    if (systemId==null) {
      systemId = "";
    }
    try {
      new URL(systemId);
      return systemId;   // all is well
    } catch (MalformedURLException err) {
      String dir;
      try {
        dir = System.getProperty("user.dir");
      } catch (Exception geterr) {
        // this doesn't work when running an applet
        return systemId;
      }
      if (!(dir.endsWith("/") || systemId.startsWith("/"))) {
        dir = dir + '/';
      }

      try {
        URL currentDirectoryURL = new File(dir).toURL();
        URL baseURL = new URL(currentDirectoryURL, systemId);
        // System.err.println("SAX Driver: expanded " + systemId + " to " + baseURL);
        return baseURL.toString();
      } catch (MalformedURLException err2) {
        // go with the original one
        return systemId;
      }
    }
  }
}
