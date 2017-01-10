package org.plugin.chibi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public class Util {
	public static String readFileContents(String fileName) throws IOException {

		StringBuilder code = new StringBuilder();
		try {
			URL url = new URL(fileName);
			InputStream inputStream = url.openConnection().getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				code.append(inputLine);
				code.append("\n");
			}

			in.close();
			return code.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return code.toString();
	}
	
	public static String readFileContents(InputStream inputStream) throws IOException {
		StringBuilder code = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				code.append(inputLine);
				code.append("\n");
			}

			in.close();
			return code.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return code.toString();
	}
	
	public static Image createImageIcon(String iconName){
		try {
			URL url = new URL("platform:/plugin/org.plugin.chibi/icons/"+iconName);
			InputStream inputStream;
			inputStream = url.openConnection().getInputStream();
			ImageData id = new ImageData(inputStream);
			Image image = new Image(null, id);	
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
