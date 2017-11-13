package exia.ipc.client.protocol;

import exia.ipc.client.ChatClient;

public interface IClientProtocol {
	
	public void setClient(ChatClient client);

	public String sayHelloToServer(String clientName);

	public String sendMessageToServer(String log);
	
	public String processServerInput(String inputLine);

}
