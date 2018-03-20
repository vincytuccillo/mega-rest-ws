package nz.megaRest.mega_rest_ws.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class NzMegaWsController{
	public static final Logger logger = LogManager.getLogger(NzMegaWsController.class);
	public static byte[] buffering;
	/** Condition variable to signal asynchronous continuation. */
	public Object continueEvent = null;
	/** Signal for condition variable to indicate signalling. */
	public boolean wasSignalled = false ;
	public static HttpServletResponse response ;
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
	public ResponseEntity<byte[]> play (@RequestParam(value="email") String email, 
			@RequestParam(value="password") String password,
			@RequestParam(value="nodeIndex") int nodeIndex,
			@RequestParam(value="nodeName") String nodeName) {
		byte[] buf = null;
		try {				
				LoginNzResponse result = coreStreamingMega.initPlayStreaming( email,password,nodeIndex,nodeName);
				buf = result.getBos().toByteArray();
				
			}catch(Exception exc) {
				logger.error(exc);
			}
		return ResponseEntity.ok()
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(buf);
			
	}
	

		
	

	
	
	
	public static byte[] getBuffering() {
		return buffering;
	}

	public static void setBuffering(byte[] buffering) {
		NzMegaWsController.buffering = buffering;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		NzMegaWsController.response = response;
	}

	
	
}
