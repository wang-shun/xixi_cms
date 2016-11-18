package com.sogou.ms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.sogou.ms.util.StreamUtil.*;

/** 主要用于从classpath等读取配置文件 */
public class ResourceUtil {

	/* ------------------------- as property ------------------------- */

	public static Properties classpathAsProperties(String file, Charset charset) {
		return asProperties(file, charset, true);
	}
	public static Properties fileAsProperties(String file, Charset charset) {
		return asProperties(file, charset, false);
	}
	private static Properties asProperties(String file, Charset charset, boolean byClasspath) {
		Properties p = new Properties();
		InputStream in = null;
		try {
			in = getStream(file, byClasspath);
			InputStreamReader reader = new InputStreamReader(in, charset);
			p.load(reader);
		} catch (IOException e) {
			safeClose(in);
			logger.error("load file " + file + " failed.", e);
		}
		return p;
	}

	/* ------------------------- as lines ------------------------- */

	/** 读文件每行，已去除空行和注释 */
	public static List<String> classpathAsStringList(String file, Charset charset) {
		return asStringList(file, charset, true);
	}
	/** 读文件每行，已去除空行和注释 */
	public static List<String> fileAsStringList(String file, Charset charset) {
		return asStringList(file, charset, false);
	}
	private static List<String> asStringList(String file, Charset charset, boolean byClasspath) {
		List<String> list = new ArrayList<String>();
		InputStream in = null;
		String line;
		try {
			in = getStream(file, byClasspath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("#"))
					continue;
				list.add(line);
			}
		} catch (IOException e) {
			safeClose(in);
			logger.error("load file " + file + " failed.", e);
		}
		return list;
	}

	/* ------------------------- as bytes ------------------------- */

	public static byte[] classpathAsBytes(String file) {
		return _asBytes(file, true);
	}
	public static byte[] fileAsBytes(String file) {
		return _asBytes(file, false);
	}
	private static byte[] _asBytes(String file, boolean byClasspath) {
		InputStream in = null;
		try {
			in = getStream(file, byClasspath);
			return consume(in);
		} catch (IOException e) {
			safeClose(in);
			logger.error("load file " + file + " failed.", e);
			return new byte[0];
		}
	}

	/* ------------------------- impl ------------------------- */

	private static InputStream getStream(String file, boolean byClasspath) throws IOException {
		if (byClasspath)
			return ResourceUtil.class.getClassLoader().getResourceAsStream(file);
		else
			return new FileInputStream(file);
	}
	static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);

}
