class YoYo {
    void bar () {}
    void f () {
        YoYo yoYoYo = foo();
        /*[*/yoYoYo/*]*/.bar();
    }

    private YoYoYo foo() {
	// added to improve compilability
	return null;
    }
    class YoYoYo extends YoYo {}
}