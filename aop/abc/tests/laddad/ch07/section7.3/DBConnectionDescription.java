//Listing 7.5 DBConnectionDescription.java

public class DBConnectionDescription {
    private String _url;
    private String _userName;
    private String _password;

    public DBConnectionDescription(String url, String userName,
				   String password) {
	_url = url;
	_userName = userName;
	_password = password;
    }

    public int hashCode() {
	return _url.hashCode();
    }

    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if(!obj.getClass().equals(getClass())) {
	    return false;
	}
	DBConnectionDescription desc = (DBConnectionDescription)obj;
	return (_url == null ?
		desc._url == null :
		_url.equals(desc._url))
	    && (_userName == null ?
		desc._userName == null :
		_userName.equals(desc._userName))
	    && (_password == null ?
		desc._password == null :
		_password.equals(desc._password));
    }
}
