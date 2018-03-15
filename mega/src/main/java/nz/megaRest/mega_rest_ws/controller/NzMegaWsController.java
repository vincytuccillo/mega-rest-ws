package nz.megaRest.mega_rest_ws.controller;




import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nz.megaRest.core.CoreMegaWs;
import nz.megaRest.core.CoreStreamingMegaWs;
import nz.megaRest.mega_rest_ws.model.LoginNzResponse;

@RestController
public class NzMegaWsController {
	static final Logger logger = LogManager.getLogger(NzMegaWsController.class);
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
	
	
	
	
	
}
