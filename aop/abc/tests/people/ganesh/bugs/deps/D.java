public aspect D
{
    before():
        C.hello()
    {
        int x = 3;
    }
}
