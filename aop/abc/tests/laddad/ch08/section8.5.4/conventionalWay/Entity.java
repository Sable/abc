//Listing 8.33 Entity.java: implementing the Nameable interface in a conventional way

public class Entity implements Nameable {
    private String _name;

    public void setName(String name) {
	_name = name;
    }

    public String getName() {
	return _name;
    }
}

