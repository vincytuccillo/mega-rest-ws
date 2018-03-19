package nz.megaRest.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
public class CoreStreamingMegaWs implements MegaRequestListenerInterface,
MegaTransferListenerInterface,
MegaGlobalListenerInterface {

	/** Reference to the Mega API object. */
	static MegaApiJava megaApi = null;

	/**
	 * Mega SDK application key.
	 * Generate one for free here: https://mega.nz/#sdk
	 */
	public static final String APP_KEY = "YYJwAIRI";

	/** Condition variable to signal asynchronous continuation. */
	public Object continueEvent = null;

	/** Signal for condition variable to indicate signalling. */
	public boolean wasSignalled = false ;

	/** Root node of the logged in account. */
	public MegaNode rootNode = null;

	/** Java logging utility. */
    static final Logger logger = LogManager.getLogger(CoreStreamingMegaWs.class);

    public static final String STR_ERROR_INCORRECT_EMAIL_OR_PWD = "Incorrect email or password";
    public static String codEsito;
    public static String errorMessage;
    public static MegaNode temp ;	
    public static HashMap<Integer, String> listModel;
    public static int nodeIndex;
    public static String nodeName;
    public static byte[] buffering;
	/** Constructor. */
	public CoreStreamingMegaWs() {
		// Make a new condition variable to signal continuation.
		this.continueEvent = new Object();

		// Get reference to Mega API.
		if (megaApi == null) {
			String path = System.getProperty("user.dir");
			megaApi = new MegaApiJava(CoreStreamingMegaWs.APP_KEY, path);
		}
	}
	
	
	
	public void initPlayStreaming(String email, String password,int nodeIndex,String nodeName) {
		// Log in.
		logger.info("*** start: login ***");
		CoreStreamingMegaWs myListener = new CoreStreamingMegaWs();
		
		synchronized(myListener.continueEvent) {
			setNodeIndex(nodeIndex);
			setNodeName(nodeName);
			logger.info("*** indice del nodo ***"+ nodeIndex);
			logger.info("*** nome del nodo ***"+ nodeName);
			megaApi.login(email, password, myListener);
			while (!myListener.wasSignalled) {
				try {
					myListener.continueEvent.wait();
				} catch(InterruptedException e) {
					logger.info("login interrupted: " + e.toString());
				}
			}
			
			 playStreaming(myListener);
			 logout(myListener);
			 myListener.wasSignalled = false;
			
		}
			
	}

	
	
	
	
	public LoginNzResponse initStreaming(String email, String password,int nodeIndex,String nodeName) {
		// Log in.
		logger.info("*** start: login ***");
		CoreStreamingMegaWs myListener = new CoreStreamingMegaWs();
		
		synchronized(myListener.continueEvent) {
			setNodeIndex(nodeIndex);
			setNodeName(nodeName);
			logger.info("*** indice del nodo ***"+ nodeIndex);
			logger.info("*** nome del nodo ***"+ nodeName);
			megaApi.login(email, password, myListener);
			while (!myListener.wasSignalled) {
				try {
					myListener.continueEvent.wait();
				} catch(InterruptedException e) {
					logger.info("login interrupted: " + e.toString());
				}
			}
			
			streaming(myListener);
			logout(myListener);
			myListener.wasSignalled = false;
			 return new LoginNzResponse (codEsito,errorMessage) ;
		}
			
	}
	
	public LoginNzResponse initDownload(String email, String password,int nodeIndex,String nodeName) {
		// Log in.
		logger.info("*** start: login ***");
		CoreStreamingMegaWs myListener = new CoreStreamingMegaWs();
		
		synchronized(myListener.continueEvent) {
			setNodeIndex(nodeIndex);
			setNodeName(nodeName);
			logger.info("*** indice del nodo ***"+ nodeIndex);
			logger.info("*** nome del nodo ***"+ nodeName);
			megaApi.login(email, password, myListener);
			while (!myListener.wasSignalled) {
				try {
					myListener.continueEvent.wait();
				} catch(InterruptedException e) {
					logger.info("login interrupted: " + e.toString());
				}
			}
			
			download(myListener);
			logout(myListener);
			myListener.wasSignalled = false;
			 return new LoginNzResponse (codEsito,errorMessage) ;
		}
			
	}
	
	
	
	// Log in
	public LoginNzResponse login(String email, String password,int nodeIndex,String nodeName) {
		
		logger.info("*** start: login ***");
		CoreStreamingMegaWs myListener = new CoreStreamingMegaWs();
		
		synchronized(myListener.continueEvent) {			
			setNodeIndex(nodeIndex);
			setNodeName(nodeName);
			logger.info("*** indice del nodo ***"+ nodeIndex);
			logger.info("*** nome del nodo ***"+ nodeName);
			megaApi.login(email, password, myListener);
			while (!myListener.wasSignalled) {
				try {
					myListener.continueEvent.wait();
				} catch(InterruptedException e) {
					logger.info("login interrupted: " + e.toString());
				}
			}			
			myListener.wasSignalled = false;
			logger.info("*** done: login ***");
			 return new LoginNzResponse (codEsito,errorMessage,megaApi,temp,myListener) ;
		}
			
	}
	
    // Logout.
    public void logout(CoreStreamingMegaWs myListener) {
	logger.info("*** start: logout ***");
	myListener.wasSignalled = false;
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
	
    // Logout.
    public void logout(CoreStreamingMegaWs myListener, MegaApiJava megaApi) {
	logger.info("*** start: logout ***");
	myListener.wasSignalled = false;
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
    
    
    // Streaming.
    public void streaming(CoreStreamingMegaWs myListener) {
	logger.info("*** start: streaming ***");
	logger.info("*** Streaming ***" + temp.getName());
	myListener.wasSignalled = false;
    synchronized(myListener.continueEvent) {
    	megaApi.startStreaming(temp, 0, temp.getSize(), myListener );  
    	while (!myListener.wasSignalled) {
            try {
                myListener.continueEvent.wait();
            } catch(InterruptedException e) {
                logger.warn("remove interrupted: " + e.toString());
            }
        }
        myListener.wasSignalled = false;
    }
   
    logger.info("*** done: streaming ***");
    
}   
 
    // Play Streaming.
    public void playStreaming(CoreStreamingMegaWs myListener) {
	logger.info("*** start: playStreaming ***");
	logger.info("*** Play Streaming ***" + temp.getName());
	myListener.wasSignalled = false;
    synchronized(myListener.continueEvent) {
    	megaApi.startStreaming(temp, 0, temp.getSize(), myListener );  
    	while (!myListener.wasSignalled) {
            try {
            	myListener.continueEvent.wait();

            } catch(InterruptedException e) {
                logger.warn("remove interrupted: " + e.toString());
            }
        }
        myListener.wasSignalled = false;
    }
   
    logger.info("*** done: streaming ***");
    
}   
    
    
    
    
    // Download a file (read).
    public void download(CoreStreamingMegaWs myListener) {
    logger.info("*** start: download ***");
    myListener.wasSignalled = false;
        synchronized(myListener.continueEvent) {
            megaApi.startDownload(temp, "C:\\log\\pippo.mp4", myListener);
            while (!myListener.wasSignalled) {
                try {
                    myListener.continueEvent.wait();
                } catch(InterruptedException e) {
                    logger.warn("download interrupted: " + e.toString());
                }
            }
            myListener.wasSignalled = false;
        }
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
                for (int i = 0; i < nodes.size(); i++) {                	
                	if(i == nodeIndex) {
                		temp = nodes.get(i);
                		setTemp(temp);
                		break;
                	}
                   }
                               
            }
	 
          setCodEsito (codEsito) ;
  	      setErrorMessage(errorMessage);
  	      logger.info(nodeIndex + temp.getName() );
  	      logger.info(e.getErrorCode()+" - "+ codEsito +" - "+ errorMessage);
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
		logger.info("Transfer update (" + transfer.getFileName()
		+ "): " + transfer.getSpeed() + " B/s ");		
		writeOnFile(buffering);		
		logger.info(buffering);
	}




      public void writeOnFile(byte[] buffering) {
		FileOutputStream fos;
		try {
			
			fos = new FileOutputStream(new File ("C:\\log\\pippo.pdf"), true);
			if(buffering != null) fos.write(buffering);		
			fos.close();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
      
      
     /* public ByteArrayInputStream writeOnStream(byte[] buffering) {
    	 InputStream is;
  		try {
  			
  			is = new InputStream;
  			if(buffering != null) bais.		
  			fos.close();
  			} catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  			}
  		}*/
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
		setBuffering(buffer);
		
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

    public static void printContent(File file) throws Exception {
        System.out.println("Print File Content");
        BufferedReader br = new BufferedReader(new FileReader(file));
 
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
 
        br.close();
    }
    
	public static String getCodEsito() {
		return codEsito;
	}


	public static void setCodEsito(String codEsito) {
		CoreStreamingMegaWs.codEsito = codEsito;
	}


	public static String getErrorMessage() {
		return errorMessage;
	}


	public static void setErrorMessage(String errorMessage) {
		CoreStreamingMegaWs.errorMessage = errorMessage;
	}


	public static HashMap<Integer, String> getListModel() {
		return listModel;
	}


	public static void setListModel(HashMap<Integer, String> listModel) {
		CoreStreamingMegaWs.listModel = listModel;
	}


	public static int getNodeIndex() {
		return nodeIndex;
	}


	public static void setNodeIndex(int nodeIndex) {
		CoreStreamingMegaWs.nodeIndex = nodeIndex;
	}


	public static String getNodeName() {
		return nodeName;
	}


	public static void setNodeName(String nodeName) {
		CoreStreamingMegaWs.nodeName = nodeName;
	}


	public MegaNode getTemp() {
		return temp;
	}


	public void setTemp(MegaNode temp) {
		CoreStreamingMegaWs.temp = temp;
	}


	public byte[] getBuffering() {
		return buffering;
	}


	public void setBuffering(byte[] buffering) {
		CoreStreamingMegaWs.buffering = buffering;
	}




}
