package bg.sofia.uni.fmi.mjt.splitwise.client.exceptions;

public class ClientServerConnectionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8389874977183244955L;

	public ClientServerConnectionException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

}
