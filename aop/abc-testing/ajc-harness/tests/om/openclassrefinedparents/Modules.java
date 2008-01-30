module OpenClassTest {
	class A || B || C;
	openclass parent(!(Type+)) : Type+;
	openclass parent(*) : !(Type+);
}
