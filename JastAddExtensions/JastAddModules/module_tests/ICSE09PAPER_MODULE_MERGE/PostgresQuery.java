module postgresbackend;
package query;

import types.ResultSet;

public class PostgresQuery {
	public ResultSet query() {
		return new ResultSet("Postgres");
	}
}
