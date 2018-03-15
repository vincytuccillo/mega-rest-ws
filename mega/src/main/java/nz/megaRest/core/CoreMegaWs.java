package nz.megaRest.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferListenerInterface;
import nz.mega.sdk.MegaUser;
import nz.megaRest.mega_rest_ws.model.LoginNzResponse;

@Component
public class CoreMegaWs implements MegaRequestListenerInterface,
MegaTransferListenerInterface,
MegaGlobalListenerInterface {

	/** Reference to the Mega API object. */
	static MegaApiJava megaApi = null;

	/**
	 * Mega SDK application key.
	 * Generate one for free here: https://mega.nz/#sdk
	 */
	static final String APP_KEY = "YYJwAIRI";

	/** Condition variable to signal asynchronous continuation. */
	private Object continueEvent = null;

	/** Signal for condition variable to indicate signalling. */
	private boolean wasSignalled = false;

	/** Root node of the logged in account. */
	private MegaNode rootNode = null;

	/** Java logging utility. */
    static final Logger logger = LogManager.getLogger(CoreMegaWs.class);

    private static final String STR_ERROR_INCORRECT_EMAIL_OR_PWD = "Incorrect email or password";
    private static String codEsito;
    private static String errorMessage;
    private MegaNode temp = new MegaNode() ;	
    private static HashMap<Integer, String> listModel;
	
	/** Constructor. */
	public CoreMegaWs() {
		// Make a new condition variable to signal continuation.
		this.continueEvent = new Object();

		// Get reference to Mega API.
		if (megaApi == null) {
			String path = System.getProperty("user.dir");
			megaApi = new MegaApiJava(CoreMegaWs.APP_KEY, path);
		}
	}
	
	
	public LoginNzResponse login(String email, String password) {
		// Log in.
		logger.info("*** start: login ***");
		CoreMegaWs myListener = new CoreMegaWs();
		
		synchronized(myListener.continueEvent) {
			megaApi.login(email, password, myListener);
			while (!myListener.wasSignalled) {
				try {
					myListener.continueEvent.wait();
				} catch(InterruptedException e) {
					logger.info("login interrupted: " + e.toString());
				}
			}
			
			myListener.wasSignalled = false;
			logout(myListener);
			 return new LoginNzResponse (codEsito,errorMessage,listModel) ;
		}
			
	}

    // Logout.
    public void logout(CoreMegaWs myListener) {
	logger.info("*** start: logout ***");
    synchronized(myListener.continueEvent) {
        megaApi.logout(myListener);
        while (!myListener.wasSignalled) {
            try {
                myListener.continueEvent.wait();
            } catch(InterruptedException e) {
                logger.warn("remove interrupted: " + e.toString());
            }
        }
        myListener.wasSignalled = false;
    }
    myListener.rootNode = null;
    logger.info("*** done: logout ***");
    
}
	
	
	// Implementation of listener methods.

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaRequestListenerInterface#onRequestStart(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaRequest)
	 */
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		logger.info("Request start (" + request.getRequestString() + ")");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaRequestListenerInterface#onRequestUpdate(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaRequest)
	 */
	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		logger.info("Request update (" + request.getRequestString() + ")");

		if (request.getType() == MegaRequest.TYPE_FETCH_NODES) {
			if (request.getTotalBytes() > 0) {
				double progressValue = 100.0 * request.getTransferredBytes()
						/ request.getTotalBytes();
				if ((progressValue > 99) || (progressValue < 0)) {
					progressValue = 100;
				}
				 logger.info("progressValue = " + (int) progressValue);
				 logger.info("Preparing nodes ... " + String.valueOf((int)progressValue) + "%");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaRequestListenerInterface#onRequestFinish(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaRequest, nz.mega.sdk.MegaError)
	 */
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		logger.info("Request finish (" + request.getRequestString()
		+ "); Result: " + e.toString());

		int requestType = request.getType();

		
		if (requestType == MegaRequest.TYPE_LOGIN) {
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
        } else if (requestType == MegaRequest.TYPE_FETCH_NODES) {
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
  	      logger.info(e.getErrorCode()+" - "+ codEsito +" - "+ errorMessage);
  	      logger.info(listModel.values().toString()); 
        }
		

		// Send the continue event so our synchronised code continues.
		if (requestType != MegaRequest.TYPE_LOGIN) {
			synchronized(this.continueEvent){
				this.wasSignalled = true;
				this.continueEvent.notify();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaRequestListenerInterface#onRequestTemporaryError(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaRequest, nz.mega.sdk.MegaError)
	 */
	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {
		logger.warn("Request temporary error (" + request.getRequestString()
		+ "); Error: " + e.toString());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaTransferListenerInterface#onTransferStart(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaTransfer)
	 */
	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		logger.info("Transfer start: " + transfer.getFileName());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaTransferListenerInterface#onTransferFinish(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaTransfer, nz.mega.sdk.MegaError)
	 */
	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer,
			MegaError e) {
		logger.info("Transfer finished (" + transfer.getFileName()
		+ "); Result: " + e.toString() + " ");
		// Signal the other thread we're done.
		synchronized(this.continueEvent){
			this.wasSignalled = true;
			this.continueEvent.notify();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaTransferListenerInterface#onTransferUpdate(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaTransfer)
	 */
	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {
		logger.info("Transfer finished (" + transfer.getFileName()
		+ "): " + transfer.getSpeed() + " B/s ");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaTransferListenerInterface#onTransferTemporaryError(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaTransfer, nz.mega.sdk.MegaError)
	 */
	@Override
	public void onTransferTemporaryError(MegaApiJava api,
			MegaTransfer transfer, MegaError e) {
		logger.warn("Transfer temporary error (" + transfer.getFileName()
		+ "); Error: " + e.toString());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaTransferListenerInterface#onTransferData(nz.mega.sdk.MegaApiJava, nz.mega.sdk.MegaTransfer, byte[])
	 */
	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer,
			byte[] buffer) {
		logger.info("Got transfer data.");
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaGlobalListenerInterface#onUsersUpdate(nz.mega.sdk.MegaApiJava, java.util.ArrayList)
	 */
	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaGlobalListenerInterface#onNodesUpdate(nz.mega.sdk.MegaApiJava, java.util.ArrayList)
	 */
	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> nodes) {
		if (nodes != null) {
			logger.info("Nodes updated (" + nodes.size() + ")");
		}

		// Signal the other thread we're done.
		synchronized(this.continueEvent){
			this.wasSignalled = true;
			this.continueEvent.notify();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaGlobalListenerInterface#onContactRequestsUpdate(nz.mega.sdk.MegaApiJava, java.util.ArrayList)
	 */
	@Override
	public void onContactRequestsUpdate(MegaApiJava api, ArrayList<MegaContactRequest> contactRequests) {
		if (contactRequests != null && contactRequests.size() != 0) {
			logger.info("Contact request received (" + contactRequests.size() + ")");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaGlobalListenerInterface#onAccountUpdate(nz.mega.sdk.MegaApiJava)
	 */
	@Override
	public void onAccountUpdate(MegaApiJava api) {
		logger.info("Account updated");
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see nz.mega.sdk.MegaGlobalListenerInterface#onReloadNeeded(nz.mega.sdk.MegaApiJava)
	 */
	@Override
	public void onReloadNeeded(MegaApiJava api) {
		// TODO Auto-generated method stub

	}


	public static String getCodEsito() {
		return codEsito;
	}


	public static void setCodEsito(String codEsito) {
		CoreMegaWs.codEsito = codEsito;
	}


	public static String getErrorMessage() {
		return errorMessage;
	}


	public static void setErrorMessage(String errorMessage) {
		CoreMegaWs.errorMessage = errorMessage;
	}


	public static HashMap<Integer, String> getListModel() {
		return listModel;
	}


	public static void setListModel(HashMap<Integer, String> listModel) {
		CoreMegaWs.listModel = listModel;
	}




}
