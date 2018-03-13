package parties.in4010.q12015.group7;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger to get detailed log information in a log-file even when tournaments
 * are run
 * 
 * @author svanbekhoven
 *
 */
public class Logger {
	private PrintWriter writer;
	private boolean print;

	public Logger(boolean print) {
		this.print = print;

		if (this.print) {
			try {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMdd_HHmmdss.SSS");
				Date date = new Date();
				writer = new PrintWriter("logs/manual_"
						+ dateFormat.format(date) + ".txt", "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void log(Object log) {
		if (this.print) {
			writer.print(log);
			writer.flush();
		}
	}

	public void logln(Object log) {
		if (this.print) {
			writer.println(log);
			writer.flush();
		}
	}
}