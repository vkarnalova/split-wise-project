package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class HelpCommand extends Command {
	private static final String[] COMMANDS_MANUAL = { "register <username> <password>",
		"This command is used for registering a new user.", System.lineSeparator(), "login <username> <password>",
		"This command is used for logging in.", System.lineSeparator(), "add-friend <username>",
		"Adds user with name username to the friend list. The user should be registered.", System.lineSeparator(),
		"create-group <group_name> <username> <username> ... <username>",
		"Creates a group with name <group_name> and members <username> ... <username>.",
		"The members of the group should be at least 3, otherwise the group will not be created.",
		"The current user is automatically added to the group.",
		"If some of the users are not registered, they are not added to the group.", System.lineSeparator(),
		"split <amount> <username> [<reason>]",
		"Splits money with user with name username. The amount is considered to be in the current",
		"user's currency. If the current user owes money to the other user, as much as possible",
		"obligations are resolved before adding a new obligation. The other user should be a friend",
		"with the current user. The argument reason is optional and it shows the reason of the obligation",
		System.lineSeparator(), "split-group <amount> <group_name> [<reason>]",
		"Splits money with all users in the group with name group_name. The money are divided equally",
		"among the users. The amount is considered to be in the current user's currency. If the current",
		"user owes money to one of the other users, as much as possible obligations are resolved before",
		"adding a new obligation. The argument reason is optional and it shows the reason of the obligation.",
		System.lineSeparator(), "payed <amount> <username> [<group_name>]",
		"Adds payment from a user with name username. The amount is considered to be in the current user's",
		"currency. The group_name argument is optional. If it is present, obligations in the group are resolved,",
		"otherwise obligations splitted as friends are resolved.", System.lineSeparator(), "get-status",
		"Prints the current status of the user - all of their obligations and the obligations to them.",
		System.lineSeparator(), "history",
		"Prints the history of all payments that the user has made including the date when it was paid.",
		System.lineSeparator(), "logout", "Used for logging out.", System.lineSeparator(),
		"switch-currency <new_currency>",
		"Changes the currency of the current user. The currency of all their obligations and the",
		"obligations is changed. From now on, all payments will be calculated in the new currency",
		System.lineSeparator(), "help", "Displays a manual of all supported commands." };

	public HelpCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		return Arrays.stream(COMMANDS_MANUAL).collect(Collectors.joining(System.lineSeparator()));
	}

}
