package jastadd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class JastAddModulesFileArgs {
	public static void main(String args[]) {
		final LinkedList<String> fileArgs = new LinkedList<String>();
		if (args.length != 1) {
			System.err.println("Usage: JastAddModulesFileArgs <argsfile>");
		}
		String infile = args[0];
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
			while (r.ready()) {
				String line = r.readLine().trim();
				fileArgs.add(line);
				System.out.println(line);
			}
			Runnable run = new Runnable() {
				public void run() {
					new JastAddModules().compile(fileArgs.toArray(new String[0]));
				}
			};
			new Thread(run).start();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
