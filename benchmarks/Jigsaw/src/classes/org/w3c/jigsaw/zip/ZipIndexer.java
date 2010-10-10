// ZipIndexer.java
// $Id: ZipIndexer.java,v 1.4 2000/08/16 21:37:48 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.zip; 

import java.util.Hashtable;

import java.io.File;

import org.w3c.tools.resources.indexer.SampleResourceIndexer;

import org.w3c.tools.resources.Resource;

public class ZipIndexer extends SampleResourceIndexer {

    protected Resource createDirectoryResource(File zipfile,
					       String name,
					       Hashtable defs) 
    {
	return super.createDirectoryResource(zipfile, null, name, defs);
    }

    protected Resource createFileResource(File zipfile,
					  String name,
					  Hashtable defs) 
    {
	return super.createFileResource(zipfile, null, name, defs);
    }
}
