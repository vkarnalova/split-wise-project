package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.IllegalCommandTypeException;
import bg.sofia.uni.fmi.mjt.splitwise.server.files.LogFileServerHandler;
import bg.sofia.uni.fmi.mjt.splitwise.server.files.RegisteredUsersFileHandler;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class CommandFactory {

	private static final String ILLEGAL_COMMAND_TYPE = "Trying to create command with invalid type.";

	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;

	public CommandFactory() {
		registeredUsers = RegisteredUsersFileHandler.retrieveRegisteredUsers();
		loggedInUsers = new HashMap<>();
	}

	public Command getCommand(CommandType commandType) {
		if (commandType == null) {
			System.out.println(ILLEGAL_COMMAND_TYPE);
			LogFileServerHandler.log(ILLEGAL_COMMAND_TYPE);
			throw new IllegalCommandTypeException(ILLEGAL_COMMAND_TYPE);
		}

		switch (commandType) {
			case REGISTER:
				return new RegisterCommand(registeredUsers, loggedInUsers);
			case LOGIN:
				return new LoginCommand(registeredUsers, loggedInUsers);
			case ADD_FRIEND:
				return new AddFriendCommand(registeredUsers, loggedInUsers);
			case CREATE_GROUP:
				return new CreateGroupCommand(registeredUsers, loggedInUsers);
			case SPLIT:
				return new SplitCommand(registeredUsers, loggedInUsers);
			case SPLIT_GROUP:
				return new SplitGroupCommand(registeredUsers, loggedInUsers);
			case GET_STATUS:
				return new GetStatusCommand(registeredUsers, loggedInUsers);
			case PAYED:
				return new PayedCommand(registeredUsers, loggedInUsers);
			case HELP:
				return new HelpCommand(registeredUsers, loggedInUsers);
			case LOGOUT:
				return new LogoutCommand(registeredUsers, loggedInUsers);
			case HISTORY:
				return new HistoryCommand(registeredUsers, loggedInUsers);
			case SWITCH_CURRENCY:
				return new SwitchCurrencyCommand(registeredUsers, loggedInUsers);
			default:
				return null;
		}
	}
}
