package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.Friend;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class AddFriendCommand extends Command {
	public static final String CANNOT_ADD_YOURSELF_MESSAGE = "You cannot add yourself as your friend :(";
	public static final String ADDED_FRIEND_MESSAGE = " was successfully added to your friends.";
	public static final String FRIEND_IS_PRESENT_MESSAGE = " is already in your friend list.";

	private static final int COMMAND_ARGUMENTS_NUMBER = 2;
	private static final int FRIEND_NAME_ARGUMENT_INDEX = 1;

	public AddFriendCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		String[] split = message.split(COMMAND_ARGUMENTS_SEPARATOR);
		if (split.length < COMMAND_ARGUMENTS_NUMBER) {
			return INVALID_NUMBER_ARGUMENTS_MESSAGE + HELP_INFORMATION_MESSAGE;
		}

		if (!isLoggedIn(socketChannel)) {
			return NOT_LOGGED_IN_USER_MESSAGE;
		}

		String username = getLoggedInUsers().get(socketChannel);
		User user = getRegisteredUsers().get(username);

		return addFriend(user, split);
	}

	/**
	 * Creates a friend relation between two users. A group of two people is
	 * considered a friend relation and it is created with a name
	 * <first-user-name>-<second-user-name>
	 * 
	 * @param user  The user that initiates the creation of the relation.
	 * @param split The split message.
	 * @return The answer from the completed command.
	 */
	private String addFriend(User user, String[] split) {
		String friendName = split[FRIEND_NAME_ARGUMENT_INDEX].trim();
		User friendUser = getRegisteredUsers().get(friendName);
		if (friendUser == null) {
			return friendName + NOT_REGISTERED_USER_MESSAGE;
		}

		if (friendUser.equals(user)) {
			return CANNOT_ADD_YOURSELF_MESSAGE;
		}

		if (user.hasFriend(friendName)) {
			return friendName + FRIEND_IS_PRESENT_MESSAGE;
		}

		addFriend(user, friendUser);

		return friendName + ADDED_FRIEND_MESSAGE;
	}

	private void addFriend(User user, User friendUser) {
		String friendGroupName = user.getUsername() + "-" + friendUser.getUsername();
		Set<User> users = new HashSet<>();
		users.add(user);
		users.add(friendUser);
		Friend friend = new Friend(friendGroupName, users);
		user.addFriend(friendUser.getUsername(), friend);
		friendUser.addFriend(user.getUsername(), friend);
	}

}
