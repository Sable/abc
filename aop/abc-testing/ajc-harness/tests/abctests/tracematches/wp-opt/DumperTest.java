public class DumperTest extends AbstractTest {
	
	public static void main(String[] args) {
		DumperTest t = new DumperTest();
		t.canMatch1();
		DumperTest t2 = new DumperTest();
		t2.canMatch1();
	}
	
	void canMatch1() {
		a(); x(); b();
	}


}