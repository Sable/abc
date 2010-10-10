// IndexersCatalog.java
// $Id: IndexersCatalog.java,v 1.5 2002/06/26 17:26:03 ylafon Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources.indexer;

import java.io.File;

import org.w3c.tools.resources.ExternalContainer;
import org.w3c.tools.resources.ResourceContext;
import org.w3c.tools.resources.ServerInterface;

public class IndexersCatalog extends ExternalContainer { 

  public File getRepository(ResourceContext context) {
    return new File(context.getServer().getIndexerDirectory(),
		    "idx.db");
  }

  public IndexersCatalog(ResourceContext context) {
    super("indexers".intern(), context, true);
  }

}


