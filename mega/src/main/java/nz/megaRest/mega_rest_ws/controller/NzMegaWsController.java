package nz.megaRest.mega_rest_ws.controller;




import java.io.InputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferListenerInterface;
import nz.megaRest.core.CoreMegaWs;
import nz.megaRest.core.CoreStreamingMegaWs;
import nz.megaRest.mega_rest_ws.model.LoginNzResponse;

@RestController
public class NzMegaWsController implements MegaTransferListenerInterface{
	public static final Logger logger = LogManager.getLogger(NzMegaWsController.class);
	public static byte[] buffering;
	/** Condition variable to signal asynchronous continuation. */
	public Object continueEvent = null;
	/** Signal for condition variable to indicate signalling. */
	public boolean wasSignalled = false ;
	@Resource CoreMegaWs coreMega;
	@Resource CoreStreamingMegaWs coreStreamingMega;
	@RequestMapping("/login")
	public LoginNzResponse login (@RequestParam(value="email") String email, 
			@RequestParam(value="password") String password) {
		   LoginNzResponse response = null;
			try {
				
				response = coreMega.login( email,password );								
			}catch(Exception exc) {
				response = new LoginNzResponse("999", "KO: "+ exc);
			}
			logger.info(response);
			return response;
	}
	
	@RequestMapping("/streaming")
	public LoginNzResponse streaming (@RequestParam(value="email") String email, 
			@RequestParam(value="password") String password,
			@RequestParam(value="nodeIndex") int nodeIndex,
			@RequestParam(value="nodeName") String nodeName) {
		   LoginNzResponse response = new LoginNzResponse("", "");
			try {
				response = coreStreamingMega.initStreaming( email,password,nodeIndex,nodeName );								
			}catch(Exception exc) {
				response = new LoginNzResponse("999", "KO: "+ exc);
			}
			logger.info(response);
			return response;
	}
	
	@RequestMapping("/download")
	public LoginNzResponse download (@RequestParam(value="email") String email, 
			@RequestParam(value="password") String password,
			@RequestParam(value="nodeIndex") int nodeIndex,
			@RequestParam(value="nodeName") String nodeName) {
		   LoginNzResponse response = new LoginNzResponse("", "");
			try {
				response = coreStreamingMega.initDownload( email,password,nodeIndex,nodeName );								
			}catch(Exception exc) {
				response = new LoginNzResponse("999", "KO: "+ exc);
			}
			logger.info(response);
			return response;
	}

	
	
	@RequestMapping("/play")
	public LoginNzResponse play (@RequestParam(value="email") String email, 
			@RequestParam(value="password") String password,
			@RequestParam(value="nodeIndex") int nodeIndex,
			@RequestParam(value="nodeName") String nodeName) {
		    LoginNzResponse param = null;
		    LoginNzResponse response = null;

			try {
				param = coreStreamingMega.login( email,password,nodeIndex,nodeName);			
				CoreStreamingMegaWs mylistener = playStreaming(param.getMegaApi(),param.getTemp(),param.getMyListener());
				coreStreamingMega.logout(mylistener,param.getMegaApi());
				response = new LoginNzResponse("200", "OK");
			}catch(Exception exc) {
				response = new LoginNzResponse("999", "KO: "+ exc);
			}
			logger.info(response);
			return response;
	}
	
	
	public CoreStreamingMegaWs playStreaming(MegaApiJava megaApi,MegaNode temp,CoreStreamingMegaWs myListener) {
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
	   	return myListener;
	   }
		
	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		// TODO Auto-generated method stub
		logger.info("Transfer start: " + transfer.getFileName());
	}

	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		// TODO Auto-generated method stub
		logger.info("Transfer finished (" + transfer.getFileName()
		+ "); Result: " + e.toString() + " ");
		 // Signal the other thread we're done.
        synchronized(this.continueEvent){
            this.wasSignalled = true;
            this.continueEvent.notify();
        }
	}

	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {
		// TODO Auto-generated method stub
		logger.info("Transfer update (" + transfer.getFileName()
		+ "): " + transfer.getSpeed() + " B/s ");		
		logger.info(buffering);
		
		
		
	}

	@Override
	public void onTransferTemporaryError(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer, byte[] buffer) {
		// TODO Auto-generated method stub
		logger.info("Got transfer data.");
		setBuffering(buffer);
		return true;
	}
	
	public static byte[] getBuffering() {
		return buffering;
	}

	public static void setBuffering(byte[] buffering) {
		NzMegaWsController.buffering = buffering;
	}

	
	
}
