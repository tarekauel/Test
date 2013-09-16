import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * 
 * @author Tarek Versuch einen eigenen Proxy-Server zu schreiben
 */
public class Proxy {

	// Logger
	private final Logger	log;

	// Port auf dem der Proxy angesprochen werden kann
	private final int		port;

	// ServerSocket des Proxy
	private ServerSocket	sSocket;	
	
	// TCP Socket für die aktuelle Verbindung mit dem Browser
	private Socket			socketIn;

	public static void main(String[] args) throws IOException  {
		Proxy p = null;
		if(args.length > 0 && args[0].equals("install")) {
			install();
		}
		try {
			p = new Proxy(11111);
		} catch (IOException e) {
			System.err.println("IOException beim erstellen des Proxys");
			return;
		}
		int i=0;
		while (true) {

				try {
					Socket sIn = p.connectToBrowser();
					new Thread( new ProxyThread(sIn, i++)).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
		}

	}
	

	public Proxy(int port) throws IOException {
		log = Logger.getLogger(this.getClass().getSimpleName());
		log.addHandler(new FileHandler("C:\\Proxy\\log.txt"));
		log.info("Logger gestartet");
		this.port = port;
		sSocket = new ServerSocket(port);
	}

	public Socket connectToBrowser() throws IOException {

		log.info("Start()");
		return sSocket.accept();
	}
	
	private static void install() throws IOException {
		Runtime.getRuntime().exec("taskkill /IM firefox.exe");
		File f = new File("c:/users/" + System.getProperty("user.name")+"/AppData/Roaming/Mozilla/Firefox/Profiles/");
		File[] fileArray = f.listFiles();
		for(File single: fileArray) {
			String path = "c:/users/" + System.getProperty("user.name")+"/AppData/Roaming/Mozilla/Firefox/Profiles/" + single.getName() + "/prefs.js";
			BufferedReader reader = new BufferedReader( new FileReader( path ));
			String line = reader.readLine();
			ArrayList<String> prefsFile = new ArrayList<String>();
			while(line != null) {
				prefsFile.add(line);
				line = reader.readLine();
			}
			reader.close();
			prefsFile.add("user_pref(\"javascript.enabled\", false);");
			prefsFile.add("user_pref(\"network.proxy.http\", \"127.0.0.1\");");
			prefsFile.add("user_pref(\"network.proxy.http_port\", 11111);");
			prefsFile.add("user_pref(\"network.proxy.type\", 1);");			
		}
	}

	
}
