
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class FileOperator {

	FileOperator() throws InstantiationException {
		throw new InstantiationException(
				"Cannot instantiate class FileOperator");
	}

	public static String load(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(name)));
		} catch (IOException e) {
			return null;
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean save(String addr, ArrayList data){
		return save(addr, String.join("\n", data), false);
	}
	public static boolean save(String addr, String data){
		return save(addr, data, false);
	}
	public static boolean append(String addr, String data){
		return save(addr, data, true);
	}
	private static boolean save(String addr, String data, boolean append) {

		try (FileWriter writer = new FileWriter(addr, append)) {
			writer.write(data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}