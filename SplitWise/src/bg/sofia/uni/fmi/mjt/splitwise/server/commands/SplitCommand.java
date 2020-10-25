package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class SplitCommand extends Command {
	public static final String SUCCESSFUL_SPLIT = "Splitted successfully ";
	private static final String BETWEEN_YOU_AND = " between you and ";

	private static final int FRIEND_NAME_ARGUMENT_INDEX = 2;
	private static final int MIN_ARGUMENTS_NUMBER = 3;
	private static final long START_INDEX_REASON = 3;
	private static final int MONEY_INDEX = 1;

	public SplitCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		if (!isLoggedIn(socketChannel)) {
			return NOT_LOGGED_IN_USER_MESSAGE;
		}

		String username = getLoggedInUsers().get(socketChannel);
		User user = getRegisteredUsers().get(username);

		String[] split = message.split(COMMAND_ARGUMENTS_SEPARATOR);
		if (split.length < MIN_ARGUMENTS_NUMBER) {
			return INVALID_NUMBER_ARGUMENTS_MESSAGE;
		}

		return split(user, split);
	}

	private String split(User user, String[] split) {
		String friendName = split[FRIEND_NAME_ARGUMENT_INDEX].trim();
		User friendUser = getRegisteredUsers().get(friendName);
		if (friendUser == null) {
			return friendName + NOT_REGISTERED_USER_MESSAGE;
		}

		if (!user.hasFriend(friendName)) {
			return friendName + FRIEND_NOT_PRESENT_MESSAGE;
		}

		Optional<Double> optionalAmount = getMoneyAmount(split[MONEY_INDEX]);
		if (optionalAmount.isEmpty()) {
			return INVALID_AMOUNT_MESSAGE;
		}

		String reason = getReason(split);
		user.getFriends()
				.get(friendName)
				.split(user, friendUser, optionalAmount.get(), reason, !isLoggedIn(friendName));

		String statusFriend = user.getStatusFriend(friendName);
		return SUCCESSFUL_SPLIT + optionalAmount.get() + BETWEEN_YOU_AND + friendUser.getUsername() + PERIOD
				+ System.lineSeparator() + CURENT_STATUS_MESSAGE + System.lineSeparator()
				+ (statusFriend.isEmpty() ? NOTHING_TO_SHOW_MESSAGE : statusFriend);
	}

	private String getReason(String[] split) {
		return Arrays.stream(split).skip(START_INDEX_REASON).collect(Collectors.joining(COMMAND_ARGUMENTS_SEPARATOR));
	}

}
