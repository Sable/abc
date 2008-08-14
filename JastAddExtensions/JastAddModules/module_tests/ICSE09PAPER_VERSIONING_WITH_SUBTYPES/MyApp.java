module myapplication;

public class MyApp {
	appv1_1::App a11 = new appv1_1::App();
	appv1_2::App a12 = new appv1_2::App();
	appv2::App a2 = new appv2::App();
	public MyApp() {
		System.out.println(this.getClass());
	}
}
