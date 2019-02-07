package com.notikumi.playpass.client.model;

public class PlaypassCredentials {
	
	private String supplier_id;
	private String tenant;
	private String hmac_access_id;
	private String hmac_secret;
	
	
	public PlaypassCredentials(String supplier_id, String tenant, String hmac_access_id, String hmac_secret){
		this.supplier_id = supplier_id;
		this.tenant = tenant;
		this.hmac_access_id = hmac_access_id;
		this.hmac_secret = hmac_secret;
	}
	
	
	public String getSupplier_id(){
		return supplier_id;
	}
	
	public void setSupplier_id(String supplier_id){
		this.supplier_id = supplier_id;
	}
	
	public String getTenant(){
		return tenant;
	}
	
	public void setTenant(String tenant){
		this.tenant = tenant;
	}
	
	public String getHmac_access_id(){
		return hmac_access_id;
	}
	
	public void setHmac_access_id(String hmac_access_id){
		this.hmac_access_id = hmac_access_id;
	}
	
	public String getHmac_secret(){
		return hmac_secret;
	}
	
	public void setHmac_secret(String hmac_secret){
		this.hmac_secret = hmac_secret;
	}
}
