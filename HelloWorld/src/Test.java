import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.HashMap;

public class Test {
	
	private static final HashMap<String, Integer> mapping = new HashMap<String, Integer>();
	
	public static void main(String[] args) throws MalformedURLException, UnsupportedEncodingException {
		
		String input = URLDecoder.decode("http://de.wikipedia.org/wiki/Fuchsschwanzgew%C3%A4chse", "UTF-8");
		System.out.println(input);
		
		
	}
}
