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
import bg.sofia.uni.fmi.mjt.splitwise.server.commands.PayedCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.Friend;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.RegularGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class PayedCommandTest {
	private static final String PAYED = "payed 2 ";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";
	private static final String GROUP_NAME = "friends";
	private static final String REASON_OBLIGATION = "beer";

	private PayedCommand payedCommand;
	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;
	private SocketChannel socketChannelTestcho;
	private SocketChannel socketChannelPesho;
	private User testchoUser;
	private User peshoUser;
	private Friend friend;
	private RegularGroup group;

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
		testchoUser.getFriends().put(PESHO_NAME, friend);

		group = new RegularGroup(GROUP_NAME, users);
		testchoUser.getGroups().put(GROUP_NAME, group);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);
		socketChannelPesho = SocketChannel.open();
		loggedInUsers.put(socketChannelPesho, TESTCHO_NAME);

		payedCommand = new PayedCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteInvalidArguments() {
		final String message = PAYED;
		final String expexted = Command.INVALID_NUMBER_ARGUMENTS_MESSAGE;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid arguments number is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteUserNotInFriendList() {
		// add a new user who is not in your friend list
		final String newUserName = "user";
		User user = new User(newUserName, TESTCHO_PASSWORD);
		registeredUsers.put(newUserName, user);

		final String message = PAYED + newUserName;
		final String expexted = newUserName + Command.FRIEND_NOT_PRESENT_MESSAGE;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification is not correct when trying to add money from a user who not in friend list .";
		assertEquals(assertMessage, actual, expexted);
	}

	@Test
	public void testExecutePayedFriendsEqualAmount() {
		// add pesho's obligation to testcho
		friend.addObligation(testchoUser, peshoUser, 2.0, 2.0, REASON_OBLIGATION, false);

		// resolve obligation
		final String message = PAYED + PESHO_NAME;
		final String expexted = PESHO_NAME + " payed you 2.0 BGN." + System.lineSeparator()
				+ Command.CURENT_STATUS_MESSAGE + System.lineSeparator() + Command.NOTHING_TO_SHOW_MESSAGE;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Paying the same amount of money as the money in the obligation is not correct.";
		assertEquals(assertMessage, actual, expexted);
	}

	@Test
	public void testExecutePayedFriendsMoreMoney() {
		// add pesho's obligation to testcho
		friend.addObligation(testchoUser, peshoUser, 1.0, 1.0, REASON_OBLIGATION, false);

		// resolve obligation
		final String message = PAYED + PESHO_NAME;
		final String expexted = PESHO_NAME + " has given you more money than they owe you. They have payed you 1.0."
				+ System.lineSeparator() + Command.CURENT_STATUS_MESSAGE + System.lineSeparator()
				+ Command.NOTHING_TO_SHOW_MESSAGE;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Paying more money than the other user owes is not correct.";
		assertEquals(assertMessage, actual, expexted);
	}

	@Test
	public void testExecutePayedFriendsLessMoney() {
		// add pesho's obligation to testcho
		friend.addObligation(testchoUser, peshoUser, 3.0, 3.0, REASON_OBLIGATION, false);

		// resolve obligation
		final String message = PAYED + PESHO_NAME;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Paying less money than the other user owes is not correct.";
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " payed you 2.0 BGN."));
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " owes you 1.0 BGN [beer]."));
	}

	@Test
	public void testExecutePaymentInGroup() {
		// add pesho's obligation to testcho in group
		group.addObligation(testchoUser, peshoUser, 2.0, 2.0, REASON_OBLIGATION, false);

		// resolve obligation
		final String message = PAYED + PESHO_NAME + " " + GROUP_NAME;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Payment in group is not correct.";
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " payed you 2.0 BGN."));
		assertTrue(assertMessage, actual.contains(Command.NOTHING_TO_SHOW_MESSAGE));
	}

	@Test
	public void testExecuteObligationInFriendsPaymentInGroup() {
		// add pesho's obligation to testcho as a friend
		friend.addObligation(testchoUser, peshoUser, 2.0, 2.0, REASON_OBLIGATION, false);

		// try to pay in a group
		final String message = PAYED + PESHO_NAME + " " + GROUP_NAME;
		String actual = payedCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Paying in group when the obligations are not there is not correct.";
		assertTrue(assertMessage, actual.contains(PESHO_NAME + " does not owe you any money at the moment."));
		assertTrue(assertMessage, actual.contains(Command.NOTHING_TO_SHOW_MESSAGE));
	}

}
