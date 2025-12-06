package com.nosaku.rattle;

import java.io.StringWriter;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.nosaku.rattle.util.CommonConstants;
import com.nosaku.rattle.vo.ApiModelVo;
import com.nosaku.rattle.vo.ProxySettingsVo;

public class ApiHelper {
	private static ApiHelper instance;
	private HttpClient httpClient;
	
	private ApiHelper() {
		this.httpClient = HttpClient.newHttpClient();
	}

	public static synchronized ApiHelper getInstance() {
		if (instance == null) {
			instance = new ApiHelper();
		}
		return instance;
	}
	
	public void setProxySettings(ProxySettingsVo proxySettings) {
		this.httpClient = createHttpClientWithProxy(proxySettings);
	}
	
	private HttpClient createHttpClientWithProxy(ProxySettingsVo proxySettings) {
		HttpClient.Builder builder = HttpClient.newBuilder();
		
		if (proxySettings != null) {
			ProxySettingsVo.ProxyMode mode = proxySettings.getProxyMode();
			
			if (!proxySettings.isVerifySslCertificate()) {
				try {
					SSLContext sslContext = createInsecureSSLContext();
					builder.sslContext(sslContext);
				} catch (Exception e) {
					System.err.println("Error creating insecure SSL context: " + e.getMessage());
				}
			}
			
			if (mode == ProxySettingsVo.ProxyMode.ON) {
				if (proxySettings.getHttpProxy() != null && !proxySettings.getHttpProxy().isEmpty()) {
					String[] parts = proxySettings.getHttpProxy().split(":");
					if (parts.length == 2) {
						try {
							int port = Integer.parseInt(parts[1]);
							builder.proxy(ProxySelector.of(new InetSocketAddress(parts[0], port)));
						} catch (NumberFormatException e) {
							System.err.println("Invalid HTTP proxy port: " + parts[1]);
						}
					}
				}
				
				if (proxySettings.getUsername() != null && !proxySettings.getUsername().isEmpty() &&
					proxySettings.getPassword() != null && !proxySettings.getPassword().isEmpty()) {
					final String username = proxySettings.getUsername();
					final String password = proxySettings.getPassword();
					
					Authenticator authenticator = new Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password.toCharArray());
						}
					};
					builder.authenticator(authenticator);
				}
			} else if (mode == ProxySettingsVo.ProxyMode.SYSTEM_PROXY) {
				builder.proxy(ProxySelector.getDefault());
			}
		}
		
		return builder.build();
	}
	
	private SSLContext createInsecureSSLContext() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
				
				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
					// No-op - trust all
				}
				
				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
					// No-op - trust all
				}
			}
		};
		
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		return sslContext;
	}
	
	public void invokeApi(ApiModelVo apiModelVo) throws Exception {
		String response = null;
		StringBuilder consoleLog = new StringBuilder();
		try {
			HttpRequest httpRequest = null;
			if (CommonConstants.HTTP_METHOD_GET.equals(apiModelVo.getMethod())) {
				HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
						.uri(URI.create(getUrlWithParams(apiModelVo)));
				setHeaders(requestBuilder, apiModelVo);
				httpRequest = requestBuilder.GET().build();
			} else if (CommonConstants.HTTP_METHOD_POST.equals(apiModelVo.getMethod())) {
				HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
						.uri(URI.create(getUrlWithParams(apiModelVo)));
				setHeaders(requestBuilder, apiModelVo);
				String body = apiModelVo.getBody();
				if (body != null && !body.trim().isEmpty()) {
					try {
						ObjectMapper mapper = new ObjectMapper();
						Object jsonObject = mapper.readValue(body, Object.class);
						String validJson = mapper.writeValueAsString(jsonObject);
						requestBuilder.header("Content-Type", "application/json");
						httpRequest = requestBuilder
								.POST(HttpRequest.BodyPublishers.ofString(validJson))
								.build();
					} catch (Exception e) {
						// If JSON parsing fails, send as-is
						System.err.println("Warning: Invalid JSON format, sending as-is: " + e.getMessage());
						requestBuilder.header("Content-Type", "application/json");
						httpRequest = requestBuilder
								.POST(HttpRequest.BodyPublishers.ofString(body))
								.build();
					}
				} else if (apiModelVo.getParams() != null && !apiModelVo.getParams().isEmpty()) {
					String paramBody = getParamBody(apiModelVo);
					requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
					httpRequest = requestBuilder
							.POST(HttpRequest.BodyPublishers.ofString(paramBody))
							.build();
				} else {
					httpRequest = requestBuilder
							.POST(HttpRequest.BodyPublishers.noBody())
							.build();
				}
			} else if (CommonConstants.HTTP_METHOD_PUT.equals(apiModelVo.getMethod())) {
				HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
						.uri(URI.create(getUrlWithParams(apiModelVo)));
				setHeaders(requestBuilder, apiModelVo);
				String body = apiModelVo.getBody();
				if (body != null && !body.trim().isEmpty()) {
					try {
						ObjectMapper mapper = new ObjectMapper();
						Object jsonObject = mapper.readValue(body, Object.class);
						String validJson = mapper.writeValueAsString(jsonObject);
						requestBuilder.header("Content-Type", "application/json");
						httpRequest = requestBuilder
								.PUT(HttpRequest.BodyPublishers.ofString(validJson))
								.build();
					} catch (Exception e) {
						// If JSON parsing fails, send as-is
						System.err.println("Warning: Invalid JSON format, sending as-is: " + e.getMessage());
						requestBuilder.header("Content-Type", "application/json");
						httpRequest = requestBuilder
								.PUT(HttpRequest.BodyPublishers.ofString(body))
								.build();
					}
				} else if (apiModelVo.getParams() != null && !apiModelVo.getParams().isEmpty()) {
					String paramBody = getParamBody(apiModelVo);
					requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
					httpRequest = requestBuilder
							.PUT(HttpRequest.BodyPublishers.ofString(paramBody))
							.build();
				} else {
					httpRequest = requestBuilder
							.PUT(HttpRequest.BodyPublishers.noBody())
							.build();
				}
			} else if (CommonConstants.HTTP_METHOD_DELETE.equals(apiModelVo.getMethod())) {
				HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
						.uri(URI.create(getUrlWithParams(apiModelVo)));
				setHeaders(requestBuilder, apiModelVo);
				httpRequest = requestBuilder.DELETE().build();
			}
			if (httpRequest != null) {
				consoleLog.append("=== REQUEST ===\n");
				consoleLog.append(httpRequest.method()).append(" ").append(httpRequest.uri()).append("\n\n");
				consoleLog.append("--- Request Headers ---\n");
				httpRequest.headers().map().forEach((key, values) -> {
					values.forEach(value -> consoleLog.append(key).append(": ").append(value).append("\n"));
				});
				
				// Log request body if present
				if (apiModelVo.getBody() != null && !apiModelVo.getBody().trim().isEmpty()) {
					consoleLog.append("\n--- Request Body ---\n");
					consoleLog.append(apiModelVo.getBody()).append("\n");
				} else if ((CommonConstants.HTTP_METHOD_POST.equals(apiModelVo.getMethod()) ||
						   CommonConstants.HTTP_METHOD_PUT.equals(apiModelVo.getMethod())) && 
						   apiModelVo.getParams() != null && !apiModelVo.getParams().isEmpty()) {
					consoleLog.append("\n--- Request Body (URL-encoded) ---\n");
					apiModelVo.getParams().forEach((key, value) -> 
						consoleLog.append(key).append("=").append(value).append("&")
					);
					consoleLog.append("\n");
				}
				
				HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				apiModelVo.setStatusCode(httpResponse.statusCode());
				response = httpResponse.body();

				consoleLog.append("\n=== RESPONSE ===\n");
				consoleLog.append("Status: ").append(httpResponse.statusCode()).append("\n\n");
				consoleLog.append("--- Response Headers ---\n");
				httpResponse.headers().map().forEach((key, values) -> {
					values.forEach(value -> consoleLog.append(key).append(": ").append(value).append("\n"));
				});
				consoleLog.append("\n--- Response Body ---\n");
				
				Gson gson = new Gson();
				StringWriter stringWriter = new StringWriter();
				try (JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
					jsonWriter.setIndent("   ");
					gson.toJson(JsonParser.parseString(response), jsonWriter);
					apiModelVo.setResponse(stringWriter.toString());
					consoleLog.append(stringWriter.toString()).append("\n");
				} catch (Exception e) {
					e.printStackTrace();
					apiModelVo.setResponse(response);
					consoleLog.append(response).append("\n");
				}
				
				apiModelVo.setConsoleLog(consoleLog.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			consoleLog.append("\n=== ERROR ===\n");
			consoleLog.append(e).append("\n");
			apiModelVo.setConsoleLog(consoleLog.toString());
			throw e;
		}
	}

	private String getUrlWithParams(ApiModelVo apiModelVo) {
		String url = apiModelVo.getUrl();
		if (apiModelVo.getParams() != null && !apiModelVo.getParams().isEmpty()) {
			StringBuilder urlBuilder = new StringBuilder(url);
			urlBuilder.append(url.contains("?") ? "&" : "?");
			apiModelVo.getParams().forEach((key, value) -> 
				urlBuilder.append(key).append("=").append(value).append("&")
			);
			url = urlBuilder.toString().replaceAll("&$", "");
		}
		return url;
	}

	private void setHeaders(HttpRequest.Builder requestBuilder, ApiModelVo apiModelVo) {
		if (apiModelVo.getHeaders() != null && !apiModelVo.getHeaders().isEmpty()) {
			apiModelVo.getHeaders().forEach((key, value) -> 
				requestBuilder.header(key, value)
			);
		}		
	}

	private String getParamBody(ApiModelVo apiModelVo) {
		StringBuilder paramBuilder = new StringBuilder();
		apiModelVo.getParams().forEach((key, value) -> {
			paramBuilder.append(key).append("=").append(value).append("&");
		});
		String paramBody = paramBuilder.toString().replaceAll("&$", "");
		return paramBody;
	}
}
