package com.sogou.ms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sogou.ms.util.StreamUtil.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EnvUtil {

	public static boolean isWindows() {
		return isWindows;
	}
	private static final boolean isWindows = System.getProperty("os.name") != null
			&& System.getProperty("os.name").toLowerCase().contains("windows");

	/**
	 * 执行shell/bat。危险，线上勿用
	 */
	public static String exec(String command) {
		StringBuilder stdout = new StringBuilder("run: " + command + "\n");
		StringBuilder stderr = new StringBuilder();
		BufferedReader readerOut = null, readerErr = null;
		try {
			String[] cmdArray = isWindows ? new String[]{"cmd", "/c", command}
					: new String[]{"/bin/sh", "-c", command};
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmdArray);

			readerOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			readerErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while ((line = readerOut.readLine()) != null) {
				stdout.append(line).append('\n');
			}
			readerOut.close();
			while ((line = readerErr.readLine()) != null) {
				stderr.append(line).append('\n');
			}
			readerErr.close();

			process.waitFor();
			logger.info("exec: {}\nsysout: {}\nsyserr: {}", command, stdout.toString().trim(),
					stderr.toString().trim());
		} catch (Exception e) {
			logger.error("shell error on: " + command, e);
			stderr.append("\nexception: ").append(e.toString());
		} finally {
			safeClose(readerOut);
			safeClose(readerErr);
		}
		return stdout.toString().trim() + (stderr.length() > 0 ? "\n" + stderr.toString().trim() : "");
	}

	static final Logger logger = LoggerFactory.getLogger(EnvUtil.class);

}
