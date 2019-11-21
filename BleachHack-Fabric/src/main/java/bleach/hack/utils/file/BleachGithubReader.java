package bleach.hack.utils.file;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BleachGithubReader {

	private static URI url;

	static {
		//why java, why do i have to use a static block to set a varable, theres nothing wrong with that url
		try { url = new URI("https://raw.githubusercontent.com/BleachDrinker420/bleachhack-1.14/master/online/");
		} catch (URISyntaxException e) {}
	}

	public static List<String> readFileLines(String file) {
		List<String> st = new ArrayList<>();
		try {
			URL fileUrl = url.resolve(file).toURL();
			Scanner sc = new Scanner(fileUrl.openStream());
			while(sc.hasNextLine()) st.add(sc.nextLine());
			sc.close();
		} catch (IOException e) {}
		return st;
	}
}