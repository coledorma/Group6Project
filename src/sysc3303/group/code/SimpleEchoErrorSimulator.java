package sysc3303.group.code;
// SimpleEchoServer.java
// This class is the server side of a simple echo server based on
// UDP/IP. The server receives from a client a packet containing a character
// string, then echoes the string back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Scanner;

public class SimpleEchoErrorSimulator {

   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendReceiveSocket, receiveSocket;
   String packetType = null, packetDelayTime = null;
   Boolean duplicateSim = false, delaySim = false, lostSim = false;
   Boolean runSim = false;
   byte zero = 0;
   byte RRQ = 1;
   byte WRQ = 2;
   byte DATA = 3;
   byte ACK = 4;
   byte[] packetNumByteArray = {zero,zero};
   
   int ccPort;

   public SimpleEchoErrorSimulator()
   {
      ccPort = 0;
      try {
         // Construct a datagram socket and bind it to any available 
         // port on the local host machine. This socket will be used to
         // send UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();

         // Construct a datagram socket and bind it to port 2323 
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets.
         receiveSocket = new DatagramSocket(2323);
         
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
      

      byte data[] = new byte[516];
      receivePacket = new DatagramPacket(data, data.length);
      System.out.println("Intermediate Host: Waiting for Packet.\n");
      

      // Block until a datagram packet is received from receiveSocket.
      try {        
         System.out.println("Waiting..."); // so we know we're waiting
         //System.out.println(receiveSocket.getPort());
         receiveSocket.receive(receivePacket);
      } catch (IOException e) {
         System.out.print("IO Exception: likely:");
         System.out.println("Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }

      // Process the received datagram.
      System.out.println("Intermediate Host: Packet received");
      System.out.println("From Client: " + receivePacket.getAddress());
      InetAddress clientAddress = receivePacket.getAddress();
      System.out.println("Client port: " + receivePacket.getPort());
      int clientPort = receivePacket.getPort();
      int len = receivePacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: " );

      // Form a String from the byte array.
      String received = new String(receivePacket.getData());   
      System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String Form: " + receivePacket.getData()[0] + receivePacket.getData()[1] + receivePacket.getData()[2] + receivePacket.getData()[3] + "\n");
      
      byte[] receivedByteArray = {receivePacket.getData()[0],receivePacket.getData()[1],receivePacket.getData()[2],receivePacket.getData()[3]};
      
      //Check to see if the received packet is the same as the inputed simulation packet
      Boolean isSimPacket = false;
      if(runSim) {
    	  isSimPacket = checkIfSimPacket(receivedByteArray,packetType, packetNumByteArray);  
      }
      
      if (isSimPacket){
    	  if (lostSim) {
    		  System.out.println("This packet equals the simulation entered packet.");
    		//TODO: Lose packet implementation
    	  } else if (delaySim) {
    		  System.out.println("This packet equals the simulation entered packet.");
    		  //Delay packet implementation
    		  //Wait the inputed simulation amount of time entered
    		  System.out.println("Delaying " + Integer.parseInt(packetDelayTime) + " milliseconds until packet being sent...");
    	      try {
    	          Thread.sleep(Integer.parseInt(packetDelayTime));
    	      } catch (InterruptedException e ) {
    	          e.printStackTrace();
    	          System.exit(1);
    	      }
    	  }
      }
      
      // Slow things down (wait 5 seconds)
      try {
          Thread.sleep(5000);
      } catch (InterruptedException e ) {
          e.printStackTrace();
          System.exit(1);
      }
      /*//Check if this is a new RRQ/WRQ and forwards message to the listener port for the server
        byte zero = 0;
	    byte one = 1;
	    byte two = 2;
	    if (!(msg[0] == zero && msg[1] == one || msg[0] == zero && msg[1] == two)) {
		    ccPort = 0;
	    }
       */

      if (ccPort == 0) {
         sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                  receivePacket.getAddress(), 6969);
      } else {
        sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                  receivePacket.getAddress(), ccPort);
      }
      

      System.out.println( "Intermediate Host: Sending packet");
      System.out.println("To server: " + sendPacket.getAddress());
      System.out.println("Destination server port: " + sendPacket.getPort());
      len = sendPacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: ");
      received = new String(sendPacket.getData());  
      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + receivePacket.getData()[0] + receivePacket.getData()[1] + receivePacket.getData()[2] + receivePacket.getData()[3] + "\n");
      System.out.println("DATA: "+ new String(data));
        
      // Send the datagram packet to the client via the send socket. 
      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Intermediate Host: packet sent");
      
      if(isSimPacket){
    	  if (duplicateSim){
    		  System.out.println("This packet equals the simulation entered packet.");
    		  
    		  // Duplicate packet implementation
    		  
    		  //Wait the inputed simulation amount of time entered
    		  System.out.println("Waiting " + packetDelayTime + " milliseconds until duplicate packet being sent...");
    	      try {
    	          Thread.sleep(Integer.parseInt(packetDelayTime));
    	      } catch (InterruptedException e ) {
    	          e.printStackTrace();
    	          System.exit(1);
    	      }
    		  
    	      if (ccPort == 0) {
    	          sendPacket = new DatagramPacket(data, receivePacket.getLength(),
    	                   receivePacket.getAddress(), 6969);
    	       } else {
    	         sendPacket = new DatagramPacket(data, receivePacket.getLength(),
    	                   receivePacket.getAddress(), ccPort);
    	       }
    	       

    	       System.out.println( "Intermediate Host: Sending duplicate packet");
    	       System.out.println("To server: " + sendPacket.getAddress());
    	       System.out.println("Destination server port: " + sendPacket.getPort());
    	       len = sendPacket.getLength();
    	       System.out.println("Length: " + len);
    	       System.out.print("Containing: ");
    	       received = new String(sendPacket.getData());  
    	       System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");
    	         
    	       // Send the datagram packet to the client via the send socket. 
    	       try {
    	          sendReceiveSocket.send(sendPacket);
    	       } catch (IOException e) {
    	          e.printStackTrace();
    	          System.exit(1);
    	       }

    		  System.out.println("Intermediate Host: Duplicate packet sent");
    	  }
      }
      
      
      //Receiving message from server-------------------------------------------------

      byte data2[] = new byte[516];
      receivePacket = new DatagramPacket(data2, data2.length);
      System.out.println("Intermediate Host: Waiting for Packet.\n");

      // Block until a datagram packet is received from receiveSocket.
      try {        
         System.out.println("Waiting..."); // so we know we're waiting
         System.out.println(sendReceiveSocket.getLocalPort());
         sendReceiveSocket.receive(receivePacket);
      } catch (IOException e) {
         System.out.print("IO Exception: likely:");
         System.out.println("Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }

      // Process the received datagram.
      System.out.println("Intermediate Host: Packet received");
      System.out.println("From Server: " + receivePacket.getAddress());
      System.out.println("Server port: " + receivePacket.getPort());
      ccPort = receivePacket.getPort();
      int len2 = receivePacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: " );

      // Form a String from the byte array.
      String received2 = new String(receivePacket.getData());   
      System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String Form: " + receivePacket.getData()[0] + receivePacket.getData()[1] + receivePacket.getData()[2] + receivePacket.getData()[3] + "\n");
      	
      byte[] secReceivedByteArray = {receivePacket.getData()[0],receivePacket.getData()[1],receivePacket.getData()[2],receivePacket.getData()[3]};
      
      //Check to see if the received packet is the same as the inputed simulation packet
      if(runSim) {
    	  isSimPacket = checkIfSimPacket(receivedByteArray,packetType, packetNumByteArray);  
      }
      
      if (isSimPacket){
    	  if (lostSim) {
    		  System.out.println("This packet equals the simulation entered packet.");
    		  //TODO: Lose packet implementation
    	  } else if (delaySim) {
    		  System.out.println("This packet equals the simulation entered packet.");
    		  //Delay packet implementation
    		  //Wait the inputed simulation amount of time entered
    		  System.out.println("Delaying " + Integer.parseInt(packetDelayTime) + " milliseconds until packet being sent...");
    	      try {
    	          Thread.sleep(Integer.parseInt(packetDelayTime));
    	      } catch (InterruptedException e ) {
    	          e.printStackTrace();
    	          System.exit(1);
    	      }
    	  }
      }
      
      
      sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                               clientAddress, clientPort);

      System.out.println( "Intermediate Host: Sending packet");
      System.out.println("To client: " + sendPacket.getAddress());
      System.out.println("Destination client port: " + sendPacket.getPort());
      len = sendPacket.getLength();
      System.out.println("Length: " + len);
      System.out.print("Containing: ");
      received = new String(sendPacket.getData());
      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + sendPacket.getData()[0] + sendPacket.getData()[1] + sendPacket.getData()[2] + sendPacket.getData()[3] + "\n");
      
      System.out.println("DATA: "+ new String(data2));
        
      // Send the datagram packet to the client via the send socket. 
      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Intermediate Host: packet sent");
      
      if(isSimPacket){
    	  if (duplicateSim){
    		  System.out.println("This packet equals the simulation entered packet.");
    		  
    		  // Duplicate packet implementation
    		  
    		  //Wait the inputed simulation amount of time entered
    		  System.out.println("Waiting " + Integer.parseInt(packetDelayTime) + " milliseconds until duplicate packet being sent...");
    	      try {
    	          Thread.sleep(Integer.parseInt(packetDelayTime));
    	      } catch (InterruptedException e ) {
    	          e.printStackTrace();
    	          System.exit(1);
    	      }
    		  
    		  sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                      clientAddress, clientPort);

    		  System.out.println( "Intermediate Host: Sending duplicate packet");
    		  System.out.println("To client: " + sendPacket.getAddress());
    		  System.out.println("Destination client port: " + sendPacket.getPort());
    		  len = sendPacket.getLength();
    		  System.out.println("Length: " + len);
    		  System.out.print("Containing: ");
    		  received = new String(sendPacket.getData());
    		  System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + sendPacket.getData()[0] + sendPacket.getData()[1] + sendPacket.getData()[2] + sendPacket.getData()[3] + "\n");
	
    		  // Send the duplicate datagram packet to the client via the send socket. 
    		  try {
    			  sendReceiveSocket.send(sendPacket);
    		  } catch (IOException e) {
    			  e.printStackTrace();
    			  System.exit(1);
    		  }

    		  System.out.println("Intermediate Host: Duplicate packet sent");
    	  }
      }
      
      /*// Slow things down (wait 5 seconds)
      try {
          Thread.sleep(5000);
      } catch (InterruptedException e ) {
          e.printStackTrace();
          System.exit(1);
      }*/

   }
   
   public void testMenu(){
	   Scanner readUserInput = new Scanner(System.in);
	   System.out.println("Would you like to test out a timeout/retransmission situation? (Y/N)\n");
	   String requestAnswer = readUserInput.next(); // Scans the next token of the input as an int.
	   //readUserInput.close();
	   
	   //Wait for answer from user for read or write request
	   if (requestAnswer.equals("Y") || requestAnswer.equals("y")){
		   //Wait for sim number
		   runSim = true;
		   Scanner readSimInput = new Scanner(System.in);
		   System.out.println("Which of the following would you like to simulate?\n(1) - Lose A Packet\n(2) - Delay A Packet\n(3) - Duplicate A Packet\n ");
		   String simNum = readSimInput.next(); // Scans the next token of the input as an int.
		   
		   if (simNum.equals("1")){
			   lostSim = true;
			   
			   //Wait for packet type
			   Scanner readPacketType = new Scanner(System.in);
			   System.out.println("What type of packet would you like to lose? (WRQ/RRQ/DATA/ACK)\n");
			   packetType = readPacketType.next(); // Scans the next token of the input as an int.
			   
			   if (packetType.equals("DATA") || packetType.equals("ACK")){
				   //Wait for packet number
				   Scanner readPacketNum = new Scanner(System.in);
				   System.out.println("What is the first byte of the block you would like to lose? (1/2/3/etc...)\n");
				   byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.
				   
				   //Wait for packet number
				   Scanner readPacketSecondNum = new Scanner(System.in);
				   System.out.println("What is the second byte of the block you would like to lose? (1/2/3/etc...)\n");
				   byte tempSecond = readPacketSecondNum.nextByte(); // Scans the next token of the input as an int.
				   
				   packetNumByteArray[0] = tempFirst;
				   packetNumByteArray[1] = tempSecond;
			   }

		   } else if (simNum.equals("2")){
			   delaySim = true;
			   
			   //Wait for packet type
			   Scanner readPacketType = new Scanner(System.in);
			   System.out.println("What type of packet would you like to delay? (WRQ/RRQ/DATA/ACK)\n");
			   packetType = readPacketType.next(); // Scans the next token of the input as an int.
			   
			   if (packetType.equals("DATA") || packetType.equals("ACK")){
				   //Wait for packet number
				   Scanner readPacketNum = new Scanner(System.in);
				   System.out.println("What is the first of the two bytes of the block number? (ie 0 of 01, 2 of 23)\n");
				   byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.
				   
				   //Wait for packet number
				   Scanner readPacketSecondNum = new Scanner(System.in);
				   System.out.println("What is the second of the two bytes of the block number? (ie 1 of 01, 3 of 23)\n");
				   byte tempSecond = readPacketSecondNum.nextByte(); // Scans the next token of the input as an int.
				   
				   packetNumByteArray[0] = tempFirst;
				   packetNumByteArray[1] = tempSecond;
				   
				   //Wait for delay time
				   Scanner readPacketDelay = new Scanner(System.in);
				   System.out.println("How long would you like to delay (in milliseconds) the packet? (1000/50/etc)\n");
				   packetDelayTime = readPacketDelay.next(); // Scans the next token of the input as an int.				   
			   } else {
				 //Wait for delay time
				   Scanner readPacketDelay = new Scanner(System.in);
				   System.out.println("How long would you like to delay (in milliseconds) the packet? (1000/50/etc)\n");
				   packetDelayTime = readPacketDelay.next(); // Scans the next token of the input as an int.	
			   }

		   } else if (simNum.equals("3")){
			   duplicateSim = true;
			   
			   //Wait for packet type
			   Scanner readPacketType = new Scanner(System.in);
			   System.out.println("What type of packet would you like to duplicate? (WRQ/RRQ/DATA/ACK)\n");
			   packetType = readPacketType.next(); // Scans the next token of the input as an int.
			   
			   if (packetType.equals("DATA") || packetType.equals("ACK")){
				   //Wait for packet number
				   Scanner readPacketNum = new Scanner(System.in);
				   System.out.println("What is the first byte of the block you would like to duplicate? (1/2/3/etc...)\n");
				   byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.
				   
				   //Wait for packet number
				   Scanner readPacketSecondNum = new Scanner(System.in);
				   System.out.println("What is the second byte of the block would you like to duplicate? (1/2/3/etc...)\n");
				   byte tempSecond = readPacketSecondNum.nextByte(); // Scans the next token of the input as an int.
				   
				   packetNumByteArray[0] = tempFirst;
				   packetNumByteArray[1] = tempSecond;
				   
				   //Wait for delay time
				   Scanner readPacketDelay = new Scanner(System.in);
				   System.out.println("How long would you like to wait (in milliseconds) in between duplicate packets? (1000/50/etc)\n");
				   packetDelayTime = readPacketDelay.next(); // Scans the next token of the input as an int.				   
			   } else {
				   //Wait for delay time
				   Scanner readPacketDelay = new Scanner(System.in);
				   System.out.println("How long would you like to wait (in milliseconds) in between duplicate packets? (1000/50/etc)\n");
				   packetDelayTime = readPacketDelay.next(); // Scans the next token of the input as an int.
			   }
		   }
		   
	   }
   }
   
   
   //Check to see if received packet information is same as inputed simulation information
   public Boolean checkIfSimPacket(byte[] receivedPacketData, String packetType, byte[] packetNumByteArray) {
	   byte[] blockNumber = null;
	   if (packetNumByteArray != null){
		   blockNumber = packetNumByteArray;
	   }
	   byte[] receivedPacketType = {receivedPacketData[0],receivedPacketData[1]};
	   byte[] receivedBlockNumber = {receivedPacketData[2],receivedPacketData[3]};
	   
	   if (packetType.equals("DATA")) {
		   byte[] packetTypeByte = {zero, DATA};
		   if (blockNumber[0] == receivedBlockNumber[0] && blockNumber[1] == receivedBlockNumber[1] && packetTypeByte[1] == receivedPacketType[1]) {
			   return true;
		   }
	   } else if (packetType.equals("ACK")) {
		   byte[] packetTypeByte = {zero, ACK};
		   if (blockNumber[0] == receivedBlockNumber[0] && blockNumber[1] == receivedBlockNumber[1] && packetTypeByte[1] == receivedPacketType[1]) {
			   return true;
		   }
	   } else if (packetType.equals("WRQ")) {
		   byte[] packetTypeByte = {zero, WRQ};
		   if (packetTypeByte[1] == receivedPacketType[1]) {
			   return true;
		   }
	   } else if (packetType.equals("RRQ")) {
		   byte[] packetTypeByte = {zero, RRQ};
		   if (packetTypeByte[1] == receivedPacketType[1]) {
			   return true;
		   }
	   }
	   
	   return false;
   }

   public static void main( String args[] )
   {
     SimpleEchoErrorSimulator c = new SimpleEchoErrorSimulator();
     c.testMenu();
     while(true){
         c.receiveAndEcho(); 
      }
   }
}

