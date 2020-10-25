package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

public enum CommandType {
	REGISTER("register"), LOGIN("login"), ADD_FRIEND("add-friend"), CREATE_GROUP("create-group"), SPLIT("split"),
	SPLIT_GROUP("split-group"), GET_STATUS("get-status"), PAYED("payed"), HELP("help"), LOGOUT("logout"),
	HISTORY("history"), SWITCH_CURRENCY("switch-currency");

	private final String commandName;

	private CommandType(String commandName) {
		this.commandName = commandName;
	}

	public String getCommandName() {
		return commandName;
	}
}
