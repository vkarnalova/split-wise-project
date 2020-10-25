package bg.sofia.uni.fmi.mjt.splitwise.server.currency;

import java.util.Map;
import java.util.Optional;

public class ExchangeRates {
	Map<String, Double> rates;
	String base;

	public ExchangeRates(Map<String, Double> rates, String base) {
		this.rates = rates;
		this.base = base;
	}

	public Map<String, Double> getRates() {
		return rates;
	}

	public String getBase() {
		return base;
	}

	public Optional<ExchangeRate> getExchangeRate(String toCurrency) {
		Double rate = rates.get(toCurrency);
		if (rate == null) {
			Optional.empty();
		}
		return Optional.of(new ExchangeRate(base, toCurrency, rate));
	}
}
