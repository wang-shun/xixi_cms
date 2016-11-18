package com.sogou.ms.util.db;

import com.sogou.ms.util.infrastructure.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static com.sogou.ms.util._.*;

public class Db {

	public static Logger logger = LoggerFactory.getLogger(Db.class);

	public static DBRunner of(String alias, String defHost, String defPort, String defDb,
							  String defUser, String defPassword) {
		return of(alias, defHost, defPort, defDb, defUser, defPassword, DEFAULT_MAX_CONN);
	}

	public static DBRunner of(String alias, String defHost, String defPort, String defDb,
							  String defUser, String defPassword, int defMaxConnection) {
		DBRunner runner = registered.get(alias);
		if (runner == null) {
			String host = Config.of(alias + ".host", defHost).get();
			int port = toInt(Config.of(alias + ".port", defPort).get());
			String dbName = Config.of(alias + ".db", defDb).get();
			String user = Config.of(alias + ".user", defUser).get();
			String pass = Config.of(alias + ".password", defPassword).get();
			int maxConn = toInt(Config.of(alias + ".maximum-connection-count", defMaxConnection).get(), defMaxConnection);
			registered.put(alias, runner = new DBRunner(alias, host, port, dbName, user, pass, maxConn));
			logger.info("db register: {}={}@{}:{}/{}?max_conn={}", alias, user, host, port, dbName, maxConn);
		} else {
			logger.error("duplicate db " + alias);
		}
		return runner;
	}
	static final ConcurrentHashMap<String, DBRunner> registered = new ConcurrentHashMap<>();

	private static final int DEFAULT_MAX_CONN = 50;
}
