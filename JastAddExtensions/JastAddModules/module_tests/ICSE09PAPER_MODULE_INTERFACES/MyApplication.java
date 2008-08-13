module myapplication;

public class MyApplication {
	Engine3d e3d= new Engine3d();
	EnginePhysics ephys = new EnginePhysics();

	public MyApplication() {
		System.out.println(this.getClass());
	}
}
