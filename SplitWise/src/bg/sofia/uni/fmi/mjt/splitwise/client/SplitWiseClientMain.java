package bg.sofia.uni.fmi.mjt.splitwise.client;

public class SplitWiseClientMain {
	public static void main(String... args) {
		SplitWiseClient chatClient = new SplitWiseClient();
		chatClient.startClient(System.in, System.out);
	}
}