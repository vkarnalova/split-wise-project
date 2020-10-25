package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class PayedCommand extends Command {
	private static final String USER_NOT_IN_GROUP_MESSAGE = "You are not in the entered group.";
	private static final String FRIEND_NOT_IN_GROUP = "Your friend is not in the entered group.";
	private static final String CURENT_STATUS_MESSAGE = "Current status: ";

	private static final int FRIEND_PAYMENT_ARGUMENTS_NUMBER = 3;
	private static final int GROUP_PAYMENT_ARGUMENTS_NUMBER = 4;
	private static final int FRIEND_NAME_ARGUMENT_INDEX = 2;
	private static final int GROUP_NAME_ARGUMENT_INDEX = 3;
	private static final int MIN_ARGUMENTS_NUMBER = 3;
	private static final int MONEY_INDEX = 1;

	public PayedCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
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

		return addPayment(user, split);
	}

	private String addPayment(User user, String[] split) {
		String friendName = split[FRIEND_NAME_ARGUMENT_INDEX].trim();
		User friendUser = getRegisteredUsers().get(friendName);
		if (friendUser == null) {
			return friendName + NOT_REGISTERED_USER_MESSAGE;
		}

		if (!user.hasFriend(friendName)) {
			return friendName + FRIEND_NOT_PRESENT_MESSAGE;
		}

		Optional<Double> moneyOptional = getMoneyAmount(split[MONEY_INDEX]);
		if (moneyOptional.isEmpty()) {
			return INVALID_AMOUNT_MESSAGE;
		}

		if (split.length == FRIEND_PAYMENT_ARGUMENTS_NUMBER) {
			return addPaymentFriend(user, friendUser, moneyOptional.get());
		} else if (split.length == GROUP_PAYMENT_ARGUMENTS_NUMBER) {
			return addPaymentGroup(user, friendName, split[GROUP_NAME_ARGUMENT_INDEX], moneyOptional.get());
		}

		return INVALID_NUMBER_ARGUMENTS_MESSAGE;
	}

	private String addPaymentGroup(User user, String friendName, String groupName, double moneyAmount) {
		Group group = user.getGroups().get(groupName);
		if (group == null) {
			return USER_NOT_IN_GROUP_MESSAGE;
		}

		if (!group.hasUser(friendName)) {
			return FRIEND_NOT_IN_GROUP;
		}

		String paymentMessage = user.addPaymentFriendInGroup(getRegisteredUsers().get(friendName), groupName,
				moneyAmount, !isLoggedIn(friendName));
		String statusGroup = user.getStatusGroup(groupName);

		return paymentMessage + System.lineSeparator() + CURENT_STATUS_MESSAGE + System.lineSeparator()
				+ (statusGroup.isEmpty() ? NOTHING_TO_SHOW_MESSAGE : statusGroup);
	}

	private String addPaymentFriend(User user, User friendUser, double moneyAmount) {
		String paymentFriendResultMessage = user.addPaymentFriend(friendUser, moneyAmount,
				!isLoggedIn(friendUser.getUsername()));
		String statusFriend = user.getStatusFriend(friendUser.getUsername());

		return paymentFriendResultMessage + System.lineSeparator() + CURENT_STATUS_MESSAGE + System.lineSeparator()
				+ (statusFriend.isEmpty() ? NOTHING_TO_SHOW_MESSAGE : statusFriend);
	}

}
