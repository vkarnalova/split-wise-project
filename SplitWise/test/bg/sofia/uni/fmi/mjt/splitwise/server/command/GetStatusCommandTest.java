package bg.sofia.uni.fmi.mjt.splitwise.server.command;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.commands.GetStatusCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRate;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.Friend;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.RegularGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class GetStatusCommandTest {
	private static final String GET_STATUS = "get-status";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";

	private GetStatusCommand getStatusCommand;
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

		group = new RegularGroup("friends", users);
		testchoUser.getGroups().put("friends", group);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);
		socketChannelPesho = SocketChannel.open();
		loggedInUsers.put(socketChannelPesho, TESTCHO_NAME);

		getStatusCommand = new GetStatusCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteNoObligationsFround() {
		final String message = GET_STATUS;
		final String expexted = GetStatusCommand.NO_OBLIGATIONS_FOUND_MESSAGE;
		String actual = getStatusCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Getting status when there aren't any obligations is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteFriendsObligationsFround() {
		friend.addObligation(testchoUser, peshoUser, 4.0, 4.0, "beer", false);

		final String message = GET_STATUS;
		final String expexted = "Friends:" + System.lineSeparator() + PESHO_NAME + " owes you 4.0 BGN [beer].";
		String actual = getStatusCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Getting status when there are friends' obligations is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteGroupObligationsFround() {
		group.addObligation(testchoUser, peshoUser, 4.0, 4.0, "beer", false);

		final String message = GET_STATUS;
		final String expexted = "Groups:" + System.lineSeparator() + group.getGroupName() + System.lineSeparator()
				+ PESHO_NAME + " owes you 4.0 BGN [beer].";
		String actual = getStatusCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Getting status when there are obligations in a group is not correct.";
		assertEquals(assertMessage, expexted, actual);
	}

	@Test
	public void testExecuteGroupDifferentCurrency() {
		group.addObligation(testchoUser, peshoUser, 1.0, 1.0, "beer", false);
		testchoUser.switchCurrency(new ExchangeRate("BGN", "USD", 0.56));

		final String message = GET_STATUS;
		String expexted = "Groups:" + System.lineSeparator() + group.getGroupName() + System.lineSeparator()
				+ PESHO_NAME + " owes you 0.56 USD [beer].";
		String actual = getStatusCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Getting status after switching currency is not correct.";
		assertEquals(assertMessage, expexted, actual);

		expexted = "Groups:" + System.lineSeparator() + group.getGroupName() + System.lineSeparator() + "You owe "
				+ TESTCHO_NAME + " 1.0 BGN [beer].";
		actual = getStatusCommand.execute(socketChannelPesho, message);
	}
}
