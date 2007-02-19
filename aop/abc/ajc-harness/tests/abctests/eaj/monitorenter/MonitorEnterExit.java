public class MonitorEnterExit {

	static class Foo{}
	
	static Foo f,g;
	
	static String log = "";
	
	public static void main(String[] args) {
		f = new Foo();
		g = new Foo();
		
		synchronized (f) {
			log += "-sf";
			
			synchronized (g) {
				log += "-sg";
			}
		}
				
		if(!log.equals("-bnf-bnFoo-anf-sf-bng-bnFoo-ang-sg-bxg-bxFoo-axg-bxf-bxFoo-axf")) {			            
			throw new RuntimeException("MonitorEnterExit misbehaved; log:"+log);
		}
	}
	
	static aspect TestAspect {
		
		before(Object o): monitorenter(o) {
			if(o==f) {
				log += "-bnf";
			} else if(o==g) {
				log += "-bng";
			}
	    }
		
		before(): monitorenter(Foo) {
			log += "-bnFoo";
	    }

		before(): monitorenter(MonitorEnterExit /*some random "other class"*/) {
			log += "-bnSHOULD_NOT_MATCH";
	    }

		before(Object o): monitorexit(o) {
			if(o==f) {
				log += "-bxf";
			} else if(o==g) {
				log += "-bxg";
			}
	    }
		
		before(): monitorexit(Foo) {
			log += "-bxFoo";
	    }

		before(): monitorexit(MonitorEnterExit /*some random "other class"*/) {
			log += "-bxSHOULD_NOT_MATCH";
	    }

		after(Object o): monitorenter(o) {
			if(o==f) {
				log += "-anf";
			} else if(o==g) {
				log += "-ang";
			}
	    }

		after(Object o): monitorexit(o) {
			if(o==f) {
				log += "-axf";
			} else if(o==g) {
				log += "-axg";
			}
	    }

	}
	
}
