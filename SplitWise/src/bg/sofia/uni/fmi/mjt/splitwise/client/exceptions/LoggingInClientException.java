package bg.sofia.uni.fmi.mjt.splitwise.client.exceptions;

public class LoggingInClientException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8200356120549727795L;

	public LoggingInClientException() {
		super();
	}

	public LoggingInClientException(String errorMessage) {
		super(errorMessage); 
	}

	public LoggingInClientException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

}
