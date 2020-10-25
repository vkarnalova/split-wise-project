package bg.sofia.uni.fmi.mjt.splitwise.server.exceptions;

public class IllegalCommandTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -82745225532034686L;

	public IllegalCommandTypeException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

	public IllegalCommandTypeException(String errorMessage) {
		super(errorMessage);
	}

}
