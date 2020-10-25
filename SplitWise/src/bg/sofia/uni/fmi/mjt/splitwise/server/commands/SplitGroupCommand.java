package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.RegularGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class SplitGroupCommand extends Command {
	public static final String INVALID_GROUP_NAME_MESSAGE = "You have entered an invalid group name.";
	public static final String SUCCESSFUL_SPLIT = "Splitted successfully ";
	private static final String BETWEEN_YOU_AND = " between you and ";

	private static final int MONEY_INDEX = 1;
	private static final int GROUP_NAME_INDEX = 2;
	private static final long START_INDEX_REASON = 3;
	private static final int MIN_ARGUMENTS_NUMBER = 3;

	public SplitGroupCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
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

		return splitMoney(user, split);
	}

	private String splitMoney(User user, String[] split) {
		Optional<Double> optionalAmount = getMoneyAmount(split[MONEY_INDEX]);
		if (optionalAmount.isEmpty()) {
			return INVALID_AMOUNT_MESSAGE;
		}

		String groupName = split[GROUP_NAME_INDEX].trim();
		RegularGroup group = user.getGroups().get(groupName);
		if (group == null) {
			return INVALID_GROUP_NAME_MESSAGE;
		}

		List<User> usersToBeSentNotifications = getNotLoggedInUsers(group);
		group.split(user, optionalAmount.get(), getReason(split), usersToBeSentNotifications);

		String statusGroup = user.getStatusGroup(groupName);
		return SUCCESSFUL_SPLIT + optionalAmount.get() + BETWEEN_YOU_AND + groupName + PERIOD + System.lineSeparator()
				+ CURENT_STATUS_MESSAGE + System.lineSeparator()
				+ (statusGroup.isEmpty() ? NOTHING_TO_SHOW_MESSAGE : groupName + System.lineSeparator() + statusGroup);
	}

	private List<User> getNotLoggedInUsers(RegularGroup group) {
		return group.getUsers().stream().filter(user -> !isLoggedIn(user.getUsername())).collect(Collectors.toList());
	}

	private String getReason(String[] split) {
		return Arrays.stream(split).skip(START_INDEX_REASON).collect(Collectors.joining(COMMAND_ARGUMENTS_SEPARATOR));
	}

}
