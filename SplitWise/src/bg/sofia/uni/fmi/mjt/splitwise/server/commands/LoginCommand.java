package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class LoginCommand extends Command {
	private static final String INCORRECT_PASSWORD_MESSAGE = "You have entered an incorrect password.";
	private static final String LOGGED_IN_SUCCESSFULLY_MESSAGE = "You have logged in successfully.";
	private static final String NO_NOTIFICATIONS_MESSAGE = "There are no notifications.";
	private static final String ALREADY_LOGGED_IN_MESSAGE = "You are already logged in as ";
	private static final String NOTIFICATIONS_MESSAGE = "*** Notifications ***";

	private static final int USERNAME_INDEX = 1;
	private static final int START_PASSWORD_INDEX = 2;
	private static final int MIN_ARGUMENTS_NUMBER = 3;

	public LoginCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		String[] split = message.split(COMMAND_ARGUMENTS_SEPARATOR);
		if (split.length < MIN_ARGUMENTS_NUMBER) {
			return INVALID_NUMBER_ARGUMENTS_MESSAGE + HELP_INFORMATION_MESSAGE;
		}

		String username = split[USERNAME_INDEX].trim();
		String password = getPassword(split);

		if (!isRegistered(username)) {
			return username + NOT_REGISTERED_USER_MESSAGE;
		}

		if (isLoggedIn(socketChannel)) {
			return ALREADY_LOGGED_IN_MESSAGE + getLoggedInUsers().get(socketChannel) + ".";
		}

		if (!getRegisteredUsers().get(username).getPassword().equals(password)) {
			return INCORRECT_PASSWORD_MESSAGE;
		}

		return login(socketChannel, username);
	}

	private String login(SocketChannel socketChannel, String username) {
		getLoggedInUsers().put(socketChannel, username);

		String notifications = getRegisteredUsers().get(username).getNotificationsAsString();
		String messageNotificaions = notifications.isEmpty() ? NO_NOTIFICATIONS_MESSAGE
				: NOTIFICATIONS_MESSAGE + System.lineSeparator() + notifications;

		return LOGGED_IN_SUCCESSFULLY_MESSAGE + System.lineSeparator() + HELP_INFORMATION_MESSAGE
				+ System.lineSeparator() + messageNotificaions;
	}

	private String getPassword(String[] split) {
		return Arrays.stream(split).skip(START_PASSWORD_INDEX).collect(Collectors.joining(COMMAND_ARGUMENTS_SEPARATOR));
	}

}
