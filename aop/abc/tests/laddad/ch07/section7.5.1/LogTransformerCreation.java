//Listing 7.18 LogTransformerCreation.java: monitors the creation of Transformer objects

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
}
