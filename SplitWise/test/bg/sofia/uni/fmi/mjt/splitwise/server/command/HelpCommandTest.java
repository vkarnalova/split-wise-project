package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.commands.HelpCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class HelpCommandTest {
	private static final String HELP = "help";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";

	private HelpCommand helpCommand;
	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;
	private SocketChannel socketChannelTestcho;
	private User testchoUser;

	@Before
	public void setUp() throws IOException {
		registeredUsers = new HashMap<>();
		testchoUser = new User(TESTCHO_NAME, TESTCHO_PASSWORD);
		registeredUsers.put(TESTCHO_NAME, testchoUser);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);

		helpCommand = new HelpCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteAllCommandsInformationPresent() {
		final String message = HELP;
		String actual = helpCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Help does not contain information about all commands.";
		assertTrue(assertMessage, actual.contains("add-friend"));
		assertTrue(assertMessage, actual.contains("create-group"));
		assertTrue(assertMessage, actual.contains("login"));
		assertTrue(assertMessage, actual.contains("register"));
		assertTrue(assertMessage, actual.contains("payed"));
		assertTrue(assertMessage, actual.contains("split"));
		assertTrue(assertMessage, actual.contains("split-group"));
		assertTrue(assertMessage, actual.contains("get-status"));
		assertTrue(assertMessage, actual.contains("history"));
		assertTrue(assertMessage, actual.contains("help"));
		assertTrue(assertMessage, actual.contains("logout"));
		assertTrue(assertMessage, actual.contains("switch-currency"));
	}

}
