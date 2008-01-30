//Listing 6.2 ShoppingCartAccessAspect.java: enforcing access control

public aspect ShoppingCartAccessAspect {
    declare error
	: (call(* ShoppingCart.add*(..))
	   || call(* ShoppingCart.remove*(..))
	   || call(* ShoppingCart.empty(..)))
	&& !within(ShoppingCartOperator)
	: "Illegal manipulation to ShoppingCart;\n only ShoppingCartOperator may perform such operations";
}
