aspect AspectAAR {
    pointcut foo() : execution(int foo());
	int around() : execution(int foo()) {
	return proceed();
    }

    after () returning (int y) : execution(int foo()) {
    }
}