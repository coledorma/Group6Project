package sysc3303.group.code;
// SimpleEchoServer.java
// This class is the server side of a simple echo server based on
// UDP/IP. The server receives from a client a packet containing a character
// string, then echoes the string back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SimpleEchoServer {

   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendSocket, receiveSocket;

   public SimpleEchoServer()
   {
      try {
         // Construct a datagram socket and bind it to any available 
         // port on the local host machine. This socket will be used to
         // send UDP Datagram packets.
         //sendSocket = new DatagramSocket();

         // Construct a datagram socket and bind it to port 6969 
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets.
         receiveSocket = new DatagramSocket(6969);
         
         // to test socket timeout (2 seconds)
         //receiveSocket.setSoTimeout(2000);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      } 
   }

   public void receiveAndEcho()
   {
      // Construct a DatagramPacket for receiving packets up 
      // to 100 bytes long (the length of the byte array).

      byte data[] = new byte[100];
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Server: Waiting for Packet.\n");

      // Block until a datagram packet is received from receiveSocket.
      try {        
         System.out.println("Waiting..."); // so we know we're waiting
         receiveSocket.receive(receivePacket);
         System.out.println("Creating new Client Connection Thread...");
         new Thread(new ClientConnection(receivePacket, receiveSocket)).start();
      } catch (IOException e) {
         System.out.print("IO Exception: likely:");
         System.out.println("Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }
      
      /*
      // Process the received datagram.
      System.out.println("Server: Packet received");
      System.out.println("From client: " + receivePacket.getAddress());
      System.out.println("Client port: " + receivePacket.getPort());
      int len = receivePacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: " );

      // Form a String from the byte array.
      String received = new String(receivePacket.getData());
      System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String Form: " + received + "\n");
      
      byte msg[] = receivePacket.getData();
      
      boolean read = false;
      
      //Check for validity of request and type of request
      if (msg[0] == 0 && (msg[1] == 1 || msg[1] == 2)){
    	  int indexOfZero = 0;
    	  int lengthOfText = 0;
    	  for (int i = 2; i < msg.length; i++){
    		  lengthOfText++;
    		  if (msg[i] == 0){
    			  indexOfZero = i;
    			  break;
    		  }
    	  }
    	  String byteArray = new String(msg);
    	  if (byteArray.substring(0, lengthOfText).matches(".*[a-z].*")){ 
    		  if(msg[indexOfZero] == 0){
    			  int indexOfSecZero = 0;
    	    	  for (int j = indexOfZero+1; j < msg.length; j++){
    	    		  if (msg[j] == 0){
    	    			  indexOfSecZero = j;
    	    			  break;
    	    		  }
    	    	  }
    	    	  if (byteArray.substring(indexOfZero+1, indexOfSecZero).matches(".*[a-z].*")){
    	    		  if (msg[msg.length - 1] == 0){
    	    			  System.out.println("This is a correct request.");
    	    			  if (msg[1] == 1) {
    	    				  read = true;
    	    			  } else {
    	    				  read = false;
    	    			  }
    	    				  
    	    		  }
    	    	  }
    		  }
    	  }
      } else {
    	  try {
			throw new InterruptedException();
    	  } catch (InterruptedException e) {
			// TODO Auto-generated catch block
    		System.out.println("ERROR: This isn't a correct request.");
			e.printStackTrace();
			System.exit(1);
    	  }
    	  
      }
      
      // Slow things down (wait 5 seconds)
      try {
          Thread.sleep(5000);
      } catch (InterruptedException e ) {
          e.printStackTrace();
          System.exit(1);
      }
 
      // Create a new datagram packet containing the string received from the client.

      // Construct a datagram packet that is to be sent to a specified port 
      // on a specified host.
      // The arguments are:
      //  data - the packet data (a byte array). This is the packet data
      //         that was received from the client.
      //  receivePacket.getLength() - the length of the packet data.
      //    Since we are echoing the received packet, this is the length 
      //    of the received packet's data. 
      //    This value is <= data.length (the length of the byte array).
      //  receivePacket.getAddress() - the Internet address of the 
      //     destination host. Since we want to send a packet back to the 
      //     client, we extract the address of the machine where the
      //     client is running from the datagram that was sent to us by 
      //     the client.
      //  receivePacket.getPort() - the destination port number on the 
      //     destination host where the client is running. The client
      //     sends and receives datagrams through the same socket/port,
      //     so we extract the port that the client used to send us the
      //     datagram, and use that as the destination port for the echoed
      //     packet.
      
      byte newMsg[];
      
      byte zero = 0;
      byte one = 1;
      byte three = 3;
      byte four = 4;
      
      if (read){
    	  newMsg = createRequest(three,one);
      } else {
    	  newMsg = createRequest(four,zero);
      }
      
      sendPacket = new DatagramPacket(newMsg, newMsg.length,
                               receivePacket.getAddress(), receivePacket.getPort());

      System.out.println( "Server: Sending packet");
      System.out.println("To client: " + sendPacket.getAddress());
      System.out.println("Destination client port: " + sendPacket.getPort());
      len = sendPacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: ");
      System.out.println("--> Byte Form: " + newMsg[0] + newMsg[1] + newMsg[2] + newMsg[3] + "\n");
      // or (as we should be sending back the same thing)
      // System.out.println(received);
      
      try {
		sendSocket = new DatagramSocket();
      } catch (SocketException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
      }
        
      // Send the datagram packet to the client via the send socket. 
      try {
         sendSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Server: packet sent");
      
      */

      // We're finished, so close the sockets.
      //sendSocket.close();
      //receiveSocket.close();
      
      //Read input from terminal
      Scanner readInput = new Scanner(System.in);
	  System.out.println("Would you like to shutdown the server (Y/N)? ");
	  String shutdownAnswer = readInput.next(); // Scans the next token of the input as an int.
	  readInput.close();

	  //Wait for answer from user
	  if (shutdownAnswer.equals("Y") || shutdownAnswer.equals("y")){
		  System.out.println("Shutting the server down.");
		  System.exit(1);
	  } 
   }
   
   public byte[] createRequest(byte thirdByte, byte fourthByte){
	   ByteArrayOutputStream output = new ByteArrayOutputStream();
	   byte zeroByte = 0;
	   
	   output.write(zeroByte);
	   output.write(thirdByte);
	   output.write(zeroByte);
	   output.write(fourthByte);
	   
	   
	   byte msg[] = output.toByteArray();
	   return msg;
   }

   public static void main( String args[] )
   {
      SimpleEchoServer c = new SimpleEchoServer();
      while(true){
    	  c.receiveAndEcho(); 
      }
      
   }
}

