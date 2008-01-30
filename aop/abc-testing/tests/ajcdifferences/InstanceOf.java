public aspect InstanceOf {
    static int x;
    before() : if(x instanceof int) { }
}