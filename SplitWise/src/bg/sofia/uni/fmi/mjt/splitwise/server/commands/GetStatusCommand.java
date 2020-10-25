package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class GetStatusCommand extends Command {
	public static final String NO_OBLIGATIONS_FOUND_MESSAGE = "Currently there are no obligations to be shown.";

	private static final String FRIENDS = "Friends:";
	private static final String GROUPS = "Groups:";
	private static final int ONE = 1;

	public GetStatusCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		if (!isLoggedIn(socketChannel)) {
			return NOT_LOGGED_IN_USER_MESSAGE;
		}

		String username = getLoggedInUsers().get(socketChannel);
		User user = getRegisteredUsers().get(username);
		List<String> statuses = getFriendsStatus(user);
		statuses.addAll(getGroupsStatus(user));

		if (statuses.isEmpty()) {
			return NO_OBLIGATIONS_FOUND_MESSAGE;
		}

		return statuses.stream().collect(Collectors.joining(System.lineSeparator()));
	}

	private List<String> getFriendsStatus(User user) {
		List<String> statuses = new ArrayList<>();
		statuses.add(FRIENDS);
		user.getFriends().entrySet().stream().forEach(entry -> {
			List<String> friendStatus = entry.getValue().getStatus(user);
			if (!friendStatus.isEmpty()) {
				statuses.addAll(friendStatus);
			}
		});

		return statuses.size() == ONE ? new ArrayList<>() : statuses;
	}

	private List<String> getGroupsStatus(User user) {
		List<String> statuses = new ArrayList<>();
		statuses.add(GROUPS);
		user.getGroups().values().stream().forEach(group -> {
			List<String> groupStatus = group.getStatus(user);
			if (!groupStatus.isEmpty()) {
				statuses.add(group.getGroupName());
				statuses.addAll(groupStatus);
			}
		});

		return statuses.size() == ONE ? new ArrayList<>() : statuses;
	}

}
