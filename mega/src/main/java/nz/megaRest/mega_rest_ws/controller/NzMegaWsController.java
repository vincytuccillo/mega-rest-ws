package nz.megaRest.mega_rest_ws.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import nz.megaRest.core.CoreMegaWs;
import nz.megaRest.core.CoreStreamingMegaWs;
import nz.megaRest.mega_rest_ws.model.LoginNzResponse;
import nz.megaRest.mega_rest_ws.model.MediaStreamer;

@RestController
public class NzMegaWsController {
	public static final Logger logger = LogManager.getLogger(NzMegaWsController.class);
	public static byte[] buffering;
	/** Condition variable to signal asynchronous continuation. */
	public Object continueEvent = null;
	/** Signal for condition variable to indicate signalling. */
	public boolean wasSignalled = false;
	public LoginNzResponse result = null;
	private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
	private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;
	private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
	private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
	private static final int DEFAULT_BUFFER_SIZE = 204800;
	private static final int BUFFER_LENGTH = 1024 * 2;

	@Resource
	CoreMegaWs coreMega;
	@Resource
	CoreStreamingMegaWs coreStreamingMega;

	@RequestMapping("/login")
	public LoginNzResponse login(@RequestParam(value = "email") String email,
			@RequestParam(value = "password") String password) {
		LoginNzResponse response = null;
		try {
			response = coreMega.login(email, password);
		} catch (Exception exc) {
			response = new LoginNzResponse("999", "KO: " + exc);
		}
		logger.info(response);
		return response;
	}

	@RequestMapping("/streaming")
	public LoginNzResponse streaming(@RequestParam(value = "email") String email,
			@RequestParam(value = "password") String password, @RequestParam(value = "nodeIndex") int nodeIndex,
			@RequestParam(value = "nodeName") String nodeName) {
		LoginNzResponse response = new LoginNzResponse("", "");
		try {
			response = coreStreamingMega.initStreaming(email, password, nodeIndex, nodeName);
		} catch (Exception exc) {
			response = new LoginNzResponse("999", "KO: " + exc);
		}
		logger.info(response);
		return response;
	}

	@RequestMapping("/Asyncdownload")
	public LoginNzResponse download(@RequestParam(value = "email") String email,
			@RequestParam(value = "password") String password, @RequestParam(value = "nodeIndex") int nodeIndex,
			@RequestParam(value = "nodeName") String nodeName) {
		LoginNzResponse response = new LoginNzResponse("", "");
		try {
			response = coreStreamingMega.initDownload(email, password, nodeIndex, nodeName);
		} catch (Exception exc) {
			response = new LoginNzResponse("999", "KO: " + exc);
		}
		logger.info(response);
		return response;
	}

