module myapplication;
import mysqlbackend::query.MySQLQuery;
import postgresbackend::query.PostgresQuery;
import types.ResultSet;

public class Main {
	public void someMethod() {
		boolean mysql = true;
		boolean postgres = true;

		ResultSet s = null;
		if (mysql) {
			s = new MySQLQuery().query();
		} 
		System.out.println(s.getSource());
		if (postgres) {
			s = new PostgresQuery().query();
		}
		System.out.println(s.getSource());
	}
}
