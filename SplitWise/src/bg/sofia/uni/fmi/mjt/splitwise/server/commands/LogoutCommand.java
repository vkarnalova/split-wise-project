package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class LogoutCommand extends Command {
	private static final String DISCONNECTED_FROM_SERVER = "Disconnected from server";

	public LogoutCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		getLoggedInUsers().remove(socketChannel);
		return DISCONNECTED_FROM_SERVER;
	}

}
