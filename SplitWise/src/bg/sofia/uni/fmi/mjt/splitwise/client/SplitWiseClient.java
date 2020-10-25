package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import bg.sofia.uni.fmi.mjt.splitwise.client.exceptions.ClientServerConnectionException;
import bg.sofia.uni.fmi.mjt.splitwise.client.file.LogFileClientHandler;

public class SplitWiseClient {
	private static final String PROBLEM_WITH_SERVER_MESSAGE = "Unable to communicate correctly with the server. ";
	private static final String ERROR_DURING_SLEEP_METHOD = "Error during sleep method while waiting for reader thread. ";
	private static final String TRY_AGAIN_LATER_MESSAGE = "Try again later or contact administrator by providing the logs in ";
	private static final String PROBLEM_OPENING_CHANNEL_MESSAGE = "A problem while getting ready to start"
			+ " communication with the server was detected.";
	private static final String PROMPT_MESSAGE = "=> ";
	private static final String LOGOUT = "logout";

	private static final int SERVER_PORT = 8888;
	private static final String SERVER_HOST = "localhost";
	private static final int BUFFER_SIZE = 4096;
	private static final int SLEEP_MILLIS = 200;

	/**
	 * Starts the client.
	 * 
	 * @param in  The stream used for collecting information about the commands from
	 *            the client.
	 * @param out The stream used for sending results from commands to the client.
	 */
	public void startClient(InputStream in, OutputStream out) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		try (SocketChannel socketChannel = SocketChannel.open()) {
			startClient(in, out, socketChannel, buffer, null);
		} catch (IOException e) {
			System.out.println(PROBLEM_OPENING_CHANNEL_MESSAGE + TRY_AGAIN_LATER_MESSAGE
					+ LogFileClientHandler.getLogFilesAbsolutePath());
			LogFileClientHandler.log(PROBLEM_OPENING_CHANNEL_MESSAGE + e.getStackTrace());
			throw new RuntimeException(PROBLEM_OPENING_CHANNEL_MESSAGE, e);
		}
	}

	/**
	 * Starts the client. This method was mainly creating in order for the client to
	 * be tested with mocked instances.
	 * 
	 * @param in                    The stream used for collecting information about
	 *                              the commands from the client.
	 * @param out                   The stream used for sending results from
	 *                              commands to the client.
	 * @param socketChannel         The socket channel used for communication.
	 * @param bufferSendInformation The buffer used for sending information to the
	 *                              server.
	 * @param bufferGetInformation  The buffer used for getting information to the
	 *                              server.
	 */
	public void startClient(InputStream in, OutputStream out, SocketChannel socketChannel,
			ByteBuffer bufferSendInformation, ByteBuffer bufferGetInformation) {
		try (Scanner scanner = new Scanner(in)) {
			socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

			ClientReaderThread clientReaderThread = new ClientReaderThread(socketChannel, bufferGetInformation, out);
			clientReaderThread.setDaemon(true);
			clientReaderThread.start();

			while (true) {
				out.write(PROMPT_MESSAGE.getBytes());
				String message = scanner.nextLine();

				bufferSendInformation.clear();
				bufferSendInformation.put(message.getBytes());
				bufferSendInformation.flip();
				socketChannel.write(bufferSendInformation);

				// Wait for the server to send respond. This is only used in order to ensure
				// proper ordering of the messages on the console.
				try {
					Thread.sleep(SLEEP_MILLIS);
				} catch (InterruptedException e) {
					System.out.println(PROBLEM_WITH_SERVER_MESSAGE + TRY_AGAIN_LATER_MESSAGE
							+ LogFileClientHandler.getLogFilesAbsolutePath());
					LogFileClientHandler.log(ERROR_DURING_SLEEP_METHOD + e.getStackTrace());
					throw new ClientServerConnectionException(ERROR_DURING_SLEEP_METHOD, e);
				}

				if (LOGOUT.equals(message)) {
					return;
				}
			}
		} catch (IOException e) {
			System.out.println(PROBLEM_WITH_SERVER_MESSAGE + TRY_AGAIN_LATER_MESSAGE
					+ LogFileClientHandler.getLogFilesAbsolutePath());
			LogFileClientHandler.log(PROBLEM_WITH_SERVER_MESSAGE + e.getStackTrace());
			throw new ClientServerConnectionException(PROBLEM_WITH_SERVER_MESSAGE, e);
		}
	}
}
