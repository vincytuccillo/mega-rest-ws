package nz.megaRest.mega_rest_ws.model;

import java.util.HashMap;

public class LoginNzResponse {

	public String codEsito;
	public String descEsito;
	public HashMap<Integer, String> listModel;

	public LoginNzResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	public LoginNzResponse(String codEsito, String descEsito) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
	}

	public LoginNzResponse(String codEsito, String descEsito,HashMap<Integer, String> listModel ) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
		this.listModel = listModel;
	}

	
	

	public String getCodEsito() {
		return codEsito;
	}

	public void setCodEsito(String codEsito) {
		this.codEsito = codEsito;
	}

	public String getDescEsito() {
		return descEsito;
	}

	public void setDescEsito(String descEsito) {
		this.descEsito = descEsito;
	}



	public HashMap<Integer, String> getListModel() {
		return listModel;
	}



	public void setListModel(HashMap<Integer, String> listModel) {
		this.listModel = listModel;
	}

	
	
}
