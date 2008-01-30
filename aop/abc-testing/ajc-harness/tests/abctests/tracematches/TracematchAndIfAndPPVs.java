aspect TracematchAndIfAndPPVs
{
    tracematch()
    {
        sym foo before :
            private(int i) (args(i) && if(i < 3));
        foo
        { }
    }
}
