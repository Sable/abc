package foo;

public interface Foo {
}

interface Bar extends Foo {
}

aspect FooBar {
    declare parents: foo.Foo implements foo.Foo;
    declare parents: foo.Foo implements foo.Bar;
}

