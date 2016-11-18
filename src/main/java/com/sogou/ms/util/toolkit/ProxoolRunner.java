package com.sogou.ms.util.toolkit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import static com.sogou.ms.util._.f;


/** DbUtil+Proxool */
public class ProxoolRunner extends QueryRunner {

	static final Logger logger = LoggerFactory.getLogger(ProxoolRunner.class);
	public static boolean trace = false;

	/* ------------------------- register ------------------------- */

	public static boolean registered(String alias) {
		return registered.contains(alias);
	}
	public static void registerDB(String alias, String driverUrl, String user, String password) {
		registerDB(alias, driverUrl, user, password, 100);
	}
	public static void registerDB(String alias, String driverUrl, String user, String password, int maxConn) {
		if (registered.contains(alias)) {
			logger.warn("register duplicate db:" + alias);
			return;
		}
		registered.add(alias);
		try {
			final String driverClass = "org.gjt.mm.mysql.Driver";

			Class.forName(driverClass);

			Properties info = new Properties();
			info.setProperty("proxool.maximum-connection-count", Integer.toString(maxConn));
			info.setProperty("proxool.house-keeping-test-sql", "select 1");
			info.setProperty("proxool.house-keeping-sleep-time", "40000");
			info.setProperty("proxool.simultaneous-build-throttle", "30");

			info.setProperty("user", user);
			info.setProperty("password", password);

			String jdbcUri = "proxool.%s:%s:jdbc:mysql://%s?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8";
			String url = f(jdbcUri, alias, driverClass, driverUrl);
			ProxoolFacade.registerConnectionPool(url, info);
		} catch (Exception e) {
			logger.error("init db " + alias + " fail.", e);
		}
	}
	static final Set<String> registered = new HashSet<String>();

	/* ------------------------- connection ------------------------- */

	public ProxoolRunner(String alias) {
		super(new ProxoolDataSource(alias));
	}

	static class ProxoolDataSource implements DataSource {
		final String url;
		public ProxoolDataSource(String alias) {
			this.url = "proxool." + alias;
		}

		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection(url);
		}

