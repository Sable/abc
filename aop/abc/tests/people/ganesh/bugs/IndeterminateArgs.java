public aspect IndeterminateArgs {
    before(String s1)  : args(..,s1)  { }
}


