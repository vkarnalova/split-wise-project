package bg.sofia.uni.fmi.mjt.splitwise.server.currency;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;

import com.google.gson.Gson;

import bg.sofia.uni.fmi.mjt.splitwise.server.exceptions.ExchangeRateClientException;
import bg.sofia.uni.fmi.mjt.splitwise.server.files.LogFileServerHandler;

public class ExchangeRatesClient {
	private static final String EXCHANGE_RATE_CLIENT_ERROR_MESSAGE = "Error with exchange rate client occurred";
	private static final String API_URL = "https://api.exchangeratesapi.io";
	private static final double ONE = 1;

	private static ExchangeRatesClient exchangeRatesClient = new ExchangeRatesClient();
	private HttpClient client;

	private ExchangeRatesClient() {
		client = HttpClient.newHttpClient();
	}

	public static ExchangeRatesClient getInstance() {
		return exchangeRatesClient;
	}

	public void setHttpClient(HttpClient client) {
		this.client = client;
	}

	public Optional<ExchangeRate> getExchangeRate(String baseCurrency, String toCurrency) {
		if (baseCurrency.equals(toCurrency)) {
			return Optional.of(new ExchangeRate(baseCurrency, toCurrency, ONE));
		}

		String URL = API_URL + "/latest?symbols=" + toCurrency + "&base=" + baseCurrency + ";";

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();
		try {
			String jsonResponse = client.send(request, BodyHandlers.ofString()).body();
			Gson gson = new Gson();
			ExchangeRates exchangeRates = gson.fromJson(jsonResponse, ExchangeRates.class);
			if (exchangeRates.getRates() == null) {
				return Optional.empty();
			}
			return exchangeRates.getExchangeRate(toCurrency);
		} catch (Exception e) {
			LogFileServerHandler.log(EXCHANGE_RATE_CLIENT_ERROR_MESSAGE + "while trying to convert from" + baseCurrency
					+ " to " + toCurrency + System.lineSeparator() + e.getStackTrace());
			System.out.println();
			throw new ExchangeRateClientException(EXCHANGE_RATE_CLIENT_ERROR_MESSAGE, e);
		}
	}

}
