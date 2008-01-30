module Module1 {
	class MatchTestA;
	friend AspectA;
	open Module2;
	
	advertise : call(* MatchTestA.a(..));
}
