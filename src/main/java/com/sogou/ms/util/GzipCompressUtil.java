package com.sogou.ms.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sotan
 *2016-7-18 下午8:23:46
 */
public class GzipCompressUtil {

	public static final Logger logger = LoggerFactory.getLogger(GzipCompressUtil.class);

	/* ------------------------- compress ------------------------- */
	/**
	 * 使用gzip方法对String进行压缩
	 * 
	 * @param in
	 * @param charset
	 * 
	 * @return
	 */
	public static String Compress(String in, String charset) {
		if (_.isEmpty(in)) {
			return in;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(in.getBytes(charset));
		} catch (IOException e) {
			logger.error("exception_gzipString" + e);
		} finally {
			if (gzip != null) {
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return org.apache.commons.codec.binary.Base64.encodeBase64String(out.toByteArray());
	}

	/* ------------------------- decompress ------------------------- */
	/**
	 * 使用gzip方法对String进行解压缩
	 * 
	 * @param compressedStr
	 * @param charset 
	 * @return
	 */
	public static String Decompress(String compressedStr, String charset) {
		if (_.isEmpty(compressedStr)) {
			return compressedStr;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = null;
		GZIPInputStream gunzip = null;
		byte[] compressed = null;
		String decompressed = null;
		try {
			compressed = org.apache.commons.codec.binary.Base64.decodeBase64(compressedStr);
			in = new ByteArrayInputStream(compressed);
			gunzip = new GZIPInputStream(in);

			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = gunzip.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			
			decompressed = out.toString(charset);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (gunzip != null) {
				try {
					gunzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return decompressed;
	}
}
