import org.aspectj.testing.Tester;

aspect IncompletePathInfoConstruction {

	  static int matches = 0;
	
      tracematch() {

                sym a before: call(* a());
                sym b before: call(* b());
                sym c before: call(* c());
                sym d before: call(* d());
                a b* (c c b*)* d {
                        matches++;
                }

      }

        public static void main(String[] args) {
                a();
                c();
                b();
                c();
                d();
                Tester.check(matches==0,"expected 0 match but saw "+matches);
        }

        static void a() {}
        static void b() {}
        static void c() {}
        static void d() {}
}
