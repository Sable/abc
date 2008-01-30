//Test class

import java.util.logging.*;

public class Test {
    public void test() {
	Logger logger = Logger.getLogger("test");
	logger.log(Level.INFO, "Log message");
    }
}
