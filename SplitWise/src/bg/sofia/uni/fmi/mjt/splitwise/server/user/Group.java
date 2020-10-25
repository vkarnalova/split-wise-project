package bg.sofia.uni.fmi.mjt.splitwise.server.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRate;
import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRatesClient;
import bg.sofia.uni.fmi.mjt.splitwise.server.files.HistoryFileHandler;

public abstract class Group {
	private static final String MORE_MONEY_MESSAGE = " has given you more money than they owe you. They have payed you ";
	private static final String DOES_NOT_OWE_MONEY_MESSAGE = " does not owe you any money at the moment.";
	private static final String PAYED_MESSAGE = " payed you ";
	private static final String CLOSING_BRACKET = "].";
	private static final String OPENING_BRACKET = " [";
	private static final String OWES_YOU = " owes you ";
	private static final String YOU_OWE = "You owe ";
	private static final String PERIOD = ".";
	private static final String SPACE = " ";

	private static final int DECIMAL_PLACES = 2;
	private static final double ZERO = 0;

	private String groupName;
	private Set<User> users;
	private Map<String, Map<String, List<Obligation>>> debts; // lender -> borrower -> obligations
	private ExchangeRatesClient exchangeRatesClient;

	public Group(String groupName, Set<User> users) {
		this.groupName = groupName;
		this.users = users;
		debts = createMap(users);
		exchangeRatesClient = ExchangeRatesClient.getInstance();
	}

	public String getGroupName() {
		return groupName;
	}

	public Set<User> getUsers() {
		return users;
	}

	public Map<String, Map<String, List<Obligation>>> getDebts() {
		return debts;
	}

	public ExchangeRatesClient getExchangeRatesClient() {
		return exchangeRatesClient;
	}

	public List<String> getStatus(User user) {
		// Get status regarding who owes money to the given user.
		List<String> status = getStatusUserCredits(user);

		// Get status regarding whom the given user owes money.
		status.addAll(getStatusUserDebts(user));

		return status;
	}

	public boolean hasUser(String userName) {
		return users.stream().anyMatch(u -> u.getUsername().equals(userName));
	}

	/**
	 * Adds an obligation of the borrower to the lender in the group.
	 * 
	 */
	public void addObligation(User lender, User borrower, double moneyLenderCurrency, double moneyDebtorCurrency,
			String reason, boolean addNotifications) {
		List<Obligation> resolvedObligations = new ArrayList<>();
		moneyDebtorCurrency = resolveObligations(borrower.getUsername(), lender.getUsername(), moneyDebtorCurrency,
				resolvedObligations);

		// The payments are made by the lender of the obligations which is the borrower
		// argument
		addPaymentsByLenderToHistoryFile(borrower, resolvedObligations);

		if (addNotifications) {
			borrower.addResolvedByBorrowerObligationsNotifications(resolvedObligations);
		}

		if (moneyDebtorCurrency > 0) {
			Obligation obligation = new Obligation(lender, borrower, moneyDebtorCurrency, reason);
			debts.get(lender.getUsername()).get(borrower.getUsername()).add(obligation);

			if (addNotifications) {
				borrower.addNewObligationNotifications(obligation);
			}
		}
	}

	/**
	 * Calculates the shares each user has to pay. It divides the amount of money
	 * equally.
	 * 
	 * @param usersNumber The number of users that will pay.
	 * @param amount      The amount of money.
	 * @return List of the shares each user has to pay.
	 */
	public List<Double> calculateEqualShares(int usersNumber, double amount) {
		List<Double> shares = new ArrayList<>();
		while (usersNumber > 0) {
			double share = BigDecimal.valueOf(amount / usersNumber)
					.setScale(DECIMAL_PLACES, RoundingMode.HALF_DOWN)
					.doubleValue();
			shares.add(share);
			amount -= share;
			usersNumber--;
		}
		return shares;
	}

	/**
	 * Pays money to the lender from the borrower. Used for payed command.
	 * 
	 * @param lender   The lender.
	 * @param borrower The borrower.
	 * @param money    The amount of money.
	 * @return A message reporting the result of the payment.
	 */
	public String pay(User lender, User borrower, double money, boolean addNotifications) {
		List<Obligation> resolvedObligations = new ArrayList<>();
		double remainingMoney = resolveObligations(lender.getUsername(), borrower.getUsername(), money,
				resolvedObligations);

		if (remainingMoney == money) {
			return borrower.getUsername() + DOES_NOT_OWE_MONEY_MESSAGE;
		}

		addPaymentsByBorrowerToHistoryFile(borrower, resolvedObligations);
		if (addNotifications) {
			borrower.addResolvedByLenderObligationsNotifications(resolvedObligations);
		}

		if (remainingMoney > ZERO) {
			return borrower.getUsername() + MORE_MONEY_MESSAGE + (money - remainingMoney) + PERIOD;
		}

		return borrower.getUsername() + PAYED_MESSAGE + money + SPACE + lender.getCurrency() + PERIOD;
	}

