package sysc3303.group.code;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ClientConnection implements Runnable {
	DatagramPacket sendReceivePacket;
	DatagramSocket sendReceiveSocket;
	
	public ClientConnection(DatagramPacket packet, DatagramSocket socket){
		sendReceivePacket = packet;
		sendReceiveSocket = socket;
	}
	
	@Override
	public void run() {
		// Process the received datagram.
		System.out.println("Client Connection Thread: Packet received");
	    System.out.println("From client: " + sendReceivePacket.getAddress());
	    System.out.println("Client port: " + sendReceivePacket.getPort());
	    int len = sendReceivePacket.getLength();
	    System.out.println("Length: " + len);
	    System.out.print("Containing: " );
	     
	    // Form a String from the byte array.
	    String received = new String(sendReceivePacket.getData());
	    System.out.println("--> Byte Form: " + sendReceivePacket.getData() + "\n" + "--> String Form: " + received + "\n");
	      
	    byte msg[] = sendReceivePacket.getData();
	     
	    // Slow things down (wait 5 seconds)
	    try {
	    	Thread.sleep(5000);
	    } catch (InterruptedException e ) {
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	    
	    sendReceivePacket = new DatagramPacket(msg, msg.length,
	    		sendReceivePacket.getAddress(), sendReceivePacket.getPort());

	    System.out.println( "Client Connection Thread: Sending packet");
	    System.out.println("To client: " + sendReceivePacket.getAddress());
	    System.out.println("Destination client port: " + sendReceivePacket.getPort());
	    len = sendReceivePacket.getLength();
	    System.out.println("Length: " + len);
	    System.out.print("Containing: ");
	    System.out.println("--> Byte Form: " + msg + "\n");
	    // or (as we should be sending back the same thing)
	    // System.out.println(received);

	    // Send the datagram packet to the client via the send socket. 
	    try {
	    	sendReceiveSocket.send(sendReceivePacket);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	System.exit(1);
	    }

	    System.out.println("Client Connection Thread: packet sent");  
	      
		
	}

}
