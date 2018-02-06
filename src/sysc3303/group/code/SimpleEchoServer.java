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
   
   String reqType;

   public SimpleEchoServer()
   {
      try {
         receiveSocket = new DatagramSocket(6969);
         //Katie test
         
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

      // Wait until packet is received containing original WRQ or RRQ request from client
      try {        
         System.out.println("Waiting...");
         receiveSocket.receive(receivePacket);
	      if (!(checkRequest(data))) {
	    	  	/* *******
				TODO: send back ERROR request to client
	    	  	*/ 
	    	  	throw new IllegalArgumentException("Request is invalid");
	      } else {
	      		// Request is valid so create a new client connection thread and pass original request to it as data
	    	  	new Thread(new ClientConnection(data, receivePacket, receiveSocket)).start();	
	      }
         System.out.println("Creating new Client Connection Thread...");
      } catch (IOException e) {
         System.out.print("IO Exception: likely:");
         System.out.println("Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }
      

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
   

    // Function for creating a request -> will be used to send back ERR request if WRQ or RRQ is invalid
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
   

   /*
	Function to check incoming WRQ/RRQ request from client and verify it
   */
    public boolean checkRequest(byte msg[]) {
		byte zero = 0;
	    byte one = 1;
	    byte two = 2;
	    if (!(msg[0] == zero && msg[1] == one || msg[0] == zero && msg[1] == two)) {
		    return false;
	    }
	   
	    byte temp[] = new byte[100];
	    int i = 0;
	    while (msg[2+i] != zero) {
		    temp[i] = msg[2+i];
	    		i++;
	    }
	      
	    String file = new String(temp,0,temp.length);
	    if (file == "") {
			return false;
	    }
	   
	    if (msg[2+i] != zero) {
			return false;
	    }
	   
	    byte temp2[] = new byte[100];
	    int j = 0;
	    while (msg[3+i] != zero) {
		    temp2[j] = msg[3+i];
		    j++;
		    i++;
	    }
	   
	    String type = new String(temp2,0,temp2.length);
	    if (type == "") {
		    return false;
	    }
	   
	    if (msg[3+i] != zero) {
			return false;
	    } 
	   
	    return true;
    }

    public static void main( String args[] )
    {
    	SimpleEchoServer c = new SimpleEchoServer();
    	while(true){
    		c.receiveAndEcho(); 
        }
      
    }
}

