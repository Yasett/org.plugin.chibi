
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
public class Snippet {

	public static void FileWrite() {

		try {

			FileOutputStream fileOutputStream = new FileOutputStream("File.ser");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

			try {
				objectOutputStream.writeObject("data");
			} finally {
				objectOutputStream.close();
			}

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}
	
	public void testFrame(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
		JFrame frame = new JFrame("Test title", gc);
		
	}
	
	// public static void main(String[] args) {
	// FileWrite();
	// }
	//
	// public void loopHashTable(){
	// Hashtable<String,Integer> h = new Hashtable<String,Integer>(20);
	// Enumeration<String> en = h.keys();
	// while (en.hasMoreElements()) {
	// Object object = (Object) en.nextElement();
	// System.out.println(object);
	// }
	// }
	//
	// public void loopHashTableAlt(){
	// Hashtable<String,Integer> h = new Hashtable<String,Integer>(20);
	// Set<Entry<String, Integer>> en = h.entrySet();
	//
	// for (Entry<String, Integer> entry : en) {
	// Integer value = entry.getValue();
	// String key = entry.getKey();
	// System.out.println(value + key);
	// }
	// }

}
