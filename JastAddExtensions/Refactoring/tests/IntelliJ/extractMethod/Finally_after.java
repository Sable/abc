class Test {
    // added the following five lines to make it compile
    Process process;
    Thread myParsingThread;
    class CompilerHandler { void processTerminated() { } };
    CompilerHandler compilerHandler;
    void someOtherCode() { }

    void method(){
        newMethod();
        someOtherCode();
    }

    private void newMethod() {
        try {
            process.waitFor();
        }
        catch(InterruptedException e) {
            process.destroy();
        }
        finally {
            try {
                myParsingThread.join();
            }
            catch(InterruptedException e) {
            }
            compilerHandler.processTerminated();
        }
        synchronized (this) {
            myParsingThread = null;
        }
    }
}