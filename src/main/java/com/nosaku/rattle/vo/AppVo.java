package com.nosaku.rattle.vo;

import java.util.List;

public class AppVo {
	private List<ApiModelVo> apiList;
	private ProxySettingsVo proxySettings;

	public List<ApiModelVo> getApiList() {
		return apiList;
	}

	public void setApiList(List<ApiModelVo> apiList) {
		this.apiList = apiList;
	}

	public ProxySettingsVo getProxySettings() {
		return proxySettings;
	}

	public void setProxySettings(ProxySettingsVo proxySettings) {
		this.proxySettings = proxySettings;
	}
}