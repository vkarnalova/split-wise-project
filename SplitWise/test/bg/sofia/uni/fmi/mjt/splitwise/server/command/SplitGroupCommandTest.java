package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.commands.Command;
import bg.sofia.uni.fmi.mjt.splitwise.server.commands.SplitGroupCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRate;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.RegularGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class SplitGroupCommandTest {
	private static final String SPLIT_GROUP = "split-group 5 ";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";
	private static final String GROUP_NAME = "friends";
	private static final String GOSHO_NAME = "gosho";
	private static final String GOSHO_PASSWORD = "456";

	private SplitGroupCommand splitGroupCommand;
	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;
	private SocketChannel socketChannelTestcho;
	private SocketChannel socketChannelPesho;
	private User testchoUser;
	private User peshoUser;
	private RegularGroup group;

	@Before
	public void setUp() throws IOException {
		registeredUsers = new HashMap<>();
		testchoUser = new User(TESTCHO_NAME, TESTCHO_PASSWORD);
		registeredUsers.put(TESTCHO_NAME, testchoUser);

		peshoUser = new User(PESHO_NAME, PESHO_PASSWORD);
		registeredUsers.put(PESHO_NAME, peshoUser);

		User goshoUser = new User(GOSHO_NAME, GOSHO_PASSWORD);
		registeredUsers.put(GOSHO_NAME, goshoUser);

		Set<User> users = new HashSet<>();
		users.add(testchoUser);
		users.add(peshoUser);
		users.add(goshoUser);

		group = new RegularGroup(GROUP_NAME, users);
		testchoUser.getGroups().put(GROUP_NAME, group);
		peshoUser.getGroups().put(GROUP_NAME, group);
		goshoUser.getGroups().put(GROUP_NAME, group);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);

		splitGroupCommand = new SplitGroupCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteInvalidArguments() {
		final String message = SPLIT_GROUP;
		final String expexted = Command.INVALID_NUMBER_ARGUMENTS_MESSAGE;
		String actual = splitGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid arguments number is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteInvalidGroupName() {
		final String message = SPLIT_GROUP + "notExistingGroupName";
		final String expexted = SplitGroupCommand.INVALID_GROUP_NAME_MESSAGE;
		String actual = splitGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification, when trying to split money in a group the user is not part of, is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteInvalidAmount() {
		final String message = "split dskghd " + GROUP_NAME;
		final String expexted = Command.INVALID_AMOUNT_MESSAGE;
		String actual = splitGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Unable to verify correctly invalid amount of money argument.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteSuccessfulSplit() {
		final String message = SPLIT_GROUP + GROUP_NAME + " beer";
		String actual = splitGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Not splitting correctly.";
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " owes you 1.66 BGN [beer]"));
		assertTrue(assertMessage, actual.contains(GOSHO_NAME + " owes you 1.67 BGN [beer]"));
		assertTrue(assertMessage, actual.contains(SplitGroupCommand.SUCCESSFUL_SPLIT));
	}

	@Test
	public void testExecuteSuccessfulSplitWithObligations() {
		String message = SPLIT_GROUP + GROUP_NAME + " beer";
		String actual = splitGroupCommand.execute(socketChannelTestcho, message);

		// Split money so that pesho will have obligation to testcho
		String assertMessage = "Not splitting correctly.";
		assertTrue(assertMessage, actual.contains(SplitGroupCommand.SUCCESSFUL_SPLIT));
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " owes you 1.66 BGN [beer]"));
		assertTrue(assertMessage, actual.contains(GOSHO_NAME + " owes you 1.67 BGN [beer]"));

		// pesho splits money in friends and no obligation of testcho is added
		loggedInUsers.put(socketChannelPesho, PESHO_NAME);
		message = SPLIT_GROUP + GROUP_NAME + " mimi birthday";
		actual = splitGroupCommand.execute(socketChannelPesho, message);
		assertTrue(assertMessage, actual.contains(SplitGroupCommand.SUCCESSFUL_SPLIT));
		assertTrue(assertMessage, actual.contains(GOSHO_NAME + " owes you 1.67 BGN [mimi birthday]"));
		assertFalse(assertMessage, actual.contains(TESTCHO_NAME + " owes you"));
	}

	@Test
	public void testExecuteCorrectSplitDifferentCurrancies() {
		// change testcho's currency and do a split
		testchoUser.switchCurrency(new ExchangeRate("BGN", "USD", 0.56));
		final String message = "split 6 " + GROUP_NAME + " beer";
		String actual = splitGroupCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Not splitting correctly.";
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " owes you 2.0 USD [beer]"));
		assertTrue(assertMessage, actual.contains(GOSHO_NAME + " owes you 2.0 USD [beer]"));
		assertTrue(assertMessage, actual.contains(SplitGroupCommand.SUCCESSFUL_SPLIT));
	}

}
