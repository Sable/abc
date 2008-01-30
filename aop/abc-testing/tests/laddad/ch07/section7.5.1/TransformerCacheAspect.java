//Listing 7.19 TransformerCacheAspect.java: the caching aspect for Transformer instances

import java.util.*;
import javax.xml.transform.*;

public aspect TransformerCacheAspect {
    Map _cache = new Hashtable();

    pointcut transformerCreation(Source source)
	: call(* TransformerFactory.newTransformer(..))
	&& args(source);

    Transformer around(Source source)
	throws TransformerConfigurationException
	: transformerCreation(source) {
	Transformer transformer
	    = (Transformer)_cache.get(source.getSystemId());
	if (transformer == null) {
	    transformer = proceed(source);
	    _cache.put(source.getSystemId(), transformer);
	}
	return transformer;
    }
}
