//Listing 6.1 An aspect that detects usage of System.out or System.err

aspect DetectSystemOutErrorUsage {
    declare warning : get(* System.out) || get(* System.err)
	: "Consider Logger.logp() instead";
}
