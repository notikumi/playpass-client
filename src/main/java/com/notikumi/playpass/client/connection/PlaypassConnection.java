package com.notikumi.playpass.client.connection;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PlaypassConnection {
	
	
	public String do_Get(String url_str, Map<String,String> headers) throws Exception {
		URL url = new URL(url_str);
		HttpGet httpget = new HttpGet(url.toURI());
		for(String headerKey : headers.keySet()){
			httpget.setHeader(headerKey, headers.get(headerKey));
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);
			Throwable t = null;
			
			String response_str;
			try {
				int responseCode = response.getStatusLine().getStatusCode();
				if (responseCode < 200 || responseCode > 299) {
					throw new Exception("GET " + url.toExternalForm() + " failed, response: " + response.toString());
				}
				
				response_str = this.createResponseFromEntity(response.getEntity());
			} catch (Throwable t2) {
				t = t2;
				throw t2;
			} finally {
				if (response != null) {
					if (t != null) {
						try {
							response.close();
						} catch (Throwable t3) {
							t.addSuppressed(t3);
						}
					} else {
						response.close();
					}
				}
				
			}
			
			return response_str;
		} catch (Exception e) {
			throw new Exception("GET " + url.toExternalForm() + " failed");
		}
	}
	
	
	public String do_Post(String url_str, Map<String,String> headers, String body) throws Exception {
		URL url = new URL(url_str);
		HttpPost httppost = new HttpPost(url.toURI());
		for(String headerKey : headers.keySet()){
			httppost.setHeader(headerKey, headers.get(headerKey));
		}
		
		StringEntity bodyEntity = new StringEntity(body, "UTF-8");
		httppost.setEntity(bodyEntity);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		CloseableHttpResponse response = null;
		try{
			response = httpclient.execute(httppost);
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode < 200 || responseCode > 299) {
				throw new Exception("POST " + url.toExternalForm() + " failed, response: " + response.toString());
			}
			
			return createResponseFromEntity(response.getEntity());
		} catch (Exception e) {
			throw new Exception("POST " + url.toExternalForm() + " failed\nresponse: " + response + "\npost: " + httppost + "\nbody: " + body + "\nheaders: " + headers);
		}
	}
	
	
	private String createResponseFromEntity(HttpEntity entity) throws IOException{
		InputStream entityStream;
		if (entity != null) {
			long length = entity.getContentLength();
			entityStream = entity.getContent();
			StringBuilder strbuilder = new StringBuilder(length > 16 && length < Integer.MAX_VALUE ? (int) length : 200);
			Reader reader = new BufferedReader(new InputStreamReader(entityStream, Charset.forName(StandardCharsets.UTF_8.name())));
			int c;
			while ((c = reader.read()) != -1) {
				strbuilder.append((char) c);
			}
			return strbuilder.toString();
		}
		return null;
	}
	
}
