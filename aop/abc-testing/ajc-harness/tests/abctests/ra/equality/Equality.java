relational aspect Equality(Bit b1, Bit b2) {
	
    private boolean guard = false;
    
    relational after(): call(public void Bit.set()) && target(b1) {
		setOrClear(b2, true);
    }
    
    relational after(): call(public void Bit.clear()) && target(b1) {
		setOrClear(b2, false);
    }
    
    after(Bit b1, Bit b2): call(* Equality.associate(..)) && args(b1,b2)
        && !within(Equality) {
        associate(b2,b1);
    }

    after(Bit b1, Bit b2): call(* Equality.release(..)) && args(b1,b2)
        && !within(Equality) {
        release(b2,b1);
    }

    private void setOrClear(Bit b, boolean isSet) {
        if (!guard) {
		    guard = true;
		    if (isSet) {
		        b.set();
		    } else {
		        b.clear();
		    }
		    guard = false;
		}
    }
      
}
