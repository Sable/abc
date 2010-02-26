package p;

///import p.Foo.Inner.MoreInner;

public class Bar {
	
	{
		Foo.Inner.///
		MoreInner.bar(new Foo());
	}

}
