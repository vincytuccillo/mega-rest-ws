package nz.megaRest.core;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.HashMap;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaApiSwing;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaLoggerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;


public class LoginCore implements
MegaRequestListenerInterface,
MegaLoggerInterface{

	/** Reference to the Mega API object. */
    static MegaApiSwing megaApi = null;
    static MegaApiJava megaApiJava = null;
    /**
     * Mega SDK application key.
     * Generate one for free here: https://mega.nz/#sdk
     */
    static final String APP_KEY = "YYJwAIRI";
    
    /** User agent string used for HTTP requests to the Mega API. */
    static final String USER_AGENT = "MEGA SDK";
    // Some externalised strings.    
    private static final String STR_LOGGING_IN = "Logging in...";
    private static final String STR_FETCHING_NODES = "Fetching nodes...";
    private static final String STR_PREPARING_NODES = "Preparing nodes...";
    private static final String STR_ERROR_INCORRECT_EMAIL_OR_PWD = "Incorrect email or password";
    private static String codEsito;
    private static String errorMessage;
    private static String status;
    private static HashMap<Integer, String> listModel;
    private MegaNode temp = new MegaNode() ;	
   
    static final Logger logger = LogManager.getLogger(LoginCore.class);
	public LoginCore() throws HeadlessException {        
        initializeMegaApi();
    }
	/**
     * Set logger and get reference to MEGA API
     */
	public LoginCore(String codEsito, String errorMessage, HashMap<Integer, String> listModel) {
		LoginCore.codEsito = codEsito;
		LoginCore.errorMessage = errorMessage;
		LoginCore.listModel = listModel;
	}	
    private void initializeMegaApi() {

        MegaApiSwing.setLoggerObject(this);
        MegaApiSwing.setLogLevel(MegaApiSwing.LOG_LEVEL_MAX);

        if (megaApi == null) {
            String path = System.getProperty("user.dir");
            megaApi = new MegaApiSwing(LoginCore.APP_KEY, LoginCore.USER_AGENT, path);
        }
    }

    public LoginCore initLogin(String email,String password ) {    	
    	logger.info("Start logging...");
		megaApi.login(email, password, this);	
		
    return new LoginCore (codEsito,errorMessage,listModel) ;
    }
	
	
    public void initLogout() {
		logger.info("Start logout...");
        megaApi.logout(this);
        logger.info("Finish logout...");
    }
    
	@Override
	public void log(String time, int loglevel, String source, String message) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		logger.info("onRequestStart: " + request.getRequestString());
	        if (request.getType() == MegaRequest.TYPE_LOGIN) {
	        	logger.info(STR_LOGGING_IN);
	        } else if (request.getType() == MegaRequest.TYPE_FETCH_NODES) {
	        	logger.info(STR_FETCHING_NODES);
	        }
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		logger.info("onRequestUpdate: " + request.getRequestString());
        if (request.getType() == MegaRequest.TYPE_FETCH_NODES) {
            if (request.getTotalBytes() > 0) {
                double progressValue = 100.0 * request.getTransferredBytes() / request.getTotalBytes();
                if ((progressValue > 99) || (progressValue < 0)) {
                    progressValue = 100;
                }
                logger.info("progressValue = " + (int) progressValue);
                logger.info(STR_PREPARING_NODES + String.valueOf((int) progressValue) + "%");
            }
        }
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		// TODO Auto-generated method stub
		 logger.info("onRequestFinish: " + request.getRequestString());
		
		  if (request.getType() == MegaRequest.TYPE_LOGIN) {
	            if (e.getErrorCode() == MegaError.API_OK) {
	                megaApi.fetchNodes(this);
	                codEsito = "200";
	                errorMessage = "OK";
	            } else {
	                 errorMessage = e.getErrorString();
	                 codEsito = "500";
	                if (e.getErrorCode() == MegaError.API_ENOENT) {
	                    errorMessage = STR_ERROR_INCORRECT_EMAIL_OR_PWD;
	                }

	            }
	        } else if (request.getType() == MegaRequest.TYPE_FETCH_NODES) {
	            if (e.getErrorCode() != MegaError.API_OK) {
	                

	            } else {
	                MegaNode parentNode = new MegaNode();
	                parentNode = megaApi.getRootNode();
	                ArrayList<MegaNode> nodes = new ArrayList<MegaNode>();
	                nodes = megaApi.getChildren(parentNode);
	                listModel = new HashMap<Integer,String>();
	                for (int i = 0; i < nodes.size(); i++) {
	                    temp = nodes.get(i);
	                    listModel.put(i,temp.getName());  
	                }
	            }
		 
	          setCodEsito (codEsito) ;
	  	      setErrorMessage(errorMessage);
	  	      setListModel(listModel);
	  	      initLogout();
	  	      logger.info(e.getErrorCode()+" - "+ codEsito +" - "+ errorMessage);
	  	      logger.info(listModel.values().toString()); 
	        }
		 
		
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {
		// TODO Auto-generated method stub
	}

	public String getCodEsito() {
		return codEsito;
	}
	public void setCodEsito(String codEsito) {
		LoginCore.codEsito = codEsito;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		LoginCore.errorMessage = errorMessage;
	}
	
	public void setStatus(String status) {
		LoginCore.status = status;
	}
	public HashMap<Integer, String> getListModel() {
		return listModel;
	}
	public void setListModel(HashMap<Integer, String> listModel) {
		LoginCore.listModel = listModel;
	}
	
}
