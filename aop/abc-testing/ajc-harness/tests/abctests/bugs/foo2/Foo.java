package foo2;

public interface Foo {
}
aspect FooBar {
    declare parents: foo2..* implements foo2.Foo;
}

