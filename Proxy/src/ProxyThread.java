import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ProxyThread extends Thread {

	// Logger
	private final Logger	log;

	// Threadnumver

	private final int		id;

	// TCP Socket für die aktuelle Verbindung mit dem Browser
	private Socket			socketIn;

	// Timeout für SocketIn
	private final int		timeoutIn		= 100;

	// Ein und Ausgabestreams für die Verbindung mit dem Browser
	private InputStream		readerIn;
	private OutputStream	writerIn;

	// Byte Array mit der Anfrage vom Browser
	private byte[]			request;

	// TCP Socket für die aktuelle Verbindung mit dem Server
	private Socket			socketOut;

	// Timeout für SocketOut Default
	private final int		timeoutOutDef	= 500;

	// Timeout für SocketOut Maximal
	private final int		timeoutOutMax	= 2000;

	// Ein und Ausgabestreams für die Verbindung mit dem Server
	private InputStream		readerOut;
	private OutputStream	writerOut;

	// Byte Array mit der Antwort vom Server
	private byte[]			answer;

	public ProxyThread(Socket socketIn, int id) throws IOException {
		this.socketIn = socketIn;
		socketIn.setSoTimeout(timeoutIn);
		this.id = id;
		log = Logger.getLogger(this.getClass().getSimpleName());
		log.addHandler(new FileHandler("C:\\Proxy\\log-" + id + ".txt", false));
		log.info("Logger gestartet");
	}

	@Override
	public void run() {
		try {
			setup();
			readRequest();
			connectToServer();
			sendRequest();
			readAnswer();
			closeConnectionToServer();
			sendAnswer();
			closeConnectionToBrowser();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHost!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} finally {
			stop();
		}

	}

	public void setup() throws IOException {
		log.info("Verbindung herrgestellt");
		readerIn = socketIn.getInputStream();
		writerIn = socketIn.getOutputStream();
		log.info("Streams erzeugt");
	}

	public void closeConnectionToBrowser() throws IOException {
		readerIn.close();
		writerIn.close();
		socketIn.close();
		log.info("Browserverbindung beendet");
	}

	public void readRequest() throws IOException {
		log.info("readRequest()");

		// Buffer zum Stückweise einlesen des Streams
		byte[] buffer = new byte[1024 * 1024];

		// Helper Stream
		ByteArrayOutputStream helperStream = new ByteArrayOutputStream();

		try {
			// InputStream kb-weise einlesen
			for (int s; (s = readerIn.read(buffer)) != -1;) {
				helperStream.write(buffer, 0, s);
			}
		} catch (SocketTimeoutException e) {
			log.info("Timeout hat lesen beendet");
		}
		helperStream.flush();

		// Helper Stream in ByteArray umwandeln
		byte[] result = helperStream.toByteArray();

		log.info("Byte-Array aus Inputstream erzeugt");
		log.info("Inhalt: \n" + new String(result, 0, result.length));

		request = result;

		FileWriter spy = new FileWriter("C:\\Proxy\\request-" + id + ".txt");
		String[] spyLines = new String(result, 0, result.length).split("\n");
		for (String singleLine : spyLines) {
			spy.write(singleLine + "\n");
		}
		spy.close();
		
		String contentEncoding = getContentEncoding(result);
		contentEncoding = (contentEncoding == null) ? getContentType(result) : contentEncoding;
		contentEncoding = (contentEncoding == null) ? "body.txt" : contentEncoding;
		if (contentEncoding != null) {
			contentEncoding = (contentEncoding.equals("gzip")) ? "zip" : contentEncoding;
			contentEncoding = (contentEncoding.equals("x-icon")) ? "ico" : contentEncoding;
			saveBodyToFile(result, getContentLength(result), contentEncoding, "request-body");
		}

	}

	public void connectToServer() throws UnknownHostException, IOException {
		String host = getHost();
		socketOut = new Socket(host, 80);
		socketOut.setSoTimeout(timeoutOutDef);
		writerOut = socketOut.getOutputStream();
		readerOut = socketOut.getInputStream();
		log.info("Verbindung zum Server herrgestellt");
	}

	public void closeConnectionToServer() throws IOException {
		readerOut.close();
		writerOut.close();
		socketOut.close();
		log.info("Serververbindung beendet");
	}

	public String getHost() throws IllegalArgumentException {
		log.info("Versuche Host auszulesen");
		String request = new String(this.request, 0, this.request.length);
		String requestInLines[] = request.split("\n");
		for (String line : requestInLines) {
			if (line.length() >= 4 && line.substring(0, 4).toUpperCase().equals("HOST")) {
				String host = line.substring(6, line.length() - 1);
				log.info("Geparster Host: " + host);
				return host;
			}
		}
		throw new IllegalArgumentException("kein Host gefunden!");
	}

	public void sendRequest() throws IllegalArgumentException, IOException {
		writerOut.write(request, 0, request.length);
		log.info("Request gesendet");
	}

	public void readAnswer() throws IOException {
		// Buffer zum Stückweise einlesen des Streams
		byte[] buffer = new byte[1024];

		// Helper Stream
		ByteArrayOutputStream helperStream = new ByteArrayOutputStream();
		int bytesRead = -1;
		try {
			// InputStream kb-weise einlesen
			
			while ((bytesRead = readerOut.read(buffer)) != -1) {
				helperStream.write(buffer, 0, bytesRead);
			}
		} catch (SocketTimeoutException e) {
			log.info("Timeout hat lesen beendet");
			if (socketOut.getSoTimeout() >= timeoutOutMax && bytesRead <= 0) {
				socketOut.setSoTimeout(socketOut.getSoTimeout() + 500);
				log.info("Timeout wurde verlängert");
				readAnswer();
			}
		}

		// Helper Stream in ByteArray umwandeln
		byte[] result = helperStream.toByteArray();

		log.info("Byte-Array aus Inputstream erzeugt");
		log.info("Inhalt: \n" + new String(result, 0, result.length));

		FileWriter spy = new FileWriter("C:\\Proxy\\answer-header-" + id + ".txt");
		String[] spyLines = new String(result, 0, result.length).split("\n");
		for (String singleLine : spyLines) {
			spy.write(singleLine + "\n");
			if (singleLine.split(" ").length < 2) {
				break;
			}
		}
		spy.close();

		String contentEncoding = getContentEncoding(result);
		contentEncoding = (contentEncoding == null) ? getContentType(result) : contentEncoding;
		contentEncoding = (contentEncoding == null) ? "body.txt" : contentEncoding;
		if (contentEncoding != null) {
			contentEncoding = (contentEncoding.equals("gzip")) ? "zip" : contentEncoding;
			contentEncoding = (contentEncoding.equals("x-icon")) ? "ico" : contentEncoding;
			saveBodyToFile(result, getContentLength(result), contentEncoding, "answer-body");
		}
		answer = result;
	}

	public void sendAnswer() throws IOException {
		writerIn.write(answer, 0, answer.length);
		log.info("Antwort gesendet");
	}

	private String getContentEncoding(byte[] data) throws IOException {
		String request = new String(data, 0, data.length);
		String lineArray[] = request.split("\n");
		for (String singleLine : lineArray) {
			singleLine = singleLine.replace("\r", "");
			String parts[] = singleLine.split(" ", 2);
			if (parts[0].equals("Content-Encoding:")) {
				return parts[1];
			}
		}
		return null;
	}

	private String getContentType(byte[] data) throws IOException {
		String request = new String(data, 0, data.length);
		String lineArray[] = request.split("\n");
		for (String singleLine : lineArray) {
			singleLine = singleLine.replace("\r", "");
			String parts[] = singleLine.split(" ", 2);
			if (parts[0].equals("Content-Type:")) {
				String[] tmp = parts[1].split("/", 2);
				if (tmp.length == 2)
					return tmp[1].split(";")[0];
				else
					return null;
			}
		}
		return null;
	}

	private int getContentLength(byte[] data) throws IOException, IllegalArgumentException {
		String request = new String(data, 0, data.length);
		String lineArray[] = request.split("\n");
		int contentLength = 0;
		boolean contentStart = false;
		for (String singleLine : lineArray) {
			String parts[] = singleLine.split(" ", 2);
			if (contentStart) {
				contentLength += (singleLine + "\n").getBytes().length;

			}
			if (parts.length != 2) {
				contentStart = true;
			}

			if (parts[0].equals("Content-Length:")) {
				return Integer.parseInt(parts[1].replace("\r", ""));
			}

		}
		return contentLength - 1;
	}

	private void saveBodyToFile(byte[] data, int contentLength, String type, String praefix) throws IOException {
		if (type.equals("")) {
			type = ".body.txt";
		}
		if (contentLength <= 0) {
			return;
		}
		log.info("Content-Length: " + contentLength + " Gesamter Stream:" + data.length);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("C:\\Proxy\\"+praefix+"-" + id + "."
				+ type));
		bos.write(data, data.length - contentLength, contentLength);
		bos.flush();
		bos.close();
	}

}
