public aspect Aspect {
    before() : call(int Test.fwibble(int)) {
    }
}
