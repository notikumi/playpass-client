package com.notikumi.playpass.client.service;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notikumi.playpass.client.connection.PlaypassConnection;
import com.notikumi.playpass.client.model.PlaypassCredentials;
import com.notikumi.playpass.client.request.PlaypassRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlaypassService {
	
	private static final Logger log = LoggerFactory.getLogger(PlaypassService.class);
	
	private String supplier_id;
	private String tenant; // X-Tenant HTTP header , HTTP-X-TENANT: aeg
	private String hmac_access_id;
	private String hmac_secret;
	private String uri = "https://getin.playpass.eu";
	//private String uri_test = "https://getin.demo.playpass.eu";
	private String api = "tickets_api";
	private String api_version = "v1";
	private String format = ".json";
	
	private String contentType = "application/json";
	

	
	public PlaypassService(PlaypassCredentials credentials){
		if(credentials != null){
			this.supplier_id = credentials.getSupplier_id();
			this.tenant = credentials.getTenant();
			this.hmac_access_id = credentials.getHmac_access_id();
			this.hmac_secret = credentials.getHmac_secret();
		}
	}
	

    protected PlaypassConnection getPlaypassConnection() {
        return new PlaypassConnection();
    }
    
    
    // POST https://example.com/tickets_api/v1/ticket_imports
    // Limit your ticket batches to 500 tickets per request.
    // Do not make more than 30 requests per minute.
    public void ticketImport(PlaypassRequest request) throws Exception {
		String request_type = "POST";
    	String method = "ticket_imports";
    	String path = buildPath(method);
    	String url = buildURL(path);
	
		String body = stringify(request);
	
		Map<String, String> headers = buildHeaders(path, request_type, body);
		
		sendRequest(url, headers, body);
    }
    
    
    public void sendRequest(String url, Map<String,String> headers, String body) throws Exception{
		String response = getPlaypassConnection().do_Post(url, headers, body);
		if(log.isDebugEnabled()) log.debug("response: " + response);
	}
    
	
	private Map<String, String> buildHeaders(String path, String request_type, String body) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException{
		String contentMd5Header = generateContentMd5Header(request_type, body);
		String dateHeader = generateDateHeader();
		String authorization = buildAuthorizationHeader(path, request_type, contentMd5Header, dateHeader);
        
        /*
        // 5. Set headers
		List<String> headers = array(
				"Content-Type: $contentType",
				"Content-MD5: $contentMd5",
				"Date: $strDate",
				"Authorization: APIAuth-HMAC-SHA256 $hmac_access_id:$sig",
				"X-TENANT: $tenant"
		);
        */
		Map<String,String> headers = new HashMap<>();
		headers.put("Content-Type", this.contentType);
		headers.put("Content-MD5", contentMd5Header);
		headers.put("Date", dateHeader);
		headers.put("Authorization", authorization);
		headers.put("X-TENANT", this.tenant);
		
		return headers;
	}
	
	
	public String buildAuthorizationHeader(String path, String request_type, String contentMd5Header, String dateHeader) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException{
		String canonical_string = buildCanonicalString(path, request_type, contentMd5Header, dateHeader);
        String signature = generateSignature(canonical_string);
        
        String auth = "APIAuth-HMAC-SHA256 " + this.hmac_access_id + ":" + signature;
        return auth;
    }
    
    
    public String buildPath(String method){
		String path = "/" + this.api + "/" + this.api_version + "/" + method + this.format;
		return path;
	}
	
	
	public String buildURL(String path){
		String final_url = this.uri + path;
		return final_url;
	}
	
	
	private String generateDateHeader(){
		// 2. Ensure a DATE header is set
		Date date = new Date();
		// Must be in UTC time zone
		//date -> setTimezone(new DateTimeZone('UTC'));
		// Note the GMT at the end
		//String strDate = $date -> format("D, d M Y H:i:s"). " GMT";
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String strDate = sdf.format(date) + " GMT";
		
		if(log.isDebugEnabled()) log.debug("date: " + strDate);
		return strDate;
	}
	
	
	public String buildCanonicalString(String path, String request_type, String contentMd5Header, String dateHeader){
		// 3. Create a canonical string from the HTTP request.
		// "$request_type,$contentType,$contentMd5,$path,$strDate";
		String cs = request_type + "," + this.contentType + "," + contentMd5Header + "," + path + "," + dateHeader;
		
		if(log.isDebugEnabled()) log.debug("canonical string: " + cs);
		return cs;
	}
	
	
	public String generateSignature(String canonical_string) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException{
		// 4. Create the signature
		byte[] hash = getHashHmacSha256(canonical_string, this.hmac_secret);
		if(log.isDebugEnabled()) log.debug("hash: " + Arrays.toString(hash));
		
		String sig = getHashBase64(hash);
		
		if(log.isDebugEnabled()) log.debug("signature: " + sig);
		return sig;
	}
	
	
	public byte[] getHashHmacSha256(String baseString, String keyString) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException{
		SecretKey secretKey = new SecretKeySpec(keyString.getBytes("UTF-8"), "HmacSHA256");
		
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(secretKey);
		
		byte[] text = baseString.getBytes("UTF-8");
		
		return mac.doFinal(text);
	}
	
	
	public String generateContentMd5Header(String request_type, String body) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		// 1. Generate Content-MD5 header
		if(log.isDebugEnabled()) log.debug("body: " + body);
		String cmd5;
		if(request_type.equals("GET"))
			// GET requests don't need a Content-MD5 header
			cmd5 = "";
		else{
			// The body of the request should be encoded via raw md5, and base64
			// And it should be set as the Content-MD5 header
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] message = md.digest(body.getBytes("UTF-8"));
			//MessageDigest md = MessageDigest.getInstance("SHA1");
			//byte[] digest = md.digest(message);
			cmd5 = getHashBase64(message);
			//$contentMd5 = base64_encode(md5($body, true));
		}
		
		if(log.isDebugEnabled()) log.debug("md5header: " + cmd5);
		return cmd5;
	}
	
	
	public String getHashBase64(byte[] digest) {
		
		if(log.isDebugEnabled()) log.debug("digest: " + Arrays.toString(digest));
		Base64.Encoder enc = Base64.getEncoder();
		String hash = enc.encodeToString(digest);
		
		if(log.isDebugEnabled()) log.debug("HASH calculado: " + hash);
		return hash.trim();
	}
	
	
	public String stringify(PlaypassRequest request) throws JsonProcessingException{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		return mapper.writeValueAsString(request);
	}
	
	
	public String getHmac_secret(){
		return this.hmac_secret;
	}
	
}
