//Listing 8.34 Nameable.java: with the default implementation

public interface Nameable {
    public void setName(String name);
    public String getName();

    static aspect Impl {
	private String Nameable._name;

	public void Nameable.setName(String name) {
	    _name = name;
	}

	public String Nameable.getName() {
	    return _name;
	}
    }
}
