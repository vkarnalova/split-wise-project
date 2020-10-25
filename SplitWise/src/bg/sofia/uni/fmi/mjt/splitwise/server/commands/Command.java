package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public abstract class Command {
	public static final String INVALID_NUMBER_ARGUMENTS_MESSAGE = "The number of arguments for the current command is invalid. ";
	public static final String NOT_LOGGED_IN_USER_MESSAGE = "You are not logged in. "
			+ "You will be able to perform the command after logging in.";
	public static final String HELP_INFORMATION_MESSAGE = "You can use <help> to display a manual of all supported comamnds.";
	public static final String NOT_REGISTERED_USER_MESSAGE = " is not a registered user.";
	public static final String FRIEND_NOT_PRESENT_MESSAGE = " is not in your friend list.";
	public static final String INVALID_AMOUNT_MESSAGE = "Invalid amount of money.";
	public static final String NOTHING_TO_SHOW_MESSAGE = "Nothing to show.";
	public static final String CURENT_STATUS_MESSAGE = "Current status: ";
	public static final String COMMAND_ARGUMENTS_SEPARATOR = " ";

	public static final String PERIOD = ".";

	private static final String DIGITS_REGEX = "\\d+(\\.\\d+)?";
	private static final int DECIMAL_PLACES = 2;

	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;

	public Command(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		this.registeredUsers = registeredUsers;
		this.loggedInUsers = loggedInUsers;
	}

	boolean isRegistered(String username) {
		return registeredUsers.containsKey(username);
	}

	public Map<String, User> getRegisteredUsers() {
		return registeredUsers;
	}

	public Map<SocketChannel, String> getLoggedInUsers() {
		return loggedInUsers;
	}

	boolean isLoggedIn(SocketChannel channel) {
		return loggedInUsers.containsKey(channel);
	}

	boolean isLoggedIn(String userName) {
		return loggedInUsers.values().stream().anyMatch(name -> name.equals(userName));
	}

	/**
	 * Executes the command.
	 * 
	 * @return Message after the execution is done.
	 */
	public abstract String execute(SocketChannel socketChannel, String message);

	/**
	 * Return the money value, represented by a string argument.
	 *
	 * @param moneyString The string.
	 * @return Optional of double money or empty optional if the string does not
	 *         contain valid money representation.
	 */
	public Optional<Double> getMoneyAmount(String moneyString) {
		if (!Pattern.matches(DIGITS_REGEX, moneyString)) {
			return Optional.empty();
		}

		double moneyAmount = Double.valueOf(moneyString).doubleValue();
		if (moneyAmount < 0) {
			return Optional.empty();
		}

		Double roundAmount = BigDecimal.valueOf(moneyAmount)
				.setScale(DECIMAL_PLACES, RoundingMode.HALF_DOWN)
				.doubleValue();
		return Optional.of(roundAmount);
	}
}
