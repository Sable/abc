//Listing 7.20 LogTransformerCreation.java: modified to monitor Template creation

import javax.xml.transform.*;

public aspect LogTransformerCreation {
    declare precedence: LogTransformerCreation, *;

    after(Source source) returning (Transformer transformer)
	: call(* TransformerFactory.newTransformer(..))
	&& args(source) {
	System.out.println("Obtained transformer for:\n\t"
			   + source.getSystemId() + "\n\t"
			   + transformer);
    }

    after(Source source) returning (Templates templates)
	: call(* TransformerFactory.newTemplates(..))
	&& args(source) {
	System.out.println("Obtained template for:\n\t"
			   + source.getSystemId() + "\n\t"
			   + templates);
    }
}
