package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import static org.junit.Assert.assertEquals;
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
import bg.sofia.uni.fmi.mjt.splitwise.server.commands.SplitCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.Friend;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class SplitCommandTest {
	private static final String SPLIT = "split 5 ";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";

	private SplitCommand splitCommand;
	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;
	private SocketChannel socketChannelTestcho;
	private SocketChannel socketChannelPesho;
	private User testchoUser;
	private User peshoUser;
	private Friend friend;

	@Before
	public void setUp() throws IOException {
		registeredUsers = new HashMap<>();
		testchoUser = new User(TESTCHO_NAME, TESTCHO_PASSWORD);
		registeredUsers.put(TESTCHO_NAME, testchoUser);

		peshoUser = new User(PESHO_NAME, PESHO_PASSWORD);
		registeredUsers.put(PESHO_NAME, peshoUser);

		Set<User> users = new HashSet<>();
		users.add(testchoUser);
		users.add(peshoUser);

		friend = new Friend(TESTCHO_NAME + PESHO_NAME, users);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);

		splitCommand = new SplitCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteInvalidArguments() {
		final String message = SPLIT;
		final String expexted = Command.INVALID_NUMBER_ARGUMENTS_MESSAGE;
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid arguments number is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteNotRegesteredUser() {
		final String message = SPLIT + "iva";
		final String expexted = "iva" + Command.NOT_REGISTERED_USER_MESSAGE;
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of adding not registered friends is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteNotFriendUser() {
		final String message = SPLIT + PESHO_NAME;
		final String expexted = PESHO_NAME + Command.FRIEND_NOT_PRESENT_MESSAGE;
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Unable to verify correctly splitting with user who is not in your friend list";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteInvalidAmount() {
		testchoUser.getFriends().put(PESHO_NAME, friend);
		final String message = "split dskghd " + PESHO_NAME;
		final String expexted = Command.INVALID_AMOUNT_MESSAGE;
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Unable to verify correctly invalid amount of money argument.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteSuccessfulSplit() {
		testchoUser.getFriends().put(PESHO_NAME, friend);

		final String message = SPLIT + PESHO_NAME + " beer";
		final String expexted = SplitCommand.SUCCESSFUL_SPLIT + "5.0 between you and pesho." + System.lineSeparator()
				+ SplitCommand.CURENT_STATUS_MESSAGE + System.lineSeparator() + "pesho owes you 2.5 BGN [beer].";
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Not splitting correctly.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteSuccessfulSplitWitchCurrentObligations() {
		testchoUser.getFriends().put(PESHO_NAME, friend);
		peshoUser.getFriends().put(TESTCHO_NAME, friend);

		final String message = SPLIT + PESHO_NAME + " beer";

		final String expexted = SplitCommand.SUCCESSFUL_SPLIT + "5.0 between you and pesho." + System.lineSeparator()
				+ SplitCommand.CURENT_STATUS_MESSAGE + System.lineSeparator() + "pesho owes you 2.5 BGN [beer].";
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Not splitting correctly.";
		assertEquals(assertMessage, expexted, actual);

		loggedInUsers.put(socketChannelPesho, PESHO_NAME);
		final String messageSecondSplit = SPLIT + TESTCHO_NAME + " mom birthday";
		final String expextedSecondSplit = SplitCommand.SUCCESSFUL_SPLIT + "5.0 between you and testcho."
				+ System.lineSeparator() + Command.CURENT_STATUS_MESSAGE + System.lineSeparator()
				+ SplitCommand.NOTHING_TO_SHOW_MESSAGE;
		String actualSecondSplit = splitCommand.execute(socketChannelPesho, messageSecondSplit);

		final String assertMessageSecondSplit = "Not splitting correctly when the lender owes the borrower money.";
		assertEquals(assertMessageSecondSplit, expextedSecondSplit, actualSecondSplit);
	}

	@Test
	public void testExecuteSuccessfulSplitCurrentUserBiggerShare() {
		testchoUser.getFriends().put(PESHO_NAME, friend);

		final String message = "split 8.01 " + PESHO_NAME + " beer";
		final String expexted = SplitCommand.SUCCESSFUL_SPLIT + "8.01 between you and pesho." + System.lineSeparator()
				+ SplitCommand.CURENT_STATUS_MESSAGE + System.lineSeparator() + "pesho owes you 4.0 BGN [beer].";
		String actual = splitCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Not splitting correctly.";
		assertEquals(assertMessage, expexted, actual);
	}
}
