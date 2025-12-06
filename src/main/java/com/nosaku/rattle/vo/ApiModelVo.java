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
package com.nosaku.rattle.vo;

import java.util.Map;

public class ApiModelVo implements Cloneable {
	private String id;
	private String name;
	private String method;
	private String url;
	private Map<String, String> params;
	private Map<String, String> headers;
	private String body;
	private String response;
	private int statusCode;
	private boolean isModified;
	private boolean isTabOpen;
	private int tabNbr;
	private boolean isCurrentTab;
	private String consoleLog;
	private boolean isNewTab;

	@Override
	public String toString() {
		return name;
	}

	public String toDebugString() {
		return "ApiModelVo{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", method='" + method + '\'' +
				", url='" + url + '\'' +
				", params=" + params +
				", headers=" + headers +
				", body='" + body + '\'' +
				", response='" + response + '\'' +
				", statusCode=" + statusCode +
				", isModified=" + isModified +
				'}';
	}

	@Override
	public ApiModelVo clone() {
		try {
			ApiModelVo copy = (ApiModelVo) super.clone();
			// Deep copy mutable fields if needed
			if (this.params != null) {
				copy.params = new java.util.HashMap<>(this.params);
			}
			if (this.headers != null) {
				copy.headers = new java.util.HashMap<>(this.headers);
			}
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	public boolean isTabOpen() {
		return isTabOpen;
	}

	public void setTabOpen(boolean isTabOpen) {
		this.isTabOpen = isTabOpen;
	}

	public int getTabNbr() {
		return tabNbr;
	}

	public void setTabNbr(int tabNbr) {
		this.tabNbr = tabNbr;
	}

	public boolean isCurrentTab() {
		return isCurrentTab;
	}

	public void setCurrentTab(boolean isCurrentTab) {
		this.isCurrentTab = isCurrentTab;
	}

	public String getConsoleLog() {
		return consoleLog;
	}

	public void setConsoleLog(String consoleLog) {
		this.consoleLog = consoleLog;
	}

	public boolean isNewTab() {
		return isNewTab;
	}

	public void setNewTab(boolean isNewTab) {
		this.isNewTab = isNewTab;
	}
}