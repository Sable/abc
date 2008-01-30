
package model;

/** aspect to encode the rules about an ant resting after each move */
aspect Resting {

	/** how long to rest before the next action */
	private int Ant.resting = 0;

    /** after a move, an ant has to rest for 14 steps */
	after(Ant a) : call(void setPosition(Cell)) && target(a) {
		a.resting = 14;
	}
	
	/** count down towards waking up */
	void around(Ant a) : call(void automaton.Automaton.step(Ant)) && args(a) {
		if (a.resting > 0)
			a.resting--;
		else
			proceed(a);
	}
	
	/** adjust the result of "toString()" to include the new resting field */
	String around(Ant a) : execution(String Ant.toString()) && this(a) {
		String s = proceed(a);
		return s + ", resting "+a.resting;
	}
}
