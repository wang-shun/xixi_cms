package com.sogou.ms.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;

/** (主要用于缓存的)gzip压缩/解压 */
public class CompressUtil {

	/* ------------------------- compress ------------------------- */

	public static byte[] compress(byte[] data) throws IOException {
		if (data == null)
			return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DeflaterOutputStream deflater = new DeflaterOutputStream(out);
		deflater.write(data);
		deflater.close();
		return out.toByteArray();
	}
	public static byte[] compress(InputStream is) throws IOException {
		if (is == null)
			return null;
		is = new DeflaterInputStream(is);
		return StreamUtil.consume(is);
	}
	public static byte[] compress(String string, String charset) throws IOException {
		if (string == null)
			return null;
		return compress(string.getBytes(charset));
	}
	public static byte[] compress(String string, Charset charset) throws IOException {
		if (string == null)
			return null;
		return compress(string.getBytes(charset));
	}

	/* ------------------------- decompress ------------------------- */

	public static byte[] decompress(byte[] data) throws IOException {
		if (data == null)
			return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InflaterOutputStream inflater = new InflaterOutputStream(out);
		inflater.write(data);
		inflater.close();
		return out.toByteArray();
	}
	public static InputStream decompressToStream(byte[] data) {
		if (data == null)
			return null;
		return new InflaterInputStream(new ByteArrayInputStream(data));
	}
	public static String decompressToString(byte[] data, String charset) throws IOException {
		if (data == null)
			return null;
		return new String(decompress(data), charset);
	}
	public static String decompressToString(byte[] data, Charset charset) throws IOException {
		if (data == null)
			return null;
		return new String(decompress(data), charset);
	}

	public static byte[] decompress(byte[] data, int off, int len) throws IOException {
		if (data == null)
			return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InflaterOutputStream inflater = new InflaterOutputStream(out);
		inflater.write(data, off, len);
		inflater.close();
		return out.toByteArray();
	}
	public static String decompressToString(byte[] data, int off, int len, String charset) throws IOException {
		if (data == null)
			return null;
		return new String(decompress(data, off, len), charset);
	}
	public static String decompressToString(byte[] data, int off, int len, Charset charset) throws IOException {
		if (data == null)
			return null;
		return new String(decompress(data, off, len), charset);
	}

	/* ------------------------- misc ------------------------- */

	public static boolean isDeflater(byte[] data) {
		return isDeflater(data, 0);
	}
	public static boolean isDeflater(byte[] data, int offset) {
		return data != null && data.length >= 2 + offset
				&& (data[0 + offset] == (byte) 0x78)
				&& (data[1 + offset] == (byte) 0x9C);
	}
	// 0x50, 0x4B = zip
	// ref: http://en.wikipedia.org/wiki/Magic_number_(programming)
	// ref: http://en.wikipedia.org/wiki/List_of_file_signatures

}
