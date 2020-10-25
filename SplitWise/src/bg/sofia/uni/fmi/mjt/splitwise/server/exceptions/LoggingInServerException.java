package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions;

public class LoggingInServerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4174588713659110560L;

	public LoggingInServerException() {
		super();
	}

	public LoggingInServerException(String errorMessage) {
		super(errorMessage);
	}

	public LoggingInServerException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}
}
