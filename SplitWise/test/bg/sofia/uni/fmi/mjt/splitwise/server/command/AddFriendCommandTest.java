package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.commands.AddFriendCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.commands.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class AddFriendCommandTest {
	private static final String ADD_FRIEND = "add-friend";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";

	private AddFriendCommand addFriendCommand;
	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;
	private SocketChannel socketChannelTestcho;
	private User testchoUser;
	private User peshoUser;

	@Before
	public void setUp() throws IOException {
		registeredUsers = new HashMap<>();
		testchoUser = new User(TESTCHO_NAME, TESTCHO_PASSWORD);
		registeredUsers.put(TESTCHO_NAME, testchoUser);
		peshoUser = new User(PESHO_NAME, PESHO_PASSWORD);
		registeredUsers.put(PESHO_NAME, peshoUser);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);

		addFriendCommand = new AddFriendCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteInvalidArguments() {
		final String message = ADD_FRIEND;
		final String expexted = Command.INVALID_NUMBER_ARGUMENTS_MESSAGE;
		String actual = addFriendCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid arguments number is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteNotRegesteredUser() {
		final String message = ADD_FRIEND + " iva";
		final String expexted = "iva" + Command.NOT_REGISTERED_USER_MESSAGE;
		String actual = addFriendCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of adding not registered friends is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteAddingYourselfAsFriend() {
		final String message = ADD_FRIEND + " testcho";
		final String expexted = AddFriendCommand.CANNOT_ADD_YOURSELF_MESSAGE;
		String actual = addFriendCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of not being able to add yourself as your friend is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteAddFriend() {
		final String message = ADD_FRIEND + " " + PESHO_NAME;
		final String expexted = "pesho" + AddFriendCommand.ADDED_FRIEND_MESSAGE;
		String actual = addFriendCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Adding a friend is not correct.";
		assertEquals(assertMessage, expexted, actual);
		assertTrue(assertMessage, testchoUser.hasFriend("pesho"));
	}

	@Test
	public void testExecuteAddFriendAgain() {
		final String message = ADD_FRIEND + " " + PESHO_NAME;
		addFriendCommand.execute(socketChannelTestcho, message);

		final String expexted = "pesho" + AddFriendCommand.FRIEND_IS_PRESENT_MESSAGE;
		String actual = addFriendCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of having a friend is not correct.";
		assertEquals(assertMessage, expexted, actual);
		assertTrue(assertMessage, testchoUser.hasFriend("pesho"));
	}

	@Test
	public void testExecuteNotLoggedInUser() throws IOException {
		final String message = ADD_FRIEND + " " + PESHO_NAME;
		final String expexted = Command.NOT_LOGGED_IN_USER_MESSAGE;
		String actual = addFriendCommand.execute(SocketChannel.open(), message);

		final String assertMessage = "Verification of executing commands only when logged in is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

}
