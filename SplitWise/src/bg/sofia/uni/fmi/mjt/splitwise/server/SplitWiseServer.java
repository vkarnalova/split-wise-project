package bg.sofia.uni.fmi.mjt.splitwise.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import bg.sofia.uni.fmi.mjt.splitwise.server.commands.CommandExecutor;
import bg.sofia.uni.fmi.mjt.splitwise.server.files.LogFileServerHandler;

public class SplitWiseServer {
	private static final String WAITING_CONNECTIONS_ERROR_MESSAGE = "Error while waiting for new connections. ";
	private static final String MESSAGE_COULD_NOT_BE_SEND_USER_MESSAGE = "Message could not be send to user. ";
	private static final String PROBLEM_OPENNING_NIO_OBJECTS_MESSAGE = "Problem while openning nio objects. ";
	private static final String SERVER_PROBLEM_DETECTED_MESSAGE = "A problem with the server was detected. ";
	private static final String ERROR_ACCEPT_OPERATION_MESSAGE = "Error while accepting connection. ";
	private static final String NOTHING_TO_READ_MESSAGE = "There is nothing to read. Closing channel. ";
	private static final String SERVER_COULD_NOT_BE_CREATED_MESSAGE = "Server could not be created.";
	private static final int ZERO = 0;

	private static final String SERVER_HOST = "localhost";
	public static final int SERVER_PORT = 8888;
	private static final int BUFFER_SIZE = 4096;
	private static final int SLEEP_MILLIS = 200;

	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private ByteBuffer buffer;
	private CommandExecutor commandExecutor;

	public SplitWiseServer() {
		commandExecutor = new CommandExecutor();
		createNioObjects();
	}

	/**
	 * Starts the split wise server.
	 */
	public void startSplitWiseServer() {
		try {
			while (true) {
				int readyChannels = selector.select();
				if (readyChannels == ZERO) {
					try {
						Thread.sleep(SLEEP_MILLIS);
					} catch (InterruptedException e) {
						System.out.println(WAITING_CONNECTIONS_ERROR_MESSAGE);
						LogFileServerHandler.log(WAITING_CONNECTIONS_ERROR_MESSAGE + e.getStackTrace());
						e.printStackTrace();
					}
					continue;
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					performOperation(key);
					keyIterator.remove();
				}
			}
		} catch (IOException e) {
			System.out.println(SERVER_PROBLEM_DETECTED_MESSAGE);
			LogFileServerHandler.log(SERVER_PROBLEM_DETECTED_MESSAGE + e.getStackTrace());
			throw new RuntimeException(SERVER_PROBLEM_DETECTED_MESSAGE, e);
		}
	}

	/**
	 * Performs an operation depending on the type of the key.
	 * 
	 * @param key The key.
	 * @throws IOException
	 */
	private void performOperation(SelectionKey key) throws IOException {
		if (key.isReadable()) {
			performReadOperation(key);
		} else if (key.isAcceptable()) {
			performAcceptOperation(key);
		}
	}

	private void performReadOperation(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		buffer.clear();
		int readBytes = socketChannel.read(buffer);
		if (readBytes <= ZERO) {
			System.out.println(NOTHING_TO_READ_MESSAGE);
			socketChannel.close();
			return;
		}

		buffer.flip();
		String message = new String(buffer.array(), ZERO, buffer.limit());

		String answer = commandExecutor.execute(socketChannel, message);
		sendMessage(socketChannel, answer);
	}

	private void sendMessage(SocketChannel socketChannelReceiver, String message) {
		buffer.clear();
		buffer.put(message.getBytes());
		buffer.flip();
		try {
			socketChannelReceiver.write(buffer);
		} catch (IOException e) {
			System.out.println(MESSAGE_COULD_NOT_BE_SEND_USER_MESSAGE);
			LogFileServerHandler.log(MESSAGE_COULD_NOT_BE_SEND_USER_MESSAGE + e.getStackTrace());
		}
	}

	private void performAcceptOperation(SelectionKey key) {
		ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
		try {
			SocketChannel accept = socketChannel.accept();
			accept.configureBlocking(false);
			accept.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			System.out.println(ERROR_ACCEPT_OPERATION_MESSAGE);
			LogFileServerHandler.log(MESSAGE_COULD_NOT_BE_SEND_USER_MESSAGE + e.getStackTrace());
		}
	}

	private void createNioObjects() {
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
			serverSocketChannel.configureBlocking(false);

			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			buffer = ByteBuffer.allocate(BUFFER_SIZE);
		} catch (IOException e) {
			System.out.println(SERVER_COULD_NOT_BE_CREATED_MESSAGE);
			LogFileServerHandler.log(PROBLEM_OPENNING_NIO_OBJECTS_MESSAGE + e.getStackTrace());
			throw new RuntimeException(PROBLEM_OPENNING_NIO_OBJECTS_MESSAGE, e);
		}
	}
}
