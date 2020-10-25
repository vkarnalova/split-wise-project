package bg.sofia.uni.fmi.mjt.splitwise.server.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class HistoryFileHandler {
	private static final String HISTORY_FILE_NOT_CREATED = "File with the history of payments could not be created. ";
	private static final String HISTORY_FILE_NOT_CLOSED = "File with the history of payments could not be closed. ";
	private static final String HISTORY_FILE_NOT_OPENED = "File with the history of payments could not be opened. ";
	private static final String PROBLEM_WHILE_TRYING_TO_WRITE = "Problem while trying to write in a history file.";
	private final static String HISTORY_FILES_DIRECTORY_PATH = "resources" + File.separator;
	private static final String HISTORY_FILE_PARTIAL_NAME = "-history.txt";

	public static String retrieveHistoryOfPayments(User user) {
		String filePath = HISTORY_FILES_DIRECTORY_PATH + user.getUsername() + HISTORY_FILE_PARTIAL_NAME;
		if (Files.exists(Paths.get(filePath))) {
			try (Reader reader = new FileReader(Paths.get(filePath).toAbsolutePath().toString())) {

				return retrieveHistoryOfPayments(reader);

			} catch (FileNotFoundException e) {
				System.out.println(HISTORY_FILE_NOT_OPENED);
				LogFileServerHandler.log(HISTORY_FILE_NOT_OPENED + e.getStackTrace());
			} catch (IOException e) {
				System.out.println(HISTORY_FILE_NOT_CLOSED);
				LogFileServerHandler.log(HISTORY_FILE_NOT_CLOSED + e.getStackTrace());
			}
		}
		return "";
	}

	public static String retrieveHistoryOfPayments(Reader reader) {
		StringBuilder stringBuilder = new StringBuilder();
		try (Scanner scanner = new Scanner(new BufferedReader(reader))) {
			while (scanner.hasNext()) {
				stringBuilder.append(scanner.nextLine());
				stringBuilder.append(System.lineSeparator());
			}
		}
		return stringBuilder.toString();
	}

	public static void addPayment(User user, String payment) {
		if (payment.isEmpty()) {
			return;
		}
		Path path = Paths.get(HISTORY_FILES_DIRECTORY_PATH + user.getUsername() + HISTORY_FILE_PARTIAL_NAME);
		if (Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				System.out.println(HISTORY_FILE_NOT_CREATED);
				LogFileServerHandler.log(HISTORY_FILE_NOT_CREATED + e.getStackTrace());
				return;
			}
		}

		try (Writer writer = new FileWriter(path.toAbsolutePath().toString(), true)) {
			addPayment(payment, writer);
		} catch (IOException e) {
			System.out.println(HISTORY_FILE_NOT_CLOSED);
			LogFileServerHandler.log(HISTORY_FILE_NOT_CLOSED + e.getStackTrace());
		}
	}

	public static void addPayment(String payment, Writer writer) {
		try {
			writer.write(payment);
			writer.flush();
		} catch (IOException e) {
			System.out.println(PROBLEM_WHILE_TRYING_TO_WRITE);
			LogFileServerHandler.log(PROBLEM_WHILE_TRYING_TO_WRITE + e.getStackTrace());
		}
	}
}
