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

import java.util.Map;

import com.nosaku.rattle.vo.ApiGroupVo;

public class CommonUtil {
	public static String getStackTraceAsString(Throwable throwable) {
		java.io.StringWriter sw = new java.io.StringWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}

	public static String getGroupId(String groupName, Map<String, ApiGroupVo> apiGroupVoMap) {
		return apiGroupVoMap.values().stream().filter(group -> groupName.equalsIgnoreCase(group.getName()))
				.map(ApiGroupVo::getId).findFirst().orElse(null);
	}

	public static boolean isGroupExists(String groupName, Map<String, ApiGroupVo> apiGroupVoMap) {
		return apiGroupVoMap.values().stream().anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
	}
	
	public static boolean isLinux() {
		return "Linux".equalsIgnoreCase(System.getProperty("os.name"));
	}
}
