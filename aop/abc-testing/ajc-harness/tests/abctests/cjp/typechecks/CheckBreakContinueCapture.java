public aspect CheckBreakContinueCapture {
	
	void normalBreak() {
		Label:
		for(int i=0;i<1;i++) {
			exhibit JP {
				while(true) {
					break; //this is ok
				}
			};			
		}
	}
	
	void outerBreak() {
		Label:
		for(int i=0;i<1;i++) {
			exhibit JP {
				while(true) {
					break Label; //this should give an error
				}
			};			
		}
	}

	void normalContinue() {
		Label:
		for(int i=0;i<1;i++) {
			exhibit JP {
				while(true) {
					continue; //this is ok
				}
			};			
		}
	}
	
	void outerContinue() {
		Label:
		for(int i=0;i<1;i++) {
			exhibit JP {
				while(true) {
					continue Label; //this should give an error
				}
			};			
		}
	}
	
	jpi void JP();
}