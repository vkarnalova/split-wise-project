package bg.sofia.uni.fmi.mjt.splitwise.client.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bg.sofia.uni.fmi.mjt.splitwise.client.exceptions.LoggingInClientException;

public class LogFileClientHandler {
	private static final String LOG_FILE_NOT_CREATED = "Log file with errors could not be created.";
	private static final String LOG_FILE_NOT_CLOSED = "Log file with errors could not be closed.";
	private static final String PROBLEM_WHILE_TRYING_TO_WRITE = "Problem while trying to write in log file.";
	private final static String LOG_FILES_PATH = "resources" + File.separator + "client-logs.txt";

	public static void log(String message) {
		if (message.isEmpty()) {
			return;
		}
		Path path = Paths.get(LOG_FILES_PATH);
		if (Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				System.out.println(LOG_FILE_NOT_CREATED);
				throw new LoggingInClientException(LOG_FILE_NOT_CREATED, e);
			}
		}

		try (Writer writer = new FileWriter(path.toAbsolutePath().toString(), true)) {
			log(message, writer);
		} catch (IOException e) {
			System.out.println(LOG_FILE_NOT_CLOSED);
			throw new LoggingInClientException(LOG_FILE_NOT_CLOSED, e);
		}
	}

	public static void log(String payment, Writer writer) {
		try {
			writer.write(payment);
			writer.flush();
		} catch (IOException e) {
			System.out.println(PROBLEM_WHILE_TRYING_TO_WRITE);
			throw new LoggingInClientException(PROBLEM_WHILE_TRYING_TO_WRITE, e);
		}
	}

	public static String getLogFilesAbsolutePath() {
		return Paths.get(LOG_FILES_PATH).toAbsolutePath().toString();
	}
}
