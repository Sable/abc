//Listing 7.19 TransformerCacheAspect.java: the caching aspect for Transformer instances

import java.util.*;
import javax.xml.transform.*;

public aspect TransformerCacheAspect {
    Map _cache = new Hashtable();

    pointcut transformerCreation(Source source)
	: call(* TransformerFactory.newTransformer(..))
	&& args(source);

    Transformer around(Source source, TransformerFactory tFactory)
	throws TransformerConfigurationException
	: transformerCreation(source) && target(tFactory) {
	Templates templates = (Templates)_cache.get(source.getSystemId());
	if (templates == null) {
	    templates = tFactory.newTemplates(source);
	    _cache.put(source.getSystemId(), templates);
	}
	return templates.newTransformer();
    }
}
