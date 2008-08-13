import javax.swing.Action;

public class Main {
	public static void main(String args[]) {
		somemodule.MyClass c = new somemodule.MyClass();
		Action a = c.a;
		System.out.println(a instanceof Action);
	}
}
