package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.commands.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.commands.CreateGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class CreateGroupCommandTest {
	private static final String CREATE_GROUP_FRIENDS = "create-group friends ";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";
	private static final String GOSHO_NAME = "gosho";
	private static final String GOSHO_PASSWORD = "456";

	private CreateGroupCommand createGroupCommand;
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

		createGroupCommand = new CreateGroupCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteInvalidArguments() {
		final String message = CREATE_GROUP_FRIENDS + PESHO_NAME;
		final String expexted = Command.INVALID_NUMBER_ARGUMENTS_MESSAGE;
		String actual = createGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid arguments number is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteNotEnoughUsers() {
		final String message = CREATE_GROUP_FRIENDS + PESHO_NAME + " " + GOSHO_NAME;
		final String expexted = CreateGroupCommand.NOT_ENOUGH_USERS_MESSAGE;
		String actual = createGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of not enough users for a group is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteCreateGroup() {
		User goshoUser = new User(GOSHO_NAME, GOSHO_PASSWORD);
		registeredUsers.put(GOSHO_NAME, goshoUser);

		final String message = CREATE_GROUP_FRIENDS + PESHO_NAME + " " + GOSHO_NAME;
		final String expexted = CreateGroupCommand.CREATED_GROUP_MESSAGE;
		String actual = createGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Incorrect creation of group.";
		assertEquals(assertMessage, expexted, actual);
		assertTrue(assertMessage, testchoUser.getGroups().get("friends") != null);
		assertTrue(assertMessage, peshoUser.getGroups().get("friends") != null);
		assertTrue(assertMessage, goshoUser.getGroups().get("friends") != null);
	}

	@Test
	public void testExecuteCreateGroupWithoutUnregisteredUsers() {
		registeredUsers.put(GOSHO_NAME, new User(GOSHO_NAME, GOSHO_PASSWORD));

		final String message = CREATE_GROUP_FRIENDS + PESHO_NAME + " " + GOSHO_NAME + " iva mira";
		String actual = createGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Incorrect creation of group when some of the users are unregistered.";
		assertTrue(assertMessage, actual
				.contains("iva" + Command.NOT_REGISTERED_USER_MESSAGE + CreateGroupCommand.GROUP_WITHOUT_USER_MESSAGE));
		assertTrue(assertMessage, actual.contains(
				"mira" + Command.NOT_REGISTERED_USER_MESSAGE + CreateGroupCommand.GROUP_WITHOUT_USER_MESSAGE));
		assertTrue(assertMessage, actual.contains(CreateGroupCommand.CREATED_GROUP_MESSAGE));
	}

	@Test
	public void testExecuteAllUsersNotRegestered() {
		final String message = CREATE_GROUP_FRIENDS + " iva mira";
		final String expexted = CreateGroupCommand.ALL_NOT_REGISTERED_MESSAGE;
		String actual = createGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Incorrect creation of group when all other users are not registered.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteCurrentUserAlreadyInGroup() {
		// create a group with testcho, gosho and pesho
		registeredUsers.put(GOSHO_NAME, new User(GOSHO_NAME, GOSHO_PASSWORD));
		final String message = CREATE_GROUP_FRIENDS + PESHO_NAME + " " + GOSHO_NAME;
		createGroupCommand.execute(socketChannelTestcho, message);

		// try to create a group with the same name again
		final String expexted = CreateGroupCommand.CURRENT_USER_ALREADY_IN_GROUP;
		String actual = createGroupCommand.execute(socketChannelTestcho, message);
		final String assertMessage = "Incorrect creation of group when the current user is in a group with the same name.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteOtherUsersAlreadyInGroupWithSameName() throws IOException {
		// create a group with pesho, gosho and iva
		SocketChannel socketChannelPesho = SocketChannel.open();
		loggedInUsers.put(socketChannelPesho, PESHO_NAME);
		registeredUsers.put(GOSHO_NAME, new User(GOSHO_NAME, GOSHO_PASSWORD));
		String ivaUserName = "iva";
		registeredUsers.put(ivaUserName, new User(ivaUserName, "123"));

		String message = CREATE_GROUP_FRIENDS + PESHO_NAME + " " + GOSHO_NAME + " " + ivaUserName;
		createGroupCommand.execute(socketChannelPesho, message);

		// try to create a group with testcho, gosho and pesho with the same name
		final String expexted = PESHO_NAME + ", " + GOSHO_NAME + CreateGroupCommand.ARE_ALREADY_IN_GROUP_SAME_NAME;
		message = CREATE_GROUP_FRIENDS + PESHO_NAME + " " + GOSHO_NAME;
		String actual = createGroupCommand.execute(socketChannelTestcho, message);
		final String assertMessage = "Incorrect creation of group when the current user is in a group with the same name.";
		assertTrue(assertMessage, actual.contains(expexted));
	}
}
