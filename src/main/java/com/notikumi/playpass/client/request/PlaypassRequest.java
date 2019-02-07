package com.notikumi.playpass.client.request;

import java.io.Serializable;

public class PlaypassRequest implements Serializable {

    private PlaypassImportRequest ticket_import;
    
    /*
    "ticket_import": { PlaypassImportRequest }
    */
	
	public PlaypassImportRequest getTicket_import(){
		return ticket_import;
	}
	
	public void setTicket_import(PlaypassImportRequest ticket_import){
		this.ticket_import = ticket_import;
	}
}
