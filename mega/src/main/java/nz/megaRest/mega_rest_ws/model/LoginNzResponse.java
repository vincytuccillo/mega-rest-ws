package nz.megaRest.mega_rest_ws.model;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaNode;
import nz.megaRest.core.CoreStreamingMegaWs;

public class LoginNzResponse {

	public String codEsito;
	public String descEsito;
	public HashMap<Integer, String> listModel;
	public MegaApiJava megaApi;
	public MegaNode temp;	
	public CoreStreamingMegaWs myListener;
	public ByteArrayOutputStream bos;

	public LoginNzResponse(String codEsito, String descEsito) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
	}
	
	public LoginNzResponse(String codEsito, String descEsito,ByteArrayOutputStream bos) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
		this.bos = bos;
	}
	
	public LoginNzResponse(String codEsito, String descEsito,  HashMap<Integer, String> listModel) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
		this.listModel = listModel;
	}
	
	public LoginNzResponse(String codEsito, String descEsito, MegaApiJava megaApi) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
		this.megaApi = megaApi;
	}

	public LoginNzResponse(String codEsito, String descEsito, MegaApiJava megaApi,MegaNode temp) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
		this.megaApi = megaApi;
		this.temp = temp;
	}
	
	
	public LoginNzResponse(String codEsito, String descEsito, MegaApiJava megaApi,MegaNode temp,CoreStreamingMegaWs myListener ) {
		this.codEsito = codEsito;
		this.descEsito = descEsito;
		this.megaApi = megaApi;
		this.temp = temp;
		this.myListener = myListener;
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



	public MegaApiJava getMegaApi() {
		return megaApi;
	}



	public void setMegaApi(MegaApiJava megaApi) {
		this.megaApi = megaApi;
	}



	public MegaNode getTemp() {
		return temp;
	}



	public void setTemp(MegaNode temp) {
		this.temp = temp;
	}



	public CoreStreamingMegaWs getMyListener() {
		return myListener;
	}



	public void setMyListener(CoreStreamingMegaWs myListener) {
		this.myListener = myListener;
	}

	public ByteArrayOutputStream getBos() {
		return bos;
	}

	public void setBos(ByteArrayOutputStream bos) {
		this.bos = bos;
	}

	
	
}
