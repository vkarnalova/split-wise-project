package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.RegularGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class CreateGroupCommand extends Command {
	public static final String NOT_ENOUGH_USERS_MESSAGE = "Not enough users for creating a group. "
			+ "The minimum number of user, that could take part in a group, is 3.";
	public static final String ALL_NOT_REGISTERED_MESSAGE = "You are trying to create a group of not registered users.";
	public static final String CREATED_GROUP_MESSAGE = "You have successfully created a group.";
	public static final String GROUP_WITHOUT_USER_MESSAGE = " The group will be created without them.";
	public static final String CURRENT_USER_ALREADY_IN_GROUP = "You are already in a group with the same name.";
	public static final String ARE_ALREADY_IN_GROUP_SAME_NAME = " are already in a group with the same name."
			+ System.lineSeparator() + "Change the name of the group or create it without them.";

	private static final int MIN_COMMAND_ARGUMENTS_NUMBER = 4;
	private static final int FIRST_GROUP_MEMBER_INDEX = 2;
	private static final int MINIMUM_USERS_IN_GROUP = 3;
	private static final int GROUP_NAME_INDEX = 1;

	public CreateGroupCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		if (!isLoggedIn(socketChannel)) {
			return NOT_LOGGED_IN_USER_MESSAGE;
		}

		String[] split = message.split(COMMAND_ARGUMENTS_SEPARATOR);
		if (split.length < MIN_COMMAND_ARGUMENTS_NUMBER) {
			return INVALID_NUMBER_ARGUMENTS_MESSAGE + HELP_INFORMATION_MESSAGE;
		}

		String username = getLoggedInUsers().get(socketChannel);
		User user = getRegisteredUsers().get(username);

		return createGroup(user, split);
	}

	private String createGroup(User user, String[] split) {
		String groupName = split[GROUP_NAME_INDEX].trim();
		if (user.getGroups().containsKey(groupName)) {
			return CURRENT_USER_ALREADY_IN_GROUP;
		}
		Set<User> usersInGroup = new HashSet<>();
		String message = getUsersInGroup(user, groupName, split, usersInGroup);

		if (usersInGroup.isEmpty()) {
			return message;
		}

		RegularGroup group = new RegularGroup(groupName, usersInGroup);
		addGroupToUsers(usersInGroup, group);

		return message + CREATED_GROUP_MESSAGE;
	}

	private void addGroupToUsers(Set<User> usersInGroup, RegularGroup group) {
		usersInGroup.stream().forEach(user -> user.addGroup(group.getGroupName(), group));
	}

	private String getUsersInGroup(User user, String groupName, String[] split, Set<User> usersInGroup) {
		String message = getRegisteredUsers(split, usersInGroup);

		if (usersInGroup.isEmpty()) {
			return ALL_NOT_REGISTERED_MESSAGE;
		}

		Optional<String> userInGroupWithSameNameMessage = validateNotUserInGroupWithSameName(groupName, usersInGroup);
		if (userInGroupWithSameNameMessage.isPresent()) {
			return userInGroupWithSameNameMessage.get();
		}

		usersInGroup.add(user);
		if (usersInGroup.size() < MINIMUM_USERS_IN_GROUP) {
			usersInGroup.clear();
			return NOT_ENOUGH_USERS_MESSAGE;
		}
		return message;
	}

	private String getRegisteredUsers(String[] split, Set<User> usersInGroup) {
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(split).skip(FIRST_GROUP_MEMBER_INDEX).forEach(friendName -> {
			if (isRegistered(friendName)) {
				usersInGroup.add(getRegisteredUsers().get(friendName));
			} else {
				stringBuilder.append(
						friendName + NOT_REGISTERED_USER_MESSAGE + GROUP_WITHOUT_USER_MESSAGE + System.lineSeparator());
			}
		});

		return stringBuilder.toString();
	}

	private Optional<String> validateNotUserInGroupWithSameName(String groupName, Set<User> usersInGroup) {
		List<User> userInGroupWithSameName = usersInGroup.stream()
				.filter(u -> u.getGroups().get(groupName) != null)
				.collect(Collectors.toList());
		if (userInGroupWithSameName.size() > 0) {
			String names = userInGroupWithSameName.stream().map(User::getUsername).collect(Collectors.joining(", "));
			usersInGroup.clear();
			return Optional.of(names + ARE_ALREADY_IN_GROUP_SAME_NAME);
		}
		return Optional.empty();
	}

}
