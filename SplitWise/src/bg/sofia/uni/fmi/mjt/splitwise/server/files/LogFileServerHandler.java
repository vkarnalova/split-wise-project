package bg.sofia.uni.fmi.mjt.splitwise.server.files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.LoggingInServerException;

public class LogFileServerHandler {
	private static final String LOG_FILE_NOT_CREATED = "Log file with errors could not be created.";
	private static final String LOG_FILE_NOT_CLOSED = "Log file with errors could not be closed.";
	private static final String PROBLEM_WHILE_TRYING_TO_WRITE = "Problem while trying to write in log file.";
	private final static String LOG_FILES_DIRECTORY_PATH = "resources" + File.separator + "server-logs.txt";

	public static void log(String message) {
		if (message.isEmpty()) {
			return;
		}
		Path path = Paths.get(LOG_FILES_DIRECTORY_PATH);
		if (Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				System.out.println(LOG_FILE_NOT_CREATED);
				throw new LoggingInServerException(LOG_FILE_NOT_CREATED, e);
			}
		}

		try (Writer writer = new FileWriter(path.toAbsolutePath().toString(), true)) {
			log(message, writer);
		} catch (IOException e) {
			System.out.println(LOG_FILE_NOT_CLOSED);
			throw new LoggingInServerException(LOG_FILE_NOT_CLOSED, e);
		}
	}

	public static void log(String message, Writer writer) {
		try {
			writer.write(message + System.lineSeparator());
			writer.flush();
		} catch (IOException e) {
			System.out.println(PROBLEM_WHILE_TRYING_TO_WRITE);
			throw new LoggingInServerException(PROBLEM_WHILE_TRYING_TO_WRITE, e);
		}
	}
}
