package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.files.HistoryFileHandler;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class HistoryCommand extends Command {

	private static final String HISTORY_MESSAGE = "*** History ***";

	public HistoryCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		if (!isLoggedIn(socketChannel)) {
			return NOT_LOGGED_IN_USER_MESSAGE;
		}

		String username = getLoggedInUsers().get(socketChannel);
		User user = getRegisteredUsers().get(username);

		String historyOfPayments = HistoryFileHandler.retrieveHistoryOfPayments(user);
		return HISTORY_MESSAGE + System.lineSeparator()
				+ (historyOfPayments.isEmpty() ? NOTHING_TO_SHOW_MESSAGE : historyOfPayments);
	}

}
