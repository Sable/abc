module mysqlbackend;
package query;

import types.ResultSet;

public class MySQLQuery {
	public ResultSet query() {
		return new ResultSet("MySQL");
	}
}
