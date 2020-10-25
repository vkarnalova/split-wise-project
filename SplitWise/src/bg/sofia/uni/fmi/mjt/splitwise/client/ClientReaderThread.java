package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import bg.sofia.uni.fmi.mjt.splitwise.client.exceptions.ClientServerConnectionException;
import bg.sofia.uni.fmi.mjt.splitwise.client.file.LogFileClientHandler;

public class ClientReaderThread extends Thread {
	private static final String DISCONNECTED_FROM_SERVER = "Disconnected from server";
	private static final int BUFFER_SIZE = 4096;
	private static final String BUFFER_COULD_NOT_BE_FILLED = "Buffer could not be filled.";
	private static final String OUTPUT_PROBLEM = "Problem with writing to output was detected.";
	private static final String PROBLEM_WITH_SERVER_MESSAGE = "Unable to communicate correctly with the server. ";
	private static final String TRY_AGAIN_LATER_MESSAGE = "Try again later or contact administrator by providing the logs in ";

	private SocketChannel socketChannel;
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private OutputStream output;

	public ClientReaderThread(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
		this.output = System.out;
	}

	public ClientReaderThread(SocketChannel socketChannel, ByteBuffer buffer, OutputStream output) {
		this.socketChannel = socketChannel;
		this.buffer = (buffer != null) ? buffer : ByteBuffer.allocate(BUFFER_SIZE);
		this.output = output;
	}

	/**
	 * Receives messages from the server.
	 */
	@Override
	public void run() {
		while (true) {
			buffer.clear();
			try {
				socketChannel.read(buffer);
			} catch (IOException e) {
				handleException(e, BUFFER_COULD_NOT_BE_FILLED);
			}
			buffer.flip();
			String reply = new String(buffer.array(), 0, buffer.limit());

			try {
				output.write((reply + System.lineSeparator()).getBytes());
			} catch (IOException e) {
				handleException(e, OUTPUT_PROBLEM);
			}

			if (reply.equals(DISCONNECTED_FROM_SERVER)) {
				break;
			}
		}
	}

	private void handleException(IOException e, String messageToLog) {
		System.out.println(
				PROBLEM_WITH_SERVER_MESSAGE + TRY_AGAIN_LATER_MESSAGE + LogFileClientHandler.getLogFilesAbsolutePath());
		LogFileClientHandler.log(messageToLog + e.getStackTrace());
		throw new ClientServerConnectionException(messageToLog, e);
	}
}
