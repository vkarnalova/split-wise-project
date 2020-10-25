package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions;

public class ExchangeRateClientException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9197282760807822651L;

	public ExchangeRateClientException() {
		super();
	}

	public ExchangeRateClientException(String errorMessage) {
		super(errorMessage);
	}

	public ExchangeRateClientException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}
}