		public PrintWriter getLogWriter() throws SQLException {
			return null;
		}
		public void setLogWriter(PrintWriter out) throws SQLException {
		}
		public void setLoginTimeout(int seconds) throws SQLException {
		}
		public int getLoginTimeout() throws SQLException {
			return 0;
		}
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return null;
		}
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return false;
		}
		public Connection getConnection(String username, String password) throws SQLException {
			return null;
		}
		public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
			return null;
		}
	}

	public Connection _conn() throws SQLException {
		return getDataSource().getConnection();
	}

	/* ------------------------- utils ------------------------- */

	/** return auto generated primary key */
	public long insertReturnKey(String sql, Object... params) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.prepareConnection();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next())
				return rs.getLong(1);
		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return -1;
	}

	public int queryInt(String sql, Object... params) throws SQLException {
		return query(sql, singleIntHandler, params);
	}
	public int queryInt(String sql) throws SQLException {
		return query(sql, singleIntHandler);
	}
	static final ResultSetHandler<Integer> singleIntHandler = new ResultSetHandler<Integer>() {
		public Integer handle(ResultSet rs) throws SQLException {
			return rs.next() ? rs.getInt(1) : -1;
		}
	};

	public long queryLong(String sql, Object... params) throws SQLException {
		return query(sql, singleLongHandler, params);
	}
	public long queryLong(String sql) throws SQLException {
		return query(sql, singleLongHandler);
	}
	static final ResultSetHandler<Long> singleLongHandler = new ResultSetHandler<Long>() {
		public Long handle(ResultSet rs) throws SQLException {
			return rs.next() ? rs.getLong(1) : -1;
		}
	};
	public List<Long> queryLongList(String sql, Object... params) throws SQLException {
		return query(sql, longlistHandler, params);
	}
	static final ResultSetHandler<List<Long>> longlistHandler = new AbstractListHandler<Long>() {
		protected Long handleRow(ResultSet rs) throws SQLException {
			return rs.getLong(1);
		}
	};


	public double queryDouble(String sql, Object... params) throws SQLException {
		return query(sql, singleDoubleHandler, params);
	}
	public double queryDouble(String sql) throws SQLException {
		return query(sql, singleDoubleHandler);
	}
	static final ResultSetHandler<Double> singleDoubleHandler = new ResultSetHandler<Double>() {
		public Double handle(ResultSet rs) throws SQLException {
			return rs.next() ? rs.getDouble(1) : -1;
		}
	};

	public String queryString(String sql, Object... params) throws SQLException {
		return query(sql, singleStringHandler, params);
	}
	public String queryString(String sql) throws SQLException {
		return query(sql, singleStringHandler);
	}
	static final ResultSetHandler<String> singleStringHandler = new ResultSetHandler<String>() {
		public String handle(ResultSet rs) throws SQLException {
			return rs.next() ? rs.getString(1) : null;
		}
	};

	public List<Integer> queryIntList(String sql, Object... params) throws SQLException {
		return query(sql, intlistHandler, params);
	}
	public List<Integer> queryIntList(String sql) throws SQLException {
		return query(sql, intlistHandler);
	}
	static final ResultSetHandler<List<Integer>> intlistHandler = new AbstractListHandler<Integer>() {
		protected Integer handleRow(ResultSet rs) throws SQLException {
			return rs.getInt(1);
		}
	};

	public List<String> queryStringList(String sql, Object... params) throws SQLException {
		return query(sql, strintlistHandler, params);
	}
	public List<String> queryStringList(String sql) throws SQLException {
		return query(sql, strintlistHandler);
	}
	static final ResultSetHandler<List<String>> strintlistHandler = new AbstractListHandler<String>() {
		protected String handleRow(ResultSet rs) throws SQLException {
			return rs.getString(1);
		}
	};

	public <T> T queryBean(Class<T> type, String sql, Object... params) throws SQLException {
		final ResultSetMapper<T> meta = ResultSetMapper.of(type);
		return query(sql, new ResultSetHandler<T>() {
			public T handle(ResultSet rs) throws SQLException {
				return rs.next() ? meta.from(rs) : null;
			}
		}, params);
	}
	public <T> List<T> queryBeans(Class<T> type, String sql, Object... params) throws SQLException {
		final ResultSetMapper<T> meta = ResultSetMapper.of(type);
		return query(sql, new ResultSetHandler<List<T>>() {
			public List<T> handle(ResultSet rs) throws SQLException {
				List<T> list = new ArrayList<T>();
				while (rs.next())
					list.add(meta.from(rs));
				return list;
			}
		}, params);
	}

	/* ------------------------- time log ------------------------- */

	public final <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		long start = System.currentTimeMillis();
		boolean isSucc = true;
		try {
			return super.query(sql, rsh);
		} catch (SQLException e) {
			isSucc = false;
			throw e;
		} finally {
			if (trace)
				logger.info("query: " + sql);
			logQuery(start, isSucc);
		}
	}
	public final <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		long start = System.currentTimeMillis();
		boolean isSucc = true;
		try {
			return super.query(sql, rsh, params);
		} catch (SQLException e) {
			isSucc = false;
			throw e;
		} finally {
			if (trace)
				logger.info("query: " + sql);
			logQuery(start, isSucc);
		}
	}

	public final int[] batch(String sql, Object[][] params) throws SQLException {
		long start = System.currentTimeMillis();
		boolean isSucc = true;
		try {
			return super.batch(sql, params);
		} catch (SQLException e) {
			isSucc = false;
			throw e;
		} finally {
			if (trace)
				logger.info("update: " + sql);
			logUpdate(start, isSucc);
		}
	}

	public final int update(String sql) throws SQLException {
		long start = System.currentTimeMillis();
		boolean isSucc = true;
		try {
			return super.update(sql);
		} catch (SQLException e) {
			isSucc = false;
			throw e;
		} finally {
			if (trace)
				logger.info("update: " + sql);
			logUpdate(start, isSucc);
		}
	}
	public final int update(String sql, Object param) throws SQLException {
		long start = System.currentTimeMillis();
		boolean isSucc = true;
		try {
			return super.update(sql, param);
		} catch (SQLException e) {
			isSucc = false;
			throw e;
		} finally {
			if (trace)
				logger.info("update: " + sql);
			logUpdate(start, isSucc);
		}
	}
	public final int update(String sql, Object... params) throws SQLException {
		long start = System.currentTimeMillis();
		boolean isSucc = true;
		try {
			return super.update(sql, params);
		} catch (SQLException e) {
			isSucc = false;
			throw e;
		} finally {
			if (trace)
				logger.info("update: " + sql);
			logUpdate(start, isSucc);
		}
	}

	//	private static int DB_LONGTIME = 200;
	protected void logQuery(long start, boolean isSucc) {
//		int cost = (int) (System.currentTimeMillis() - start);
//		AutoLogs.dbQuerySucc.add(isSucc ? 1 : 0);
//		AutoLogs.dbQueryCost.add(cost);
//		AutoLogs.dbQueryLongtime.add(cost > DB_LONGTIME ? 1 : 0);
	}
	protected void logUpdate(long start, boolean isSucc) {
//		int cost = (int) (System.currentTimeMillis() - start);
//		AutoLogs.dbUpdateSucc.add(isSucc ? 1 : 0);
//		AutoLogs.dbUpdateCost.add(cost);
//		AutoLogs.dbUpdateLongtime.add(cost > DB_LONGTIME ? 1 : 0);
	}

}

