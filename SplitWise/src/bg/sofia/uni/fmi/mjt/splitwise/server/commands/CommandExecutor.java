package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandExecutor {
	private static final String UNSUPPORTED_COMMAND = "Unsupported command.";

	private Map<CommandType, Command> commands;

	public CommandExecutor() {
		commands = new HashMap<>();

		CommandFactory factory = new CommandFactory();
		Arrays.stream(CommandType.values())
				.forEach(commandType -> commands.put(commandType, factory.getCommand(commandType)));
	}

	public String execute(SocketChannel socketChannel, String message) {
		String command = message.split(" ")[0].toLowerCase().trim();
		Optional<CommandType> commandType = Arrays.stream(CommandType.values())
				.filter(ct -> ct.getCommandName().equals(command))
				.findFirst();
		if (commandType.isEmpty()) {
			return UNSUPPORTED_COMMAND;
		}
		return commands.get(commandType.get()).execute(socketChannel, message);
	}
}
