package bg.sofia.uni.fmi.mjt.splitwise.server.command;

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
import bg.sofia.uni.fmi.mjt.splitwise.server.commands.SwitchCurrencyCommand;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.Friend;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.RegularGroup;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class SwitchCurrencyCommandTest {
	private static final String SWITCH_CURRENCY = "switch-currency ";
	private static final String TESTCHO_NAME = "testcho";
	private static final String TESTCHO_PASSWORD = "123 456";
	private static final String PESHO_NAME = "pesho";
	private static final String PESHO_PASSWORD = "123 789/.";
	private static final String GROUP_NAME = "friends";

	private SwitchCurrencyCommand switchCurrencyCommand;
	private Map<String, User> registeredUsers;
	private Map<SocketChannel, String> loggedInUsers;
	private SocketChannel socketChannelTestcho;
	private User testchoUser;
	private User peshoUser;
	private RegularGroup group;
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

		friend = new Friend(GROUP_NAME, users);
		testchoUser.getFriends().put(PESHO_NAME, friend);

		group = new RegularGroup(GROUP_NAME, users);
		testchoUser.getGroups().put(GROUP_NAME, group);
		peshoUser.getGroups().put(GROUP_NAME, group);

		loggedInUsers = new HashMap<>();
		socketChannelTestcho = SocketChannel.open();
		loggedInUsers.put(socketChannelTestcho, TESTCHO_NAME);

		switchCurrencyCommand = new SwitchCurrencyCommand(registeredUsers, loggedInUsers);
	}

	@Test
	public void testExecuteInvalidArguments() {
		final String message = SWITCH_CURRENCY;
		final String expexted = Command.INVALID_NUMBER_ARGUMENTS_MESSAGE;
		String actual = switchCurrencyCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid arguments number is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteUnsupportedCurrency() {
		final String message = SWITCH_CURRENCY + "USDBN";
		final String expexted = SwitchCurrencyCommand.UNSUPPORTED_CURRENCY;
		String actual = switchCurrencyCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Verification of invalid currency is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));
	}

	@Test
	public void testExecuteSuccessfulSwitch() {
		// add obligations
		group.addObligation(testchoUser, peshoUser, 1.0, 1.0, "", false);
		friend.addObligation(testchoUser, peshoUser, 1.0, 1.0, "", false);

		final String message = SWITCH_CURRENCY + "EUR";
		final String expexted = SwitchCurrencyCommand.SUCCESSFULL_SWITCH_CURRENCY + "EUR";
		String actual = switchCurrencyCommand.execute(socketChannelTestcho, message);

		final String assertMessage = "Switching currency is not correct.";
		assertTrue(assertMessage, actual.contains(expexted));

		// verify correct switch of currency in obligations in friend
		friend.getDebts()
				.get(TESTCHO_NAME)
				.get(PESHO_NAME)
				.stream()
				.allMatch(o -> o.getLender().getCurrency().equals("EUR") && o.getMoneyDebtLenderCurrency() == 0.51);

		// verify correct switch of currency in obligations in a group
		group.getDebts()
				.get(TESTCHO_NAME)
				.get(PESHO_NAME)
				.stream()
				.allMatch(o -> o.getLender().getCurrency().equals("EUR") && o.getMoneyDebtLenderCurrency() == 0.51);
	}
}
