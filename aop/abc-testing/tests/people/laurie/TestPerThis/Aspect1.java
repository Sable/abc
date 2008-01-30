public aspect Aspect1 pertarget(call (* *..*(..))) {

// has overhead of perthis even though there are no matches for this
// this aspect (because intersection of perthis and advice 1 is empty?

  void message(String s) { System.out.println("****************" + s); }

  // advice 1
  before () : set(* *..*.*) && !within(Aspect*) 
  {
    message("before setting");

  }
}

