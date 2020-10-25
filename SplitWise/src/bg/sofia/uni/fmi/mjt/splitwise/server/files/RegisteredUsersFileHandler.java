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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class RegisteredUsersFileHandler {
	private static final String REGISTERED_USERS_FILE_NOT_CREATED = "File with registered users could not be created.";
	private static final String REGISTERED_USERS_FILE_NOT_CLOSED = "File containg the registered users could not be closed.";
	private static final String REGISTERED_USERS_FILE_NOT_OPENED = "File containg the registered users could not be opened.";
	private static final String PROBLEM_WHILE_TRYING_TO_WRITE = "Problem while trying to write in the registered users file.";
	private final static String REGISTERED_USERS_FILE_PATH = "resources" + File.separator + "registeredUsers.txt";
	private static final String USER_INFORMATION_SEPARATOR = ",";
	private static final int USERNAME_INDEX = 0;
	private static final int PASSWORD_INDEX = 1;

	public static Map<String, User> retrieveRegisteredUsers() {
		if (Files.exists(Paths.get(REGISTERED_USERS_FILE_PATH))) {
			try (Reader reader = new FileReader(Paths.get(REGISTERED_USERS_FILE_PATH).toAbsolutePath().toString())) {
				return retreiveRegisteredUsers(reader);

			} catch (FileNotFoundException e) {
				System.out.println(REGISTERED_USERS_FILE_NOT_OPENED);
				LogFileServerHandler.log(REGISTERED_USERS_FILE_NOT_OPENED + e.getStackTrace());
			} catch (IOException e) {
				System.out.println(REGISTERED_USERS_FILE_NOT_CLOSED);
				LogFileServerHandler.log(REGISTERED_USERS_FILE_NOT_CLOSED + e.getStackTrace());
			}
		}

		return new HashMap<>();
	}

	public static Map<String, User> retreiveRegisteredUsers(Reader reader) {
		Map<String, User> loggedUsers = new HashMap<>();
		try (Scanner scanner = new Scanner(new BufferedReader(reader))) {
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				String[] split = line.split(USER_INFORMATION_SEPARATOR);
				String username = split[USERNAME_INDEX].trim();
				String password = split[PASSWORD_INDEX].trim();
				loggedUsers.put(username, new User(username, password));
			}
		}
		return loggedUsers;
	}

	public static void addRegisteredUser(User user) {
		Path path = Paths.get(REGISTERED_USERS_FILE_PATH);
		if (Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				System.out.println(REGISTERED_USERS_FILE_NOT_CREATED);
				LogFileServerHandler.log(REGISTERED_USERS_FILE_NOT_CREATED + e.getStackTrace());
				return;
			}
		}

		try (Writer writer = new FileWriter(path.toAbsolutePath().toString(), true)) {
			addRegisteredUser(user, writer);
		} catch (IOException e) {
			System.out.println(REGISTERED_USERS_FILE_NOT_CLOSED);
			LogFileServerHandler.log(REGISTERED_USERS_FILE_NOT_CLOSED + e.getStackTrace());
		}
	}

	public static void addRegisteredUser(User user, Writer writer) {
		String userInformation = new StringBuilder().append(user.getUsername())
				.append(USER_INFORMATION_SEPARATOR)
				.append(user.getPassword())
				.append(System.lineSeparator())
				.toString();
		try {
			writer.write(userInformation);
			writer.flush();
		} catch (IOException e) {
			System.out.println(PROBLEM_WHILE_TRYING_TO_WRITE);
			LogFileServerHandler.log(PROBLEM_WHILE_TRYING_TO_WRITE + e.getStackTrace());
		}
	}
}
