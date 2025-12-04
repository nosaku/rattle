package com.nosaku.rattle.util;

public class StringUtil {
	public static boolean nonEmptyStr(String str) {
		return (str != null && (!str.trim().equals(""))); 
	}
}
