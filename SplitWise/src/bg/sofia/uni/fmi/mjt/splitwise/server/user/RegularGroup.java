package bg.sofia.uni.fmi.mjt.splitwise.server.user;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegularGroup extends Group {

	public RegularGroup(String groupName, Set<User> users) {
		super(groupName, users);
	}

	public void split(User lender, double amount, String reason, List<User> usersToBeSentNotifications) {
		List<Double> shares = calculateEqualShares(getUsers().size(), amount);

		// The max share is removed as it is for the lender.
		Double maxShare = Collections.max(shares);
		shares.remove(maxShare);

		Iterator<Double> iterator = shares.iterator();
		getUsers().stream().forEach(user -> {
			if (!user.getUsername().equals(lender.getUsername())) {
				Double moneyShare = iterator.next();
				double moneyDebtorCurrency = getMoneyDebtorCurrency(lender, user, moneyShare);
				addObligation(lender, user, moneyShare, moneyDebtorCurrency, reason,
						usersToBeSentNotifications.contains(user));
			}
		});
	}

	private double getMoneyDebtorCurrency(User lender, User friendUser, double money) {
		return getExchangeRatesClient().getExchangeRate(lender.getCurrency(), friendUser.getCurrency())
				.get()
				.convertMoney(money);
	}

}
