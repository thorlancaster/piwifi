import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandLine {

	CommandLine() throws InstantiationException {
		throw new InstantiationException(
				"Cannot instantiate class CommandLine");
	}

	public static String exec(String cmd) {
		String s = null;
		try {
			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			// read the output from the command
			System.out.println("STDOUT:");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out.println("STDERR:");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
}