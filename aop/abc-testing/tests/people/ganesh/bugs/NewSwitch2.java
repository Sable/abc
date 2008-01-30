public class NewSwitch2 {

  protected NewSwitch2() {
	this(3);
  }

  protected NewSwitch2(int p) {
	switch (p) {
	  case 3: break;
	}
  }
}

aspect FFDC2 {

	after() throwing(Throwable t) : (initialization(NewSwitch2.new(..)) || execution(NewSwitch2.new(..)))  { }

}
