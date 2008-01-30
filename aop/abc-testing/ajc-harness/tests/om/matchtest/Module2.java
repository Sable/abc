module Module2 {
	class MatchTestB;
	friend AspectB;
	
	advertise : call(* MatchTestB.a(..));
}
