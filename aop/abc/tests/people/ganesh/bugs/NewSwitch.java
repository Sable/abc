public class NewSwitch {

  protected NewSwitch() {
	this(3);
  }

  protected NewSwitch(int p) {
	switch (p) {
	  case 3: break;
	}
  }
}

aspect FFDC {
//	before() : initialization(NewSwitch.new()) { }
}
