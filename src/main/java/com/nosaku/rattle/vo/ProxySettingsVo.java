package com.nosaku.rattle.vo;

public class ProxySettingsVo {
	
	public enum ProxyMode {
		OFF,
		ON,
		SYSTEM_PROXY
	}
	
	private ProxyMode proxyMode = ProxyMode.OFF;
	private String httpProxy;
	private String httpsProxy;
	private String username;
	private String password;
	private boolean verifySslCertificate = true;

	public ProxySettingsVo() {
	}

	public ProxySettingsVo(String httpProxy, String httpsProxy, String username, String password) {
		this.proxyMode = ProxyMode.ON;
		this.httpProxy = httpProxy;
		this.httpsProxy = httpsProxy;
		this.username = username;
		this.password = password;
		this.verifySslCertificate = true;
	}

	public ProxySettingsVo(String httpProxy, String httpsProxy, String username, String password, boolean verifySslCertificate) {
		this.proxyMode = ProxyMode.ON;
		this.httpProxy = httpProxy;
		this.httpsProxy = httpsProxy;
		this.username = username;
		this.password = password;
		this.verifySslCertificate = verifySslCertificate;
	}
	
	public ProxySettingsVo(ProxyMode proxyMode, String httpProxy, String httpsProxy, String username, String password, boolean verifySslCertificate) {
		this.proxyMode = proxyMode;
		this.httpProxy = httpProxy;
		this.httpsProxy = httpsProxy;
		this.username = username;
		this.password = password;
		this.verifySslCertificate = verifySslCertificate;
	}

	public String getHttpProxy() {
		return httpProxy;
	}

	public void setHttpProxy(String httpProxy) {
		this.httpProxy = httpProxy;
	}

	public String getHttpsProxy() {
		return httpsProxy;
	}

	public void setHttpsProxy(String httpsProxy) {
		this.httpsProxy = httpsProxy;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isVerifySslCertificate() {
		return verifySslCertificate;
	}

	public void setVerifySslCertificate(boolean verifySslCertificate) {
		this.verifySslCertificate = verifySslCertificate;
	}

	public ProxyMode getProxyMode() {
		return proxyMode;
	}

	public void setProxyMode(ProxyMode proxyMode) {
		this.proxyMode = proxyMode;
	}
}

