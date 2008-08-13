module mysqlbackend;
package query;

import types.ResultSet;

public class MySQLQuery {
	public ResultSet runQuery() {
		return new ResultSet("MySQL");
	}
}
