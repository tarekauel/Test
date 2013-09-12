import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ProxyServer {

	private final ServerSocket sSocket;
	private Socket socket;
	private BufferedReader reader;
	private OutputStream writer;

	private Socket socketOut;
	private BufferedReader readerOut;
	private PrintWriter writerOut;

	public static void main(String[] args) throws IOException {
		ProxyServer p = new ProxyServer(11111);
		while (true) {
			try {
				p.openConnection();
				ArrayList<String> header = p.readHeader();
				String host = header.get(0).split(" ")[1].substring(7, header.get(0).split(" ")[1].length()-1);
				p.openConnectionOut(host, header);
				p.getAndSendAnswer();
				p.closeConnection();
			} catch(UnknownHostException e) {
				System.out.println("UnknownHost!");			
			} catch (IOException e) {
				e.printStackTrace();
			} catch( IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			
		}

	}

	public ProxyServer(int port) throws IOException {
		sSocket = new ServerSocket(port);
	}

	public void openConnection() throws IOException {
		System.out.println("openConnectio()");
		socket = sSocket.accept();
		socket.setSoTimeout(50);
		reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		writer = socket.getOutputStream();
		System.out.println("EOF()");
	}

	public void closeConnection() throws IOException {
		writer.close();
		reader.close();
		socket.close();
	}

	public void closeServer() throws IOException {
		sSocket.close();
	}

	public ArrayList<String> readHeader() throws IOException {
		System.out.println("read header");
		ArrayList<String> header = new ArrayList<String>();
		try {
			String line = reader.readLine();
			while (line != "" && line != null) {
				header.add(line);
				line = reader.readLine();
			}
		} catch (SocketTimeoutException e) {
			System.out.println("Read Timeout");
		}
		System.out.println("read header finish");
		return header;

	}

	public void openConnectionOut(String host, ArrayList<String> header)
			throws IOException {
		socketOut = new Socket(host, 80);
		writerOut = new PrintWriter(socketOut.getOutputStream());
		for (String line : header) {
			writerOut.println(line);
			System.out.println(line);
		}
		writerOut.flush();
		System.out.println("Anfrage abgeschickt!");
	}

	public void getAndSendAnswer() throws IOException {
		InputStream readerOut =	socketOut.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte buffer[] = new byte[1024];
		for(int s; (s=readerOut.read(buffer)) != -1; )
		{
		  baos.write(buffer, 0, s);
		}
		
		byte[] result = baos.toByteArray();
		writer.write(result);
		writer.flush();

	}
}
