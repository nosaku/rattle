/*
 * Copyright (c) 2025 nosaku
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.nosaku.rattle.util;

import java.util.Arrays;
import java.util.Collection;

public class CommonConstants {
	public static final String APP_TITLE = "Rattle";
	public static final String COPYRIGHT_LABEL_TEXT = "© 2025 nosaku. All rights reserved."; 
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_DELETE = "DELETE";
	public static final String HTTP_METHOD_PATCH = "PATCH";
	public static final String FILE_NAME = "rattle.json";
	public static final Collection<String> HTTP_HEADERS = Arrays.asList(
			"Accept", "Accept-Charset", "Accept-Encoding", "Accept-Language", "Accept-Datetime",
			"Authorization", "Cache-Control", "Connection", "Content-Length", "Content-Type",
			"Cookie", "Date", "Expect", "Forwarded", "From", "Host", "If-Match", "If-Modified-Since",
			"If-None-Match", "If-Range", "If-Unmodified-Since", "Max-Forwards", "Origin", "Pragma",
			"Proxy-Authorization", "Range", "Referer", "TE", "User-Agent", "Upgrade", "Via", "Warning",
			"X-Requested-With", "X-Forwarded-For", "X-Forwarded-Host", "X-Forwarded-Proto",
			"X-API-Key", "X-Auth-Token", "X-CSRF-Token", "Access-Control-Allow-Origin",
			"Access-Control-Allow-Methods", "Access-Control-Allow-Headers", "Access-Control-Max-Age",
			"Access-Control-Allow-Credentials", "X-Frame-Options", "X-Content-Type-Options",
			"Strict-Transport-Security", "Content-Security-Policy", "X-XSS-Protection"
	);
	public static final String AUTHENTICATION_TYPE_OAUTH2 = "OAuth2";
	public static final String AUTHENTICATION_TYPE_BASIC_AUTH = "Basic Auth";
	public static final String AUTHENTICATION_TYPE_API_KEY = "API Key";
	public static final Collection<String> AUTHENTICATION_TYPES = Arrays.asList(AUTHENTICATION_TYPE_OAUTH2, AUTHENTICATION_TYPE_BASIC_AUTH, AUTHENTICATION_TYPE_API_KEY);
	public static final String GROUP_NAME_HISTORY = "History";
	public static final String GROUP_NAME_AUTH_CONFIGURATIONS = "Auth configurations";
}
