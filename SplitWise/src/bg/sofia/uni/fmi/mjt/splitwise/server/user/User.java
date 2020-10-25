package bg.sofia.uni.fmi.mjt.splitwise.server.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRate;

public class User {
	private static final String PERIOD = ".";

	private static final String SPACE = " ";

	private static final String YOU_OWE = "You owe ";

	private static final String APPROVED_YOUR_PAYMENT = " approved your payment ";

	private static final String DEFAULT_CURRENCY = "BGN";

	private String username;
	private String password;
	private Map<String, RegularGroup> groups;
	private Map<String, Friend> friends;
	private List<String> notifications;
	private String currency;

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		groups = new HashMap<>();
		friends = new HashMap<>();
		notifications = new ArrayList<>();
		currency = DEFAULT_CURRENCY;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void addGroup(String groupName, RegularGroup group) {
		groups.put(groupName, group);
	}

	public Map<String, RegularGroup> getGroups() {
		return groups;
	}

	public Map<String, Friend> getFriends() {
		return friends;
	}

	public List<String> getNotifications() {
		return notifications;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String newCurrency) {
		this.currency = newCurrency;
	}

	/**
	 * Returns the notifications for the user as String and clears them.
	 * 
	 * @return The notifications.
	 */
	public String getNotificationsAsString() {
		String notificationsString = notifications.stream().collect(Collectors.joining(System.lineSeparator()));
		notifications.clear();
		return notificationsString;
	}

	public void addNotification(String notification) {
		notifications.add(notification);
	}

	public boolean hasFriend(String friendName) {
		return friends.containsKey(friendName);
	}

	public void addFriend(String friendName, Friend friend) {
		friends.put(friendName, friend);
	}

	public String getStatusFriend(String friendName) {
		return friends.get(friendName).getStatus(this).stream().collect(Collectors.joining(System.lineSeparator()));
	}

	public String getStatusGroup(String groupName) {
		return groups.get(groupName).getStatus(this).stream().collect(Collectors.joining(System.lineSeparator()));
	}

	public String addPaymentFriend(User friend, double amount, boolean addNotifications) {
		return friends.get(friend.getUsername()).pay(this, friend, amount, addNotifications);
	}

	public String addPaymentFriendInGroup(User friend, String groupName, double amount, boolean addNotifications) {
		return groups.get(groupName).pay(this, friend, amount, addNotifications);
	}

	public void switchCurrency(ExchangeRate rate) {
		setCurrency(rate.getToCurrency());
		friends.values().forEach(f -> f.switchCurrency(this, rate));
		groups.values().forEach(g -> g.switchCurrency(this, rate));
	}

	/**
	 * Adds notifications for the resolved obligations. This method is used when the
	 * current user is the lender and the obligations are resolved by the borrower
	 * through splitting money with the lender. The format of the notifications is
	 * "<borrower name> approved your payment <money> [<reason>]"
	 * 
	 * @param resolvedObligations The resolved obligations.
	 */
	public void addResolvedByBorrowerObligationsNotifications(List<Obligation> resolvedObligations) {
		resolvedObligations.stream().forEach(obligation -> {
			notifications.add(obligation.getBorrower().getUsername() + APPROVED_YOUR_PAYMENT
					+ obligation.getLastPaidMoneyLenderCurrency() + SPACE + obligation.getLender().getCurrency() + SPACE
					+ obligation.getReason() + PERIOD);
		});
	}

	/**
	 * Adds notifications for the resolved obligations.This method is used when the
	 * current user is the borrower and the obligations are resolved by the lender
	 * when adding payed money by the borrower. The format of the notifications is
	 * "<lender name> approved your payment <money> [<reason>]"
	 * 
	 * @param resolvedObligations The resolved obligations.
	 */
	public void addResolvedByLenderObligationsNotifications(List<Obligation> resolvedObligations) {
		resolvedObligations.stream().forEach(obligation -> {
			notifications.add(obligation.getLender().getUsername() + APPROVED_YOUR_PAYMENT
					+ obligation.getLastPaidMoneyBorrowerCurrency() + SPACE + obligation.getBorrower().getCurrency()
					+ SPACE + obligation.getReason() + PERIOD);
		});
	}

	public void addNewObligationNotifications(Obligation obligation) {
		notifications
				.add(YOU_OWE + obligation.getLender().getUsername() + SPACE + obligation.getMoneyDebtBorrowerCurrency()
						+ obligation.getBorrower().getCurrency() + SPACE + obligation.getReason());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		User other = (User) obj;
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

}
