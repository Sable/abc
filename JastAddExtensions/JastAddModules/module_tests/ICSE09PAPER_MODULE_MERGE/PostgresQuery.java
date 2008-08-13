module postgresbackend;
package query;

import types.ResultSet;

public class PostgresQuery {
	public ResultSet runQuery() {
		return new ResultSet("Postgres");
	}
}
