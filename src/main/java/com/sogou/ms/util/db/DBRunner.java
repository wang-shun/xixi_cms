package com.sogou.ms.util.db;

import com.sogou.ms.util.TimeUtil;
import com.sogou.ms.util.toolkit.AutoLog;
import com.sogou.ms.util.toolkit.ResultSetMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.sogou.ms.util._.*;

public final class DBRunner extends QueryRunner {

    static final Logger logger = LoggerFactory.getLogger(DBRunner.class);
    public static boolean trace = false;
    public static boolean trace_longtime = true;
    public static int op_longtime = 200;

	/* ------------------------- init ------------------------- */

    public final String name;

    public DBRunner(String name, String host, int port, String dbName, String user, String pass) {
        this(name, host, port, dbName, user, pass, defMaxConn);
    }

    public DBRunner(String name, String host, int port, String dbName, String user, String pass, int maxConn) {
        this(name, getDataSource(host, port, dbName, user, pass, maxConn));
    }

    public DBRunner(String name, DataSource ds) {
        super(ds);

        this.name = name;
        querySucc = AutoLog.of(f("db.%s.query.succ", name));
        queryCost = AutoLog.of(f("db.%s.query.cost", name));
        queryLongTime = AutoLog.of(f("db.%s.query.long", name));
        updateSucc = AutoLog.of(f("db.%s.update.succ", name));
        updateCost = AutoLog.of(f("db.%s.update.cost", name));
        updateLongTime = AutoLog.of(f("db.%s.update.long", name));
    }

    static final int defMaxConn = 100;

    private static DataSource getDataSource(String host, int port, String dbName, String user, String pass, int maxConn) {
        // ref: http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html
        String jdbcUri = "jdbc:mysql://%s:%s/%s?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8";
        String url = f(jdbcUri, host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(com.mysql.jdbc.jdbc2.optional.MysqlDataSource.class.getName());
        config.addDataSourceProperty("url", url);
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", pass);

        config.setMaximumPoolSize(maxConn);
        config.setMinimumIdle(0);

        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        config.setTransactionIsolation("TRANSACTION_REPEATABLE_READ");
        config.setLeakDetectionThreshold(10 * TimeUtil.minute);
        // config.setConnectionTimeout(); default: 30 seconds
        // config.setIdleTimeout(); default: 10 min
        // config.setMaxLifetime(); default: 30 min

        return new HikariDataSource(config);
    }

	/* ------------------------- connection ------------------------- */

    public Connection getConnection() throws SQLException {
        return this.prepareConnection();
    }

    @Deprecated
    public Connection _conn() throws SQLException {
        return this.prepareConnection();
    }

	/* ------------------------- utils ------------------------- */


    // return auto generated primary key
    public long insertReturnKey(String sql, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        boolean isSucc = true;
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
            isSucc = false;
            this.rethrow(e, sql, params);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(conn);
            logUpdate(start, isSucc, sql);
        }
        return 0;
    }


    public int queryInt(String sql, Object... params) throws SQLException {
        return query(sql, singleIntHandler, params);
    }

    public int queryInt(String sql) throws SQLException {
        return query(sql, singleIntHandler);
    }

    public int queryInt(Connection conn, String sql, Object... params) throws SQLException {
        return query(conn, sql, singleIntHandler, params);
    }

    public int queryInt(Connection conn, String sql) throws SQLException {
        return query(conn, sql, singleIntHandler);
    }

    static final ResultSetHandler<Integer> singleIntHandler = new ResultSetHandler<Integer>() {
        public Integer handle(ResultSet rs) throws SQLException {
            return rs.next() ? rs.getInt(1) : 0;
        }
    };


    public long queryLong(String sql, Object... params) throws SQLException {
        return query(sql, singleLongHandler, params);
    }

    public long queryLong(String sql) throws SQLException {
        return query(sql, singleLongHandler);
    }

    public long queryLong(Connection conn, String sql, Object... params) throws SQLException {
        return query(conn, sql, singleLongHandler, params);
    }

    public long queryLong(Connection conn, String sql) throws SQLException {
        return query(conn, sql, singleLongHandler);
    }

    static final ResultSetHandler<Long> singleLongHandler = new ResultSetHandler<Long>() {
        public Long handle(ResultSet rs) throws SQLException {
            return rs.next() ? rs.getLong(1) : 0;
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
            return rs.next() ? rs.getDouble(1) : 0;
        }
    };


    public String queryString(String sql, Object... params) throws SQLException {
        return query(sql, singleStringHandler, params);
    }

    public String queryString(String sql) throws SQLException {
        return query(sql, singleStringHandler);
    }

    public String queryString(Connection conn, String sql, Object... params) throws SQLException {
        return query(conn, sql, singleStringHandler, params);
    }

    public String queryString(Connection conn, String sql) throws SQLException {
        return query(conn, sql, singleStringHandler);
    }

    static final ResultSetHandler<String> singleStringHandler = new ResultSetHandler<String>() {
        public String handle(ResultSet rs) throws SQLException {
            return rs.next() ? rs.getString(1) : null;
        }
    };


    public List<Integer> queryIntList(String sql, Object... params) throws SQLException {
        return query(sql, intListHandler, params);
    }

    public List<Integer> queryIntList(String sql) throws SQLException {
        return query(sql, intListHandler);
    }

    static final ResultSetHandler<List<Integer>> intListHandler = new AbstractListHandler<Integer>() {
        protected Integer handleRow(ResultSet rs) throws SQLException {
            return rs.getInt(1);
        }
    };


    public List<Long> queryLongList(String sql, Object... params) throws SQLException {
        return query(sql, longListHandler, params);
    }

    static final ResultSetHandler<List<Long>> longListHandler = new AbstractListHandler<Long>() {
        protected Long handleRow(ResultSet rs) throws SQLException {
            return rs.getLong(1);
        }
    };


    public List<String> queryStringList(String sql, Object... params) throws SQLException {
        return query(sql, strintlistHandler, params);
    }

    public List<String> queryStringList(String sql) throws SQLException {
        return query(sql, strintlistHandler);
    }

    public List<String> queryStringList(Connection conn, String sql, Object... params) throws SQLException {
        return query(conn, sql, strintlistHandler, params);
    }

    public List<String> queryStringList(Connection conn, String sql) throws SQLException {
        return query(conn, sql, strintlistHandler);
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
                List<T> list = new ArrayList<>();
                while (rs.next())
                    list.add(meta.from(rs));
                return list;
            }
        }, params);
    }


    public synchronized int dump(String sql, Object... params) throws SQLException {
        return query(sql, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws SQLException {
                ResultSetMetaData meta = rs.getMetaData();
                int len = meta.getColumnCount();
                PrintStream out = System.out;

                int rows = 0;
                while (rs.next()) {
                    // bell(2014-6): 无输出则不print thead
                    if (rows == 0) {
                        for (int i = 1; i <= len; i++) {
                            if (i > 1)
                                out.print(" \t ");
                            out.print(meta.getColumnLabel(i));
                        }
                        out.println();
                    }

                    rows++;

                    for (int i = 1; i <= len; i++) {
                        if (i > 1)
                            out.print(" \t ");
                        out.print(rs.getObject(i));
                    }
                    out.println();
                }
                return rows;
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
            logQuery(start, isSucc, sql);
        }
    }

    public final <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
        long start = System.currentTimeMillis();
        boolean isSucc = true;
        try {
            return super.query(conn, sql, rsh);
        } catch (SQLException e) {
            isSucc = false;
            throw e;
        } finally {
            logQuery(start, isSucc, sql);
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
            logQuery(start, isSucc, sql);
        }
    }

    public final <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        boolean isSucc = true;
        try {
            return super.query(conn, sql, rsh, params);
        } catch (SQLException e) {
            isSucc = false;
            throw e;
        } finally {
            logQuery(start, isSucc, sql);
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
            logUpdate(start, isSucc, sql);
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
            logUpdate(start, isSucc, sql);
        }
    }

    public final int update(Connection conn, String sql) throws SQLException {
        long start = System.currentTimeMillis();
        boolean isSucc = true;
        try {
            return super.update(conn, sql);
        } catch (SQLException e) {
            isSucc = false;
            throw e;
        } finally {
            logUpdate(start, isSucc, sql);
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
            logUpdate(start, isSucc, sql);
        }
    }

    public final int update(Connection conn, String sql, Object param) throws SQLException {
        long start = System.currentTimeMillis();
        boolean isSucc = true;
        try {
            return super.update(conn, sql, param);
        } catch (SQLException e) {
            isSucc = false;
            throw e;
        } finally {
            logUpdate(start, isSucc, sql);
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
            logUpdate(start, isSucc, sql);
        }
    }

    public final int update(Connection conn, String sql, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        boolean isSucc = true;
        try {
            return super.update(conn, sql, params);
        } catch (SQLException e) {
            isSucc = false;
            throw e;
        } finally {
            logUpdate(start, isSucc, sql);
        }
    }


	/* ------------------------- log ------------------------- */

    final AutoLog querySucc, queryCost, queryLongTime;
    final AutoLog updateSucc, updateCost, updateLongTime;

    protected void logQuery(long start, boolean isSucc, String sql) {
        int cost = (int) (System.currentTimeMillis() - start);
        boolean isLongtime = cost > op_longtime;

        if (trace)
            logger.info("query: " + sql);
        if (trace_longtime && isLongtime)
            logger.info("longtime query: " + sql);

        querySucc.addHit(isSucc);
        queryCost.add(cost);
        queryLongTime.addHit(isLongtime);
    }

    protected void logUpdate(long start, boolean isSucc, String sql) {
        int cost = (int) (System.currentTimeMillis() - start);
        boolean isLongtime = cost > op_longtime;

        if (trace)
            logger.info("update: " + sql);
        if (trace_longtime && isLongtime)
            logger.info("longtime update: " + sql);

        updateSucc.addHit(isSucc);
        updateCost.add(cost);
        updateLongTime.addHit(isLongtime);
    }

    /* ------------------------- utils ------------------------- */
    public static void closeTransactionQuietlyWithIsolation(Connection conn, boolean commitSucc) {
        if (conn != null) {
            boolean inTransaction = true;

            try {
                inTransaction = !conn.getAutoCommit();
            } catch (SQLException var8) {
                ;
            }

            if (inTransaction && !commitSucc) {
                try {
                    conn.rollback();
                } catch (SQLException var7) {
                    ;
                }
            }

            if (inTransaction) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException var6) {
                    ;
                }
            }

            try {
                if (conn.getTransactionIsolation() != 4) {
                    conn.setTransactionIsolation(4);
                }
            } catch (SQLException var5) {
                ;
            }

            try {
                conn.close();
            } catch (SQLException var4) {
                ;
            }

        }
    }


}
