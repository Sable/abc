module myapplication;

public class MyApplication {
	Engine3d e3d= new Engine3d();
	EnginePhysics ephys = new EnginePhysics();
	MyFourier f = new MyFourier();

	public MyApplication() {
		System.out.println(this.getClass());
	}
}
