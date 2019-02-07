package com.notikumi.playpass.client.request;

import java.io.Serializable;
import java.util.List;

public class PlaypassImportRequest implements Serializable {

    private String supplier_id;
    private List<Object> data;
    
    /*
    "supplier_id": "1",
	"data": [ ... PlaypassTicketRequest ... ]
    */
	
	public String getSupplier_id(){
		return supplier_id;
	}
	
	public void setSupplier_id(String supplier_id){
		this.supplier_id = supplier_id;
	}
	
	public List<Object> getData(){
		return data;
	}
	
	public void setData(List<Object> data){
		this.data = data;
	}
}
