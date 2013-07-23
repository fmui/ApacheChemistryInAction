/**
 * Copyright 2012 Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manning.cmis.theblend.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.EncodingException;

public class HTMLHelper {

	public static String format(Object value) {
		if (value == null) {
			return "";
		}

		if (value instanceof Calendar) {
			Date date = ((Calendar) value).getTime();

			long delta = System.currentTimeMillis() - date.getTime();
			if (delta >= 0) {
				if (delta < (60 * 1000)) {
					return "just now";
				}

				if (delta < (2 * 60 * 1000)) {
					return "a minute ago";
				}

				if (delta < (10 * 60 * 1000)) {
					return ((int) Math.floor((double) delta / (60 * 1000)))
							+ " minutes ago";
				}
			}

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
			return sdf.format(date);
		} else if (value instanceof Number) {
			return NumberFormat.getInstance().format(value);
		}

		return ESAPI.encoder().encodeForHTML(value.toString());
	}

	public static String formatList(List<?> values) {
		if (values == null || values.size() < 1) {
			return "";
		}

		if (values.size() == 1) {
			return format(values.get(0));
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<ul>");

		for (Object value : values) {
			sb.append("<li>");
			sb.append(format(value));
			sb.append("</li>");
		}

		sb.append("</ul>");

		return sb.toString();
	}

	public static String formatAttribute(Object value) {
		if (value == null) {
			return "";
		}

		if (value instanceof Calendar) {
			Date date = ((Calendar) value).getTime();
			return String.valueOf(date.getTime());
		}

		return ESAPI.encoder().encodeForHTMLAttribute(value.toString());
	}

	public static String encodeUrl(HttpServletRequest request, String page) {
		return request.getScheme()
				+ "://"
				+ request.getServerName()
				+ (request.getServerPort() < 1 ? "" : ":"
						+ request.getServerPort()) + request.getContextPath()
				+ "/" + page;
	}

	public static String encodeUrlWithId(HttpServletRequest request,
			String page, String id) {
		try {
			return encodeUrl(request, page) + "?id="
					+ ESAPI.encoder().encodeForURL(id);
		} catch (EncodingException e) {
			return "???";
		}
	}

	public static String encodeUrlWithPath(HttpServletRequest request,
			String path) {
		try {
			return encodeUrl(request, "browse") + "?path="
					+ ESAPI.encoder().encodeForURL(path);
		} catch (EncodingException e) {
			return "???";
		}
	}

	public static String encodeUrlWithTag(HttpServletRequest request, String tag) {
		try {
			return encodeUrl(request, "tags") + "?tag="
					+ ESAPI.encoder().encodeForURL(tag);
		} catch (EncodingException e) {
			return "???";
		}
	}
}
