package bg.sofia.uni.fmi.mjt.splitwise.server.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRate;
import bg.sofia.uni.fmi.mjt.splitwise.server.currency.ExchangeRatesClient;
import bg.sofia.uni.fmi.mjt.splitwise.server.files.LogFileServerHandler;

public class Obligation {
	private static final String NULL_REFERENCE_ERROR = "Null reference to money in different currencies in obligation.";
	private static final String YOU_PAYED = " you payed ";
	private static final String PERIOD = ".";
	private static final String SPACE = " ";
	private static final int DECIMAL_PLACES = 2;
	private static final double ZERO = 0.0;
	private static final String OPENING_BRACKET = " [";
	private static final String CLOSING_BRACKET = "]";

	private User lender;
	private User borrower;
	private String reason;
	private Map<User, Double> moneyDebtDifferentCurrency;
	private Map<User, Double> lastPaidMoneyDifferentCurrency;
	private Map<User, ExchangeRate> exchangeRates;
	ExchangeRatesClient client;

	public Obligation(User lender, User borrower, double moneyDebtorCurrency, String reason) {
		this.lender = lender;
		this.borrower = borrower;
		this.reason = reason;

		client = ExchangeRatesClient.getInstance();
		createExchangeRates(lender, borrower);

		moneyDebtDifferentCurrency = new HashMap<>();
		moneyDebtDifferentCurrency.put(lender, exchangeRates.get(borrower).convertMoney(moneyDebtorCurrency));
		moneyDebtDifferentCurrency.put(borrower, moneyDebtorCurrency);

		lastPaidMoneyDifferentCurrency = new HashMap<>();
		lastPaidMoneyDifferentCurrency.put(lender, ZERO);
		lastPaidMoneyDifferentCurrency.put(borrower, ZERO);
	}

	private void createExchangeRates(User creditor, User debtor) {
		exchangeRates = new HashMap<>();
		ExchangeRate exchangeRateCreditor = client.getExchangeRate(creditor.getCurrency(), debtor.getCurrency()).get();
		exchangeRates.put(creditor, exchangeRateCreditor);
		ExchangeRate exchangeRateDebtor = client.getExchangeRate(debtor.getCurrency(), creditor.getCurrency()).get();
		exchangeRates.put(debtor, exchangeRateDebtor);
	}

	public User getLender() {
		return lender;
	}

	public User getBorrower() {
		return borrower;
	}

	public double getMoneyDebtBorrowerCurrency() {
		return moneyDebtDifferentCurrency.get(borrower);
	}

	public double getMoneyDebtLenderCurrency() {
		return moneyDebtDifferentCurrency.get(lender);
	}

	public double getLastPaidMoneyBorrowerCurrency() {
		return lastPaidMoneyDifferentCurrency.get(borrower);
	}

	public double getLastPaidMoneyLenderCurrency() {
		return lastPaidMoneyDifferentCurrency.get(lender);
	}

	public String getReason() {
		return reason;
	}

	public double removeDebtLenderCurrency(double amount) {
		Double moneyDebtCreditorCurrency = moneyDebtDifferentCurrency.get(lender);
		if (moneyDebtCreditorCurrency - amount < ZERO) {
			amount = moneyDebtCreditorCurrency;
			moneyDebtDifferentCurrency.put(lender, ZERO);
			moneyDebtDifferentCurrency.put(borrower, ZERO);
		} else {
			moneyDebtCreditorCurrency -= amount;
			moneyDebtDifferentCurrency.put(lender, moneyDebtCreditorCurrency);
			moneyDebtDifferentCurrency.put(borrower,
					roundMoney(moneyDebtCreditorCurrency * exchangeRates.get(lender).getRate()));
		}

		lastPaidMoneyDifferentCurrency.put(lender, amount);
		lastPaidMoneyDifferentCurrency.put(borrower, roundMoney(amount * exchangeRates.get(lender).getRate()));
		return amount;
	}

	/**
	 * Creates a message when a payment in the obligation was added by the lender
	 * with payed command. The format of the message is "You payed <lender name>
	 * <money> <borrower currency> [<reason>].".
	 * 
	 * @return The message
	 */
	public String getPaymentByBorrowerMessage() {
		return LocalDate.now().toString() + YOU_PAYED + lender.getUsername() + SPACE + getLastPaidMoneyBorrowerCurrency()
				+ SPACE + borrower.getCurrency()
				+ (!getReason().isEmpty() ? OPENING_BRACKET + getReason() + CLOSING_BRACKET : "") + PERIOD;
	}

	/**
	 * Creates a message when a payment in the obligation was added by the borrower
	 * with split command. The format of the message is "You payed <borrower name>
	 * <money> <lender currency> [<reason>].".
	 * 
	 * @return The message
	 */
	public String getPaymentByLenderMessage() {
		return LocalDate.now().toString() + YOU_PAYED + borrower.getUsername() + SPACE
				+ getLastPaidMoneyLenderCurrency() + SPACE + lender.getCurrency()
				+ (!getReason().isEmpty() ? OPENING_BRACKET + getReason() + CLOSING_BRACKET : "") + PERIOD;
	}

	public void switchCurrency(User user, ExchangeRate rate) {
		Double money = moneyDebtDifferentCurrency.get(user);
		if (money == null) {
			System.out.println(NULL_REFERENCE_ERROR);
			LogFileServerHandler.log(
					NULL_REFERENCE_ERROR + " Users: " + lender.getUsername() + ", " + borrower.getUsername() + ".");
			throw new IllegalArgumentException(NULL_REFERENCE_ERROR);
		}
		moneyDebtDifferentCurrency.put(user, roundMoney(money * rate.getRate()));
		lastPaidMoneyDifferentCurrency.put(user, roundMoney(lastPaidMoneyDifferentCurrency.get(user) * rate.getRate()));

		updateExchangeRate();
	}

	private void updateExchangeRate() {
		exchangeRates.put(lender, client.getExchangeRate(lender.getCurrency(), borrower.getCurrency()).get());
		exchangeRates.put(borrower, client.getExchangeRate(borrower.getCurrency(), lender.getCurrency()).get());
	}

	private double roundMoney(double money) {
		return BigDecimal.valueOf(money).setScale(DECIMAL_PLACES, RoundingMode.HALF_DOWN).doubleValue();
	}

}
