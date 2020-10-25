package bg.sofia.uni.fmi.mjt.splitwise.server.user;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.files.LogFileServerHandler;

public class Friend extends Group {
	private static final String TOO_MANY_USERS_ERROR_MESSAGE = "Trying to create a friend with too many users.";
	private static final int MAX_NUMBER_USERS = 2;

	public Friend(String groupName, Set<User> users) {
		super(groupName, users);
		validate(users);
	}

	private void validate(Set<User> users) {
		if (users.size() > MAX_NUMBER_USERS) {
			String userNames = users.stream().map(User::getUsername).collect(Collectors.joining(", "));
			System.out.println(TOO_MANY_USERS_ERROR_MESSAGE);
			LogFileServerHandler.log(TOO_MANY_USERS_ERROR_MESSAGE + " The user names are: " + userNames);
			throw new IllegalArgumentException(TOO_MANY_USERS_ERROR_MESSAGE);
		}
	}

	/**
	 * Splits an amount of money equally between two friends. If the amount cannot
	 * be divided equally, the borrower will owe the smaller part of the division to
	 * the lander.
	 * 
	 * @param lender           The lender.
	 * @param friendUser       The borrower.
	 * @param amount           The amount of money in the lender's currency.
	 * @param reason           The reason for the split.
	 * @param addNotifications if true, adds notifications to the borrower,
	 *                         otherwise does not
	 */
	public void split(User lender, User friendUser, double amount, String reason, boolean addNotifications) {
		List<Double> shares = calculateEqualShares(MAX_NUMBER_USERS, amount);
		Double smallerShare = Collections.min(shares);

		double moneyDebtorCurrency = getMoneyDebtorCurrency(lender, friendUser, smallerShare);
		addObligation(lender, friendUser, smallerShare, moneyDebtorCurrency, reason, addNotifications);
	}

	private double getMoneyDebtorCurrency(User lender, User friendUser, double money) {
		return getExchangeRatesClient().getExchangeRate(lender.getCurrency(), friendUser.getCurrency())
				.get()
				.convertMoney(money);
	}

}