	@RequestMapping(value = "download", method = RequestMethod.GET)
	public StreamingResponseBody getSteamingFile(HttpServletResponse response) throws IOException {
		response.setContentType("video/mp4");
		response.setHeader("Content-Disposition", "attachment; filename=\"videoplayback.ts\"");
		@SuppressWarnings("resource")
		InputStream inputStream = new FileInputStream(
				new File("C:\\Users\\VMWin7Starter\\Downloads\\videoplayback.mp4"));
		return outputStream -> {
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				outputStream.write(data, 0, nRead);
			}
		};
	}

	@RequestMapping("/playStaticVideo")
	public void getVideo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

		try {
			Path path = FileSystems.getDefault().getPath("C:\\Users\\VMWin7Starter\\Downloads\\videoplayback.mp4");
			int length = (int) Files.size(path);
			int start = 0;
			int end = length - 1;
			String range = httpServletRequest.getHeader("Range");
			Matcher matcher = RANGE_PATTERN.matcher(range);
			if (matcher.matches()) {
				String startGroup = matcher.group("start");
				start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
				start = start < 0 ? 0 : start;
				String endGroup = matcher.group("end");
				end = endGroup.isEmpty() ? end : Integer.valueOf(endGroup);
				end = end > length - 1 ? length - 1 : end;
			}
			int contentLength = end - start + 1;
			httpServletResponse.reset();
			httpServletResponse.setBufferSize(BUFFER_LENGTH);
			httpServletResponse.setHeader("Content-Disposition",
					String.format("inline;filename=\"%s\"", path.getFileName()));
			httpServletResponse.setHeader("Accept-Ranges", "bytes");
			httpServletResponse.setDateHeader("Last-Modified", Files.getLastModifiedTime(path).toMillis());
			httpServletResponse.setDateHeader("Expires", System.currentTimeMillis() + EXPIRE_TIME);
			httpServletResponse.setContentType("video/mp4");
			httpServletResponse.addHeader("Connection", "Keep-Alive");
			httpServletResponse.setHeader("Content-Range", String.format("bytes %s-%s/%s", start, end, length));
			httpServletResponse.setHeader("Content-Length", String.format("%s", contentLength));
			httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			int bytesRead = 0;
			int bytesLeft = contentLength;
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
			SeekableByteChannel input = Files.newByteChannel(path, StandardOpenOption.READ);
			OutputStream output = httpServletResponse.getOutputStream();
			input.position(start);
			while ((bytesRead = input.read(buffer)) != -1 && bytesLeft > 0) {
				buffer.clear();
				output.write(buffer.array(), 0, bytesLeft < bytesRead ? bytesLeft : bytesRead);
				output.flush();
				bytesLeft = bytesLeft - bytesRead;
			}
		} catch (Exception e) {
			logger.info("Connessione interrotta dall' utente");
		}
	}

	@RequestMapping("/playVideo")
	public void play(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

		try {

			String range = httpServletRequest.getHeader("Range");
			String email = httpServletRequest.getParameter("email");
			String password = httpServletRequest.getParameter("password");
			int nodeIndex = Integer.parseInt(httpServletRequest.getParameter("nodeIndex"));
			String nodeName = httpServletRequest.getParameter("nodeName");
			result = coreStreamingMega.login(email, password, nodeIndex, nodeName);
			int length = (int) result.getTemp().getSize();
			int start = 0;
			int end = length - 1;
			Matcher matcher = RANGE_PATTERN.matcher(range);
			if (matcher.matches()) {
				String startGroup = matcher.group("start");
				start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
				start = start < 0 ? 0 : start;
				String endGroup = matcher.group("end");
				end = endGroup.isEmpty() ? end : Integer.valueOf(endGroup);
				end = end > length - 1 ? length - 1 : end;
			}
			int contentLength = end - start + 1;

			result = coreStreamingMega.initPlayStreaming(email, password, result.getMyListener(), nodeIndex, nodeName,
					start, end);
			httpServletResponse.reset();
			httpServletResponse.setHeader("Accept-Ranges", "bytes");
			httpServletResponse.setDateHeader("Last-Modified", System.currentTimeMillis());
			httpServletResponse.setDateHeader("Expires", System.currentTimeMillis() + EXPIRE_TIME);
			httpServletResponse.setContentType("video/mp4");
			httpServletResponse.addHeader("Connection", "Keep-Alive");
			httpServletResponse.setHeader("Content-Range", String.format("bytes %s-%s/%s", start, end, length));
			httpServletResponse.setHeader("Content-Length", String.format("%s", contentLength));
			httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
			httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			int bytesRead = 0;
			int bytesLeft = contentLength;
			byte[] buffer = new byte[BUFFER_LENGTH];
			ByteArrayInputStream input = new ByteArrayInputStream(result.getBos().toByteArray());
			OutputStream output = httpServletResponse.getOutputStream();
			while ((bytesRead = input.read(buffer)) != -1 && bytesLeft > 0) {
				output.write(buffer, 0, bytesLeft < bytesRead ? bytesLeft : bytesRead);
				output.flush();
				bytesLeft = bytesLeft - bytesRead;
			}
		} catch (Exception e) {
			logger.info("Connessione interrotta dall' utente : " + e);

		}
	}

	@RequestMapping("/play")
	public ResponseEntity<byte[]> play(@RequestParam(value = "email") String email,
			@RequestParam(value = "password") String password, @RequestParam(value = "nodeIndex") int nodeIndex,
			@RequestParam(value = "nodeName") String nodeName, @RequestParam(value = "callNumber") int callNumber) {

		LoginNzResponse result = null;
		ResponseEntity<byte[]> response;
		HttpHeaders headers = null;
		result = coreStreamingMega.login(email, password, nodeIndex, nodeName);
		byte[] buf = null;
		long sizeChunk = 50000000;

		long fileSize = result.getTemp().getSize();
		long numChunk = (long) Math.ceil(fileSize / sizeChunk);
		long startPos = (callNumber - 1) * (sizeChunk);
		long endPos = (callNumber - 1) * sizeChunk + (fileSize - sizeChunk);
		buf = null;
		System.out.println("Call Number :" + callNumber + " - " + "Numero Chunks :" + numChunk);
		result = coreStreamingMega.initPlayStreaming(email, password, result.getMyListener(), nodeIndex, nodeName,
				// startPos, endPos
				0, fileSize);
		buf = result.getBos().toByteArray();
		headers = new HttpHeaders();
		headers.add("Content-Range", "bytes" + startPos + "-" + endPos + "/" + fileSize);
		headers.add("Accept-Ranges", "bytes");
		headers.add("Content-Length", String.valueOf(sizeChunk));

		headers.add("Numero Chunk", Long.toString(numChunk));
		response = (ResponseEntity<byte[]>) ResponseEntity.ok()

				.contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(fileSize)
				// .headers(headers)
				.body(buf);
		System.out.println(response);
		return response;
	}

	private Response buildStream(final byte[] chunk, final String range) throws Exception {
		// range not requested : Firefox, Opera, IE do not send range headers
		if (range == null) {
			StreamingOutput streamer = new StreamingOutput() {
				@Override
				public void write(final OutputStream output) throws IOException, WebApplicationException {
					final WritableByteChannel outputChannel = Channels.newChannel(output);
					try {
						outputChannel.write(ByteBuffer.wrap(chunk));
					} finally {
						// closing the channels
						outputChannel.close();
					}
				}
			};
			return Response.ok(streamer).header(HttpHeaders.CONTENT_LENGTH, chunk.length).build();
		}

		String[] ranges = range.split("=")[1].split("-");
		final int from = Integer.parseInt(ranges[0]);
		/**
		 * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
		 */
		int to = 5000000 + from;
		if (to >= chunk.length) {
			to = (int) (chunk.length - 1);
		}
		if (ranges.length == 2) {
			to = Integer.parseInt(ranges[1]);
		}

		final String responseRange = String.format("bytes %d-%d/%d", from, to, chunk.length);
		final ByteArrayInputStream bais = new ByteArrayInputStream(chunk);

		final int len = to - from + 1;
		final MediaStreamer streamer = new MediaStreamer(len, bais);
		Response.ResponseBuilder res = Response.status(Status.PARTIAL_CONTENT).entity(streamer)
				.header("Accept-Ranges", "bytes").header("Content-Range", responseRange)
				.header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth()).header(HttpHeaders.LAST_MODIFIED, new Date());
		return res.build();
	}

	public static byte[] getBuffering() {
		return buffering;
	}

	public static void setBuffering(byte[] buffering) {
		NzMegaWsController.buffering = buffering;
	}

	public LoginNzResponse getResult() {
		return result;
	}

	public void setResult(LoginNzResponse result) {
		this.result = result;
	}

}