	public void switchCurrency(User user, ExchangeRate rate) {
		switchCurrencyDebtsToUser(user, rate);
		switchCurrencyCreditsOfUser(user, rate);
	}

	private void switchCurrencyCreditsOfUser(User user, ExchangeRate rate) {
		for (Entry<String, Map<String, List<Obligation>>> entry : getDebts().entrySet()) {
			if (entry.getKey() != user.getUsername()) {
				entry.getValue()
						.get(user.getUsername())
						.stream()
						.forEach(obligation -> obligation.switchCurrency(user, rate));
			}
		}
	}

	private void switchCurrencyDebtsToUser(User user, ExchangeRate rate) {
		for (List<Obligation> obligations : getDebts().get(user.getUsername()).values()) {
			obligations.stream().forEach(obligation -> obligation.switchCurrency(user, rate));
		}
	}

	private void addPaymentsByBorrowerToHistoryFile(User payer, List<Obligation> resolvedObligations) {
		String payments = resolvedObligations.stream()
				.map(Obligation::getPaymentByBorrowerMessage)
				.collect(Collectors.joining(System.lineSeparator()));
		if (!payments.isEmpty()) {
			HistoryFileHandler.addPayment(payer, payments + System.lineSeparator());
		}
	}

	private void addPaymentsByLenderToHistoryFile(User payer, List<Obligation> resolvedObligations) {
		String payments = resolvedObligations.stream()
				.map(Obligation::getPaymentByLenderMessage)
				.collect(Collectors.joining(System.lineSeparator()));
		if (!payments.isEmpty()) {
			HistoryFileHandler.addPayment(payer, payments + System.lineSeparator());
		}
	}

	/**
	 * Resolves as much as possible obligations of the borrower to the lender.
	 * 
	 * @param lenderName          The lender.
	 * @param borrowerName        The borrower.
	 * @param money               The amount of money the borrower has given. They
	 *                            are considered to be in the lander's currency.
	 * @param resolvedObligations Adds here the resolved obligations.
	 * @return The amount of money left after resolving the obligations.
	 */
	private double resolveObligations(String lenderName, String borrowerName, double money,
			List<Obligation> resolvedObligations) {
		List<Obligation> obligations = debts.get(lenderName).get(borrowerName);

		// If the borrower does not owe money to the lender
		if (obligations.isEmpty()) {
			return money;
		}

		Iterator<Obligation> iterator = obligations.iterator();
		while (iterator.hasNext() && money > ZERO) {
			Obligation obligation = iterator.next();

			double payedMoney = obligation.removeDebtLenderCurrency(money);
			resolvedObligations.add(obligation);
			if (obligation.getMoneyDebtLenderCurrency() == ZERO) {
				iterator.remove();
			}
			money -= payedMoney;
		}

		return money;
	}

	private Map<String, Map<String, List<Obligation>>> createMap(Set<User> users) {
		Map<String, Map<String, List<Obligation>>> obligations = new HashMap<>();
		for (User lender : users) {
			obligations.put(lender.getUsername(), new HashMap<>());
		}

		for (User lender : users) {
			Map<String, List<Obligation>> borrowers = obligations.get(lender.getUsername());
			for (User borrower : users) {
				if (!borrower.getUsername().equals(lender.getUsername())) {
					borrowers.put(borrower.getUsername(), new ArrayList<Obligation>());
				}
			}
		}

		return obligations;
	}

	private List<String> getStatusUserDebts(User user) {
		List<String> status = new ArrayList<>();
		for (Entry<String, Map<String, List<Obligation>>> entry : getDebts().entrySet()) {
			if (entry.getKey() != user.getUsername()) {
				entry.getValue().get(user.getUsername()).stream().forEach(obligation -> {
					status.add(YOU_OWE + obligation.getLender().getUsername() + " "
							+ obligation.getMoneyDebtBorrowerCurrency() + SPACE + user.getCurrency()
							+ (!obligation.getReason().isEmpty()
									? OPENING_BRACKET + obligation.getReason() + CLOSING_BRACKET
									: ""));
				});
			}
		}
		return status;
	}

	private List<String> getStatusUserCredits(User user) {
		List<String> status = new ArrayList<>();
		for (Entry<String, List<Obligation>> entry : getDebts().get(user.getUsername()).entrySet()) {
			entry.getValue().stream().forEach(obligation -> {
				status.add(
						entry.getKey() + OWES_YOU + obligation.getMoneyDebtLenderCurrency() + SPACE + user.getCurrency()
								+ (!obligation.getReason().isEmpty()
										? OPENING_BRACKET + obligation.getReason() + CLOSING_BRACKET
										: ""));
			});
		}
		return status;
	}
}
