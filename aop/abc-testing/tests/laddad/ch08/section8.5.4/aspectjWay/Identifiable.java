//Listing 8.37 Identifiable.java: with default implementation

public interface Identifiable {
    public void setId(String id);
    public String getId();

    static aspect Impl {
	private String Identifiable._id;

	public void Identifiable.setId(String id) {
	    _id = id;
	}

	public String Identifiable.getId() {
	    return _id;
	}
    }
}
