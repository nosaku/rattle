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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class OAuthTokenStore {
	private static OAuthTokenStore instance;
	private Map<String, Token> tokenMap;

	private OAuthTokenStore() {
		tokenMap = new HashMap<>();
	}

	public static synchronized OAuthTokenStore getInstance() {
		if (instance == null) {
			instance = new OAuthTokenStore();
		}
		return instance;
	}

	public String getBearerToken(String id, String tokenJsonStr) {
		Token token = tokenMap.get(id);
		if (isTokenExpired(token)) {
			token = new Gson().fromJson(tokenJsonStr, Token.class);
			token.setIssued_time(new Date().getTime());
			tokenMap.put(id, token);
		}
		return "Bearer " + token.getAccess_token();
	}
	
	private boolean isTokenExpired(Token token) {
		boolean isExpired = false;
		if (token == null) {
			isExpired = true;
		} else {
			long currTimeMillis = new Date().getTime();
			long issuedtimeMillis = token.getIssued_time();
			long diff = currTimeMillis - issuedtimeMillis;
			int bufferMillis = 60000;
			if (diff >= token.getExpires_in()*1000 - bufferMillis) {
				isExpired = true;
			}
		}
		return isExpired;
	}

	public static class Token {
		private String access_token;
		private int expires_in;
		private long issued_time;

		public String getAccess_token() {
			return access_token;
		}
		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}
		public int getExpires_in() {
			return expires_in;
		}
		public void setExpires_in(int expires_in) {
			this.expires_in = expires_in;
		}
		public long getIssued_time() {
			return issued_time;
		}
		public void setIssued_time(long issued_time) {
			this.issued_time = issued_time;
		}
	}
}
