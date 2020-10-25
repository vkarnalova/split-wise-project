package bg.sofia.uni.fmi.mjt.splitwise.server.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeRate {
	private static final int DECIMAL_PLACES = 2;
	String baseCurrency;
	String toCurrency;
	double rate;

	public ExchangeRate(String baseCurrency, String toCurrency, double rate) {
		this.baseCurrency = baseCurrency;
		this.toCurrency = toCurrency;
		this.rate = rate;
	}

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public String getToCurrency() {
		return toCurrency;
	}

	public double getRate() {
		return rate;
	}

	public double convertMoney(double moneyBaseCurrency) {
		return BigDecimal.valueOf(moneyBaseCurrency * rate)
				.setScale(DECIMAL_PLACES, RoundingMode.HALF_DOWN)
				.doubleValue();
	}

}
