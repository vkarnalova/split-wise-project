package bg.sofia.uni.fmi.mjt.splitwise.server.currency;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeRatesClientTest {
	@Mock
	private HttpClient httpClientMock;

	@Mock
	private HttpResponse<String> httpResponseMock;

	private ExchangeRatesClient client;

	@Before
	public void setUp() {
		client = ExchangeRatesClient.getInstance();
		client.setHttpClient(httpClientMock);
	}

	@Test
	public void testGetExchangeRateEmptyOptional() throws Exception {
		when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any()))
				.thenReturn(httpResponseMock);
		when(httpResponseMock.body()).thenReturn("{\"error\":\"Base 'smth' is not supported.\"}");

		Optional<ExchangeRate> actual = client.getExchangeRate("smth", "smthelse");
		assertTrue(actual.isEmpty());
	}

	@Test
	public void testGetEchangeRate() throws Exception {
		when(httpClientMock.send(Mockito.any(HttpRequest.class), ArgumentMatchers.<BodyHandler<String>>any()))
				.thenReturn(httpResponseMock);
		when(httpResponseMock.body()).thenReturn("{\"rates\":{\"EUR\":0.5112997239},\"base\":\"BGN\"}");

		Optional<ExchangeRate> actual = client.getExchangeRate("BGN", "EUR");
		assertTrue(actual.isPresent());
		assertTrue(actual.get().getBaseCurrency().equals("BGN"));
		assertTrue(actual.get().getToCurrency().equals("EUR"));
		assertTrue(actual.get().getRate() == 0.5112997239);
	}

	@Test
	public void testGetEchangeRateEqualCurrancies() throws Exception {
		Optional<ExchangeRate> actual = client.getExchangeRate("BGN", "BGN");
		assertTrue(actual.isPresent());
		assertTrue(actual.get().getRate() == 1.0);
	}
}
