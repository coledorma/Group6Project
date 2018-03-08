package sysc3303.group.code;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class DuplicateConnection implements Runnable {
	
	DatagramPacket sendPacket;
	DatagramSocket sendSocket;
	int len;
	String delay;
	
	//is passed datagram packet information and time delay
	public DuplicateConnection(DatagramPacket packet, String packetDelayTime) {
       
		System.out.println("Duplicate Connection Thread Running...");
		sendPacket = packet;
		delay = packetDelayTime;
		
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
		
	public void run() {
		
		 //Delay for user entered amount of time
		  System.out.println("Waiting " + delay + " milliseconds until duplicate packet being sent...");
	      try {
	          Thread.sleep(Integer.parseInt(delay));	//put thread to sleep
	      } catch (InterruptedException e ) {
	          e.printStackTrace();
	          System.exit(1);
	      }
	       
	      //prepare to send packet
	       System.out.println( "Intermediate Host: Sending duplicate packet");
	       System.out.println("To server: " + sendPacket.getAddress());
	       System.out.println("Destination server port: " + sendPacket.getPort());
	       len = sendPacket.getLength();
	       System.out.println("Length: " + len);
	       System.out.print("Containing: ");
	       String received = new String(sendPacket.getData());  
	       System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");
	         
	       // Send the datagram packet to the client via the send socket. 
	       try {
	          sendSocket.send(sendPacket);
	       } catch (IOException e) {
	          e.printStackTrace();
	          System.exit(1);
	       }
	       
	      //packet has been sent
		  System.out.println("Intermediate Host: Duplicate packet sent from Duplicate Connection Thread");
			
		 //stop thread?
	}
}
