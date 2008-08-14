module myapplication;

public class MyApp {

	app1::App a1 = new app1::App();
	app2::App a2 = new app2::App();
	app3::App a3 = new app3::App();
	app4::App a4 = new app4::App();
	app5::App a5 = new app5::App();

	feature1a::App f1a = new feature1a::App();
	feature1b::App f1b = new feature1b::App();
	feature1c::App f1c = new feature1c::App();

	feature2a::App f2a = new feature2a::App();
	feature2b::App f2b = new feature2b::App();
	public MyApp() {
		System.out.println(this.getClass());
	}
}
