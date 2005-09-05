module Module1 {
	class MatchTestA;
	aspect AspectA;
	module Module2;
	__sig {
		method * MatchTestA.a(..);
	}
}
