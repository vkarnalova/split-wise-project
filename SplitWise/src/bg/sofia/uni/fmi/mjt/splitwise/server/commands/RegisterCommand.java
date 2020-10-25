package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.files.RegisteredUsersFileHandler;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class RegisterCommand extends Command {
	private static final String USER_EXISTS_MESSAGE = "User with the same name already exists.";
	private static final String SUCCESSFUL_REGISTRATION_MESSAGE = "Successful registration.";

	private static final int USERNAME_INDEX = 1;
	private static final int START_PASSWORD_INDEX = 2;
	private static final int MIN_ARGUMENTS_NUMBER = 3;

	public RegisterCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		String[] split = message.split(COMMAND_ARGUMENTS_SEPARATOR);
		if (split.length < MIN_ARGUMENTS_NUMBER) {
			return INVALID_NUMBER_ARGUMENTS_MESSAGE;
		}

		String username = split[USERNAME_INDEX].trim();

		String password = getPassword(split);

		if (isRegistered(username)) {
			return USER_EXISTS_MESSAGE;
		}

		User newUser = new User(username, password);
		getRegisteredUsers().put(username, newUser);
		RegisteredUsersFileHandler.addRegisteredUser(newUser);

		return SUCCESSFUL_REGISTRATION_MESSAGE;
	}

	/**
	 * Retrieves the password from the split message. Space is considered a valid
	 * password character.
	 * 
	 * @param split The split command.
	 * @return The password.
	 */
	private String getPassword(String[] split) {
		return Arrays.stream(split).skip(START_PASSWORD_INDEX).collect(Collectors.joining(COMMAND_ARGUMENTS_SEPARATOR));
	}

}
