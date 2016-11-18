package com.sogou.ms.util.toolkit;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SshUtil {

	static final Logger logger = LoggerFactory.getLogger(SshUtil.class);

	/* ------------------------- builder ------------------------- */

	public static Client newClient(String host, String user, String pass) throws IOException {
		return new Client(host, user, pass);
	}
	public static class Client {
		public final Connection conn;
		public Client(String host, String user, String pass) throws IOException {
			this.conn = getConn(host, user, pass);
		}
		public Pair<String, String> exec(String cmd) throws IOException {
			return SshUtil.exec(conn, cmd);
		}
		public void close() {
			this.conn.close();
		}
	}

	/* ------------------------- impl ------------------------- */

	private static Connection getConn(String host, String user, String pass) throws IOException {
		Connection conn = new Connection(host);
		conn.connect();
		boolean authenticated = conn.authenticateWithPassword(user, pass);
		if (!authenticated)
			throw new IOException("auth failed:" + host);
		return conn;
	}
	private static Pair<String, String> exec(Connection conn, String cmd) throws IOException {
		Session sess = conn.openSession();
		sess.execCommand(cmd);

		String stdout = consume(sess.getStdout());
		String stderr = consume(sess.getStderr());

		sess.close();
		return Pair.of(stdout, stderr);
	}
	private static String consume(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new StreamGobbler(is)));
		StringBuilder out = new StringBuilder();
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			out.append(line).append('\n');
		}
		return out.toString().trim();
	}

}
