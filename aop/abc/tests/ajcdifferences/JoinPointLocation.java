public aspect JoinPointLocation {
    declare warning : staticinitialization(JPLocationTest) : "static";
    declare warning : execution(void JPLocationTest.main(..)) : "execution";
}
