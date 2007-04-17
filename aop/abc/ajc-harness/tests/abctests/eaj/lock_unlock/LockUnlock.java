public class LockUnlock {

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
			throw new RuntimeException("LockUnlock misbehaved; log:"+log);
		}
	}
	
	static aspect TestAspect {
		
		before(Object o): lock() && args(o) {
			if(o==f) {
				log += "-bnf";
			} else if(o==g) {
				log += "-bng";
			}
	    }
		
		before(): lock() && args(Foo) {
			log += "-bnFoo";
	    }

		before(): lock() && args(LockUnlock /*some random "other class"*/) {
			log += "-bnSHOULD_NOT_MATCH";
	    }

		before(Object o): unlock() && args(o) {
			if(o==f) {
				log += "-bxf";
			} else if(o==g) {
				log += "-bxg";
			}
	    }
		
		before(): unlock() && args(Foo) {
			log += "-bxFoo";
	    }

		before(): unlock() && args(LockUnlock /*some random "other class"*/) {
			log += "-bxSHOULD_NOT_MATCH";
	    }

		after(Object o): lock() && args(o) {
			if(o==f) {
				log += "-anf";
			} else if(o==g) {
				log += "-ang";
			}
	    }

		after(Object o): unlock() && args(o) {
			if(o==f) {
				log += "-axf";
			} else if(o==g) {
				log += "-axg";
			}
	    }

	}
	
}
