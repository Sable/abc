package foo;

public interface Foo {
}
aspect FooBar {
    declare parents: foo..* implements foo.Foo;
}

