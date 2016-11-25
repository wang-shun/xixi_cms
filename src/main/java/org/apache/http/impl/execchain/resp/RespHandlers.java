package org.apache.http.impl.execchain.resp;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.sogou.ms.util.JsonUtil;
import com.sogou.ms.util._;
import com.sogou.ms.util.toolkit.Xml;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public final class RespHandlers {

	public static final int code_err = 600;

	public static boolean isSucc(HttpResponse resp) {
		return resp.getStatusLine().getStatusCode() == 200;
	}

	public static Charset chooseCharset(HttpEntity entity, Charset defaultCharset) {
		// 1. header
		ContentType contentType = ContentType.get(entity);
		Charset charset = contentType != null ? contentType.getCharset() : null;
		// 2. default
		if (charset == null)
			charset = defaultCharset;
		// 3. UTF-8
		if (charset == null)
			charset = Consts.UTF_8;
		return charset;
	}

	/* ------------------------- impls ------------------------- */

	public static RespHandler<Pair<Integer, String>> pairHandler = new RespHandler<Pair<Integer, String>>() {
		@Override
		public Pair<Integer, String> applySucc(HttpResponse resp, Charset defaultCharset) throws IOException {
			int code = resp.getStatusLine().getStatusCode();
			String body = EntityUtils.toString(resp.getEntity(), defaultCharset);
			return Pair.of(code, body);
		}
		@Override
		public Pair<Integer, String> applyFail(int code, Exception e) {
			return Pair.of(code, e == null ? "" : e.toString());
		}
	};

	public static RespHandler<String> trimmedHandler = new RespHandler<String>() {
		@Override
		public String applySucc(HttpResponse resp, Charset defaultCharset) throws IOException {
			return _.trimToEmpty(EntityUtils.toString(resp.getEntity(), defaultCharset));
		}
		@Override
		public String applyFail(int code, Exception e) {
			return "";
		}
	};

	public static RespHandler<Document> xmlHandler = new RespHandler<Document>() {
		@Override
		public Document applySucc(HttpResponse resp, Charset defaultCharset) throws Exception {
			HttpEntity entity = resp.getEntity();
			Charset charset = chooseCharset(entity, defaultCharset);
			return Xml.parse(new InputStreamReader(entity.getContent(), charset));
		}
	};

	public static RespHandler<JsonNode> jsonHandler = new RespHandler<JsonNode>() {
		@Override
		public JsonNode applySucc(HttpResponse resp, Charset defaultCharset) throws Exception {
			HttpEntity entity = resp.getEntity();
			Charset charset = chooseCharset(entity, defaultCharset);
			return JsonUtil.parse(new InputStreamReader(entity.getContent(), charset));
		}
	};

	public static <T> RespHandler<T> withStream(final Function<InputStream, T> decoder) {
		return new RespHandler<T>() {
			@Override
			public T applySucc(HttpResponse resp, Charset defaultCharset) throws IOException {
				try (InputStream in = resp.getEntity().getContent()) {
					return decoder.apply(in);
				}
			}
		};
	}
	public static <T> RespHandler<T> withReader(final Function<Reader, T> decoder) {
		return new RespHandler<T>() {
			@Override
			public T applySucc(HttpResponse resp, Charset defaultCharset) throws IOException {
				HttpEntity entity = resp.getEntity();
				Charset charset = chooseCharset(entity, defaultCharset);
				try (Reader in = new InputStreamReader(entity.getContent(), charset)) {
					return decoder.apply(in);
				}
			}
		};
	}
	public static <T> RespHandler<T> withString(final Function<String, T> decoder) {
		return new RespHandler<T>() {
			@Override
			public T applySucc(HttpResponse resp, Charset defaultCharset) throws IOException {
				String body = EntityUtils.toString(resp.getEntity(), defaultCharset);
				return decoder.apply(body);
			}
		};
	}

}
