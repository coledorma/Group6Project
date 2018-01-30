package sysc3303.group.code;
// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits 
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class SimpleEchoClient {

   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendReceiveSocket;
   int packetSize = 516;
   byte zero = 0;
   byte RRQ = 1;
   byte WRQ = 2;
   byte DATA = 3;
   byte ACK = 4;
   byte msg[];

   public SimpleEchoClient()
   {
      try {
         // Construct a datagram socket and bind it to any available 
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void sendAndReceive(){
	   //Read input from terminal for read or write request
	   Scanner readUserInput = new Scanner(System.in);
	   System.out.println("Would you like to send a read request or write request this client (R/W)? ");
	   String requestAnswer = readUserInput.next(); // Scans the next token of the input as an int.
	   //readUserInput.close();
	   
	   String filename;
	   
	   //Wait for answer from user for read or write request
	   if (requestAnswer.equals("R") || requestAnswer.equals("r")){
		   //Wait for filename
		   Scanner readFileInput = new Scanner(System.in);
		   System.out.println("What is the name of the file you would like to read? ");
		   String requestFileName = readFileInput.next(); // Scans the next token of the input as an int.
		   readFileInput.close();
		   
		   if (requestFileName != null){
			   System.out.println("Sending read request...");
			   
			   //Send read request and receive response
			   filename = requestFileName;
			   sendReadRequest(filename); 
		   }
		   
	   } else if (requestAnswer.equals("W") || requestAnswer.equals("w")){ 
		 //Wait for filename
		   Scanner readFileInput = new Scanner(System.in);
		   System.out.println("What is the name of the file you would like to write? ");
		   String requestFileName = readFileInput.next(); // Scans the next token of the input as an int.
		   readFileInput.close();
		   
		   System.out.println("Sending write request...");
		   
		   //Send write request and receive response
		   filename = requestFileName;
		   sendWriteRequest(filename);
	   }
	   
	      
	   //Read input from terminal
	   Scanner readInput = new Scanner(System.in);
	   System.out.println("Would you like to shutdown this client (Y/N)? ");
	   String shutdownAnswer = readInput.next(); // Scans the next token of the input as an int.
	   readInput.close();

	   //Wait for answer from user
	   if (shutdownAnswer.equals("Y") || shutdownAnswer.equals("y")){
		   System.out.println("Shutting this client down.");
		   System.exit(1);
	   }   

   }
   
   public byte[] createReadWriteRequest(byte firstByte, byte secondByte, String filename, String mode){
	   ByteArrayOutputStream output = new ByteArrayOutputStream();
	   byte zeroByte = 0;
	   
	   try {
		   output.write(firstByte);
		   output.write(secondByte);
		   output.write(filename.getBytes());
		   output.write(zeroByte);
		   output.write(mode.getBytes());
		   output.write(zeroByte);
	   } catch (IOException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   
	   byte msg[] = output.toByteArray();
	   return msg;
   }
   
   public byte[] createDataRequest(byte firstByte, byte secondByte, byte block1, byte block2, byte[] data){
	   ByteArrayOutputStream output = new ByteArrayOutputStream();

	   output.write(firstByte);
	   output.write(secondByte);
	   output.write(block1);
	   output.write(block2);
	   try {
		output.write(data);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   
	   byte msg[] = output.toByteArray();
	   return msg;
   }
   
   public byte[] createAckRequest(byte firstByte, byte secondByte, byte block1, byte block2){
	   ByteArrayOutputStream output = new ByteArrayOutputStream();

	   output.write(firstByte);
	   output.write(secondByte);
	   output.write(block1);
	   output.write(block2);
	   
	   byte msg[] = output.toByteArray();
	   return msg;
   }
   
   public void sendReadRequest(String filename){
	   	  //----------FORMING DATAGRAM REQUEST------------------
	   	  msg = createReadWriteRequest(zero,RRQ,filename,"octet");
	      
	      String message = new String(msg); 
	      System.out.println("Client: sending a packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");
	      
	      try {
	         sendPacket = new DatagramPacket(msg, msg.length,
	                                         InetAddress.getLocalHost(), 2323);
	      } catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }

	      System.out.println("Client: Sending packet:");
	      System.out.println("To Server: " + sendPacket.getAddress());
	      System.out.println("Destination Server port: " + sendPacket.getPort());
	      int len = sendPacket.getLength();
	      System.out.println("Length: " + len);
	      System.out.print("Containing: \n");
	      String received = new String(sendPacket.getData()); 
	      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n"); // or could print "s"
	      
	      //-----------SENDING REQUEST----------------
	      // Send the datagram packet to the server via the send/receive socket. 
	      try {
	         sendReceiveSocket.send(sendPacket);
	      } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }

	      System.out.println("Client: Packet sent.\n");

	      
	   // Construct a DatagramPacket for receiving packets up 
	   // to 516 bytes long (the length of the byte array).
	   int block = 0;
	   byte data[] = new byte[packetSize];
	   receivePacket = new DatagramPacket(data, data.length);  
	   boolean lastPacket = false;
	   
	   File newReadFile = new File(filename);
	   FileOutputStream receivedFile = null;
	   try {
		   receivedFile = new FileOutputStream(newReadFile);
	   } catch (FileNotFoundException e1) {
		   // TODO Auto-generated catch block
		   e1.printStackTrace();
	   }
	   
	   while (!lastPacket) {
	    	  try {
	 	         // Block until a datagram is received via sendReceiveSocket.  
	 	         sendReceiveSocket.receive(receivePacket);
	 	         block++;
	 	      } catch(IOException e) {
	 	         e.printStackTrace();
	 	         System.exit(1);
	 	      }

	 	      // Process the received datagram.
	 	      System.out.println("Client: Packet received:");
	 	      System.out.println("Block number: " + block);
	 	      System.out.println("From Server: " + receivePacket.getAddress());
	 	      System.out.println("Server port: " + receivePacket.getPort());
	 	      len = receivePacket.getLength();
	 	      if (len < 516){
	 	    	  	lastPacket = true;
	 	      }
	 	      System.out.println("Length: " + len);
	 	      System.out.print("Containing: \n");
	 	      // Form a String from the byte array.
	 	      received = new String(receivePacket.getData());   
	 	      System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String form:" + receivePacket.getData()[0] + receivePacket.getData()[1] + "\n");
	 	      
	 	      ByteArrayOutputStream output = new ByteArrayOutputStream();
	 	      
	 	      //Check what type of response was received
	 	      //DATA
	 	      if (receivePacket.getData()[1] == 3) {
	 	    	  
		 	    	 if (!newReadFile.exists()) {
		                    try {
								newReadFile.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		 	    	 }
	 	    	  byte[] blockNumber = {receivePacket.getData()[2], receivePacket.getData()[3]};
	 	    	  
	 	    	  
	 	    	  //DataOutputStream outStream = new DataOutputStream(output);
	 	    	  try {
	 	    		  //Writing to local disk
	 	    		 System.out.println("Writing file to local disk...\n");
	 	    		  receivedFile.write(receivePacket.getData(), 4, receivePacket.getLength() - 4);
	 	    	  } catch (IOException e) {
	 	    		  // TODO Auto-generated catch block
	 	    		  e.printStackTrace();
	 	    	  }
	 	    	  
	 	    	  //Create and send ACK request of DATA received
	 	    	  msg = createAckRequest(zero,ACK,blockNumber[0],blockNumber[1]);
	 		      
	 		  message = new String(msg); 
	 		  System.out.println("Client: sending a packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");
	 		      
	 		  try {
	 		         sendPacket = new DatagramPacket(msg, msg.length,
	 		                                         InetAddress.getLocalHost(), 2323);
	 		      } catch (UnknownHostException e) {
	 		         e.printStackTrace();
	 		         System.exit(1);
	 		      }

	 		      System.out.println("Client: Sending packet:");
	 		      System.out.println("To Server: " + sendPacket.getAddress());
	 		      System.out.println("Destination Server port: " + sendPacket.getPort());
	 		      len = sendPacket.getLength();
	 		      System.out.println("Length: " + len);
	 		      System.out.print("Containing: \n");
	 		      received = new String(sendPacket.getData()); 
	 		      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");
	 		      
	 		      //-----------SENDING ACK REQUEST----------------
	 		      // Send the datagram packet to the server via the send/receive socket. 
	 		      try {
	 		         sendReceiveSocket.send(sendPacket);
	 		      } catch (IOException e) {
	 		         e.printStackTrace();
	 		         System.exit(1);
	 		      }

	 		      System.out.println("Client: Packet sent.\n");
	 	    	  
	 	      }
	 	     
	 	     //Writing file to local disk
	 	   /* try {
	 	    	System.out.println("Writing file to local disk...\n");
	 			OutputStream outStream = new FileOutputStream(filename);
	 			output.writeTo(outStream);
	 	     } catch (IOException e) {
	 			e.printStackTrace();
	 	     }*/
	      }
   }
   
   
   public void sendWriteRequest(String filename){
	   	  //Setup file to send
	   	  File writeFile = new File(filename);
	   	  byte[] writeFileBytes = new byte[(int) writeFile.length()];
	   	  try {
			FileInputStream outWriteFile = new FileInputStream(writeFile);
			System.out.println("WOW");
			outWriteFile.read(writeFileBytes);
	   	  } catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
	   	  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	   	  //----------FORMING DATAGRAM REQUEST------------------
	   	  msg = createReadWriteRequest(zero,WRQ,filename,"octet");
	      
	      String message = new String(msg); 
	      System.out.println("Client: sending a packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");
	      
	      try {
	         sendPacket = new DatagramPacket(msg, msg.length,
	                                         InetAddress.getLocalHost(), 2323);
	      } catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }

	      System.out.println("Client: Sending packet:");
	      System.out.println("To Server: " + sendPacket.getAddress());
	      System.out.println("Destination Server port: " + sendPacket.getPort());
	      int len = sendPacket.getLength();
	      System.out.println("Length: " + len);
	      System.out.print("Containing: \n");
	      String received = new String(sendPacket.getData()); 
	      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n"); // or could print "s"
	      
	      //-----------SENDING REQUEST----------------
	      // Send the datagram packet to the server via the send/receive socket. 
	      try {
	         sendReceiveSocket.send(sendPacket);
	      } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }

	      System.out.println("Client: Packet sent.\n");
	      
	      
	   // Construct a DatagramPacket for receiving packets up 
	   // to 4 bytes long (the length of the byte array).
	   int block = 0;
	   byte data[] = new byte[4];
	   receivePacket = new DatagramPacket(data, data.length);  
	   boolean lastPacket = false;
	   int count = 0;
	   
	   try {
	         // Block until a datagram is received via sendReceiveSocket.  
	         sendReceiveSocket.receive(receivePacket);
	         block++;
	   } catch(IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	   }

	   // Process the received datagram.
	   System.out.println("Client: Packet received:");
	   System.out.println("From Server: " + receivePacket.getAddress());
	   System.out.println("Server port: " + receivePacket.getPort());
	   len = receivePacket.getLength();
	   System.out.println("Length: " + len);
	   System.out.print("Containing: \n");
	   // Form a String from the byte array.
	   received = new String(receivePacket.getData());   
	   System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String form:" + receivePacket.getData()[0] + receivePacket.getData()[1] + "\n");
	   
	   while (!lastPacket) {
	 	      ByteArrayOutputStream output = new ByteArrayOutputStream();
	 	      
	 	      //Check what type of response was received
	 	      //ACK
	 	      if (receivePacket.getData()[1] == 4) {
	 	    	  byte[] blockNumber = {receivePacket.getData()[2], receivePacket.getData()[3]};
	 	    	  byte[] dataBlock;
	 	    	  
	 	    	  //Create and send DATA request of ACK received
	 	    	  //Create block of data to send
	 	    	  if (writeFileBytes.length-count >= packetSize){
	 	    		 dataBlock = Arrays.copyOfRange(writeFileBytes, count, count+packetSize);
	 	    	  //Last block to send
	 	    	  } else {
	 	    		 dataBlock = Arrays.copyOfRange(writeFileBytes, count, writeFileBytes.length);
	 	    		 System.out.println("Last block to send...");
	 	    		 lastPacket = true;
	 	    	  }
	 	    	  
	 	    	  msg = createDataRequest(zero,DATA,blockNumber[0],blockNumber[1],dataBlock);
	 		      
	 		      message = new String(msg); 
	 		      System.out.println("Client: sending a packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");
	 		      
	 		      try {
	 		         sendPacket = new DatagramPacket(msg, msg.length,
	 		                                         InetAddress.getLocalHost(), 2323);
	 		      } catch (UnknownHostException e) {
	 		         e.printStackTrace();
	 		         System.exit(1);
	 		      }

	 		      System.out.println("Client: Sending packet:");
	 		      System.out.println("Block: " + block);
	 		      System.out.println("To Server: " + sendPacket.getAddress());
	 		      System.out.println("Destination Server port: " + sendPacket.getPort());
	 		      len = sendPacket.getLength();
	 		      System.out.println("Length: " + len);
	 		      System.out.print("Containing: \n");
	 		      received = new String(sendPacket.getData()); 
	 		      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");
	 		      
	 		      //-----------SENDING REQUEST----------------
	 		      // Send the datagram packet to the server via the send/receive socket.
	 		      
	 		      try {
	 		         sendReceiveSocket.send(sendPacket);
	 		      } catch (IOException e) {
	 		         e.printStackTrace();
	 		         System.exit(1);
	 		      }

	 		      System.out.println("Client: Packet sent.\n"); 
	 		      
		 	      //-----------RECEIVING REQUEST----------------
		 	      try {
		 	    	  // Block until a datagram is received via sendReceiveSocket.  
		 	    	  sendReceiveSocket.receive(receivePacket);
		 	    	  block++;
		 	      } catch(IOException e) {
		 	    	  e.printStackTrace();
		 	    	  System.exit(1);
		 	      }
		 		     
		 	      // Process the received datagram.
		 	      System.out.println("Client: Packet received:");
		 	      System.out.println("From Server: " + receivePacket.getAddress());
		 	      System.out.println("Server port: " + receivePacket.getPort());
		 	      len = receivePacket.getLength();
		 	      System.out.println("Length: " + len);
		 	      System.out.print("Containing: \n");
		 	      // Form a String from the byte array.
		 	      received = new String(receivePacket.getData());   
		 	      System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String form:" + receivePacket.getData()[0] + receivePacket.getData()[1] + "\n");
		 	      
		 	     count = count+packetSize;
	 	      
	 	      }
	 	      
	      }
   }

   public static void main(String args[])
   {
      SimpleEchoClient c = new SimpleEchoClient();
      c.sendAndReceive();
   }
}
