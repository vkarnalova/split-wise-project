package bg.sofia.uni.fmi.mjt.splitwise.server.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Test;

import bg.sofia.uni.fmi.mjt.splitwise.server.files.RegisteredUsersFileHandler;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class RegisteredUsersFileHandlerTest {
	private final static String REGISTERED_USERS_READER_CONTENT = "pesho,123" + System.lineSeparator() + "gosho,456"
			+ System.lineSeparator();

	@Test
	public void testRetreiveRegisteredUsersEmptyFile() {
		Map<String, User> registeredUsers = RegisteredUsersFileHandler.retreiveRegisteredUsers(new StringReader(""));
		assertTrue(registeredUsers.isEmpty());
	}

	@Test
	public void testRetreiveRegisteredUsers() {
		Map<String, User> registeredUsers = RegisteredUsersFileHandler
				.retreiveRegisteredUsers(new StringReader(REGISTERED_USERS_READER_CONTENT));

		final int expectedSize = 2;
		int actualSize = registeredUsers.size();
		final String assertMessageSize = "Not all registered users could be retrieved.";
		assertEquals(assertMessageSize, expectedSize, actualSize);

		final String gosho = "gosho";
		final String expextedPassword = "456";
		String actualPassword = registeredUsers.get(gosho).getPassword();
		final String assertMessagePassword = "Registered users' information is not retrieved correctly.";
		assertEquals(assertMessagePassword, expextedPassword, actualPassword);
	}

	@Test
	public void testAddRegisteredUser() {
		StringWriter writer = new StringWriter();
		writer.write(REGISTERED_USERS_READER_CONTENT);
		RegisteredUsersFileHandler.addRegisteredUser(new User("testcho", "789"), writer);

		final String expexted = REGISTERED_USERS_READER_CONTENT + "testcho,789" + System.lineSeparator();
		String actual = writer.toString();
		final String assertMessage = "Information about the registered users is not correctly added.";
		assertEquals(assertMessage, expexted, actual);
	}
}
