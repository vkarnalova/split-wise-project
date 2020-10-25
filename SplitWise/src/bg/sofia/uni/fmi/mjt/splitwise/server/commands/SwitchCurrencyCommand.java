package bg.sofia.uni.fmi.mjt.splitwise.server.commands;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRate;
import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRatesClient;
import bg.sofia.uni.fmi.mjt.splitwise.server.user.User;

public class SwitchCurrencyCommand extends Command {
	public static final String SUCCESSFULL_SWITCH_CURRENCY = "You have successfully switched your currency to ";
	public static final String UNSUPPORTED_CURRENCY = "You have entered an unsupported currency.";

	private static final int COMMAND_ARGUMENTS_NUMBER = 2;
	private static final int CURRENCY_INDEX = 1;

	private ExchangeRatesClient client;

	public SwitchCurrencyCommand(Map<String, User> registeredUsers, Map<SocketChannel, String> loggedInUsers) {
		super(registeredUsers, loggedInUsers);
		client = ExchangeRatesClient.getInstance();
	}

	@Override
	public String execute(SocketChannel socketChannel, String message) {
		if (!isLoggedIn(socketChannel)) {
			return NOT_LOGGED_IN_USER_MESSAGE;
		}

		String[] split = message.split(COMMAND_ARGUMENTS_SEPARATOR);
		if (split.length < COMMAND_ARGUMENTS_NUMBER) {
			return INVALID_NUMBER_ARGUMENTS_MESSAGE + System.lineSeparator() + HELP_INFORMATION_MESSAGE;
		}

		String username = getLoggedInUsers().get(socketChannel);
		User user = getRegisteredUsers().get(username);

		return switchCurrency(user, split[CURRENCY_INDEX]);
	}

	private String switchCurrency(User user, String newCurrency) {
		Optional<ExchangeRate> exchangeRateOptional = client.getExchangeRate(user.getCurrency(), newCurrency);
		if (exchangeRateOptional.isEmpty()) {
			return UNSUPPORTED_CURRENCY;
		}

		user.switchCurrency(exchangeRateOptional.get());

		return SUCCESSFULL_SWITCH_CURRENCY + newCurrency;
	}

}
