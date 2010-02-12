package p;
class B {
	A participant;

	boolean participates(A a) {
		///return (participant.id == a.id);
		return (this.participant.id == a.id);
	}
}