public aspect Aspect2 {
    before() : set(int Test2.x) {
	// throw new Exception();
    }

}