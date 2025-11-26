package hxc.services.ecds.util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;

public class DbUtils {
	public static void makeReadUncommitted(EntityManager entityManager) {
		Session session = entityManager.unwrap(Session.class);
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED); 
			}
		});
	}
	public static void makeRepeatableRead(EntityManager entityManager) {
		Session session = entityManager.unwrap(Session.class);
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ); 
			}
		});
	}
}
