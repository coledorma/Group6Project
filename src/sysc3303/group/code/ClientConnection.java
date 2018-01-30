package sysc3303.group.code;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ClientConnection implements Runnable {
	byte[] data;
	DatagramPacket sendReceivePacket;
	DatagramSocket sendReceiveSocket;
	DatagramPacket receivePacket;
    int packetSize = 516;
    byte zero = 0;
    byte RRQ = 1;
    byte WRQ = 2;
    byte DATA = 3;
    byte ACK = 4;
    byte msg[];
	
	public ClientConnection(byte[] fileData, DatagramPacket packet,DatagramSocket socket){
		data = fileData;
		sendReceivePacket = packet;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// Distinguish whether request is WRQ or RRQ and call appropriate functions
	    byte one = 1;
	    byte two = 2;
	  	if (data[1] == one) {
	  		readRequest();
	  	} else if (data[1] == two) {
	  		writeRequest();
	  	}
	}
	
	public void readRequest() {
		byte zero = 0;
	    byte temp[] = new byte[100];
	    int i = 0;
	    while (data[2+i] != zero) {
		    temp[i] = data[2+i];
	    	i++;
	    }
	    /*  ***
	    file is the filename that the user wants to read
		TODO: search this file in DB and retrieve it
	    */
	    String file = new String(temp,0,temp.length);
	    
	    // For now, just get file from local disk
	   	File writeFile = new File("testRead1.txt");
	   	byte[] writeFileBytes = new byte[(int) writeFile.length()];
	   	try {
			FileInputStream outWriteFile = new FileInputStream(writeFile);
			outWriteFile.read(writeFileBytes);
	   	} catch (FileNotFoundException e1) {
			e1.printStackTrace();
	   	} catch (IOException e) {
			e.printStackTrace();
	   	}

	    int block = 0; 
	    boolean lastPacket = false;
	    int count = 0;
	    String message;
	    byte data[] = new byte[4];
	    receivePacket = new DatagramPacket(data, data.length);  
	    int check = 0;
		   
		// While there is still data to send (ie the packet is not the last block), keep sending data and then waiting for ACK
		while (!lastPacket) {
		 	ByteArrayOutputStream output = new ByteArrayOutputStream();
		 	      
		 	// If ACK is valid from client or if it is original RRQ request, send block of data
		 	if (receivePacket.getData()[1] == 4 || count == 0) {
		 	    byte[] blockNumber = {sendReceivePacket.getData()[2], sendReceivePacket.getData()[3]};
		 	    byte[] dataBlock;
		 	    	  
		 	   	//Create and send DATA request of ACK received
		 	   	//Create block of data to send
		 	    if (writeFileBytes.length-count >= packetSize){
		 	    	dataBlock = Arrays.copyOfRange(writeFileBytes, count, count+packetSize);
		 	    } else {
		 	    	//Last block to send
		 	    	dataBlock = Arrays.copyOfRange(writeFileBytes, count, writeFileBytes.length);
		 	    	System.out.println("Last block to send...");
		 	    	lastPacket = true;
		 	    }
		 	    	  
		 	    msg = createDataRequest(zero,DATA,blockNumber[0],blockNumber[1],dataBlock);
		 		      
		 		message = new String(msg); 
		 		System.out.println("Server: sending a packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");
		 		      
		 		sendReceivePacket = new DatagramPacket(msg, msg.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());
		 		System.out.println(sendReceivePacket.getPort());
		 		System.out.println(msg.length);

		 		try {
		 		    sendReceiveSocket.send(sendReceivePacket);
		 		} catch (IOException e) {
		 		    e.printStackTrace();
		 		    System.exit(1);
		 		}

		 		 System.out.println("Server: Block of DATA sent.\n"); 
		 	}
		 	      
		 	count = count+packetSize;	  
		 	     
		 	// Once first block of data as been sent, wait for ACK from client before sending another block
		 	try {
		 		System.out.println("WAITING FOR ACK");
		 		sendReceiveSocket.receive(receivePacket);
		 		block++;
		 	} catch(IOException e) {
		 		e.printStackTrace();
		 		System.exit(1);
			}
  
		}
		       
	}
	
	public void writeRequest() {
		// First send ACK of WRQ request
		byte zero = 0;
		byte ACK = 4;
		byte msg[];
		msg = createAckRequest(zero,ACK,zero,zero);
		DatagramPacket sendPacket;
		
		sendPacket = new DatagramPacket(msg, msg.length,
	    		sendReceivePacket.getAddress(), sendReceivePacket.getPort());
		
		System.out.println("Sending ACK of WRQ");
		try {
	         sendReceiveSocket.send(sendPacket);
	    } catch (IOException e) {
	         e.printStackTrace();
	         System.exit(1);
	    }

	     System.out.println("Server: ACK Sent.\n");
	     
	      try {
	          Thread.sleep(5000);
	      } catch (InterruptedException e ) {
	          e.printStackTrace();
	          System.exit(1);
	      }
	      
	      boolean lastPacket = false;
	      byte[] blockNumberHolder = null;
	      
	      while(!lastPacket) {
	    	  // Now was for first block of file to be sent
		      byte data[] = new byte[516];
		      sendReceivePacket = new DatagramPacket(data, data.length);
		      System.out.println("Server: Waiting for DATA.\n");
		      byte[] blockNumber = {sendReceivePacket.getData()[2], sendReceivePacket.getData()[3]};
		      blockNumberHolder = blockNumber;

		      try {        
		         System.out.println("Waiting for file"); 
		         sendReceiveSocket.receive(sendReceivePacket);
		      } catch (IOException e) {
		         System.out.print("IO Exception: likely:");
		         System.out.println("Receive Socket Timed Out.\n" + e);
		         e.printStackTrace();
		         System.exit(1);
		      }

		      // If block size is under 516 it is the last block of data to receive
		      int len = receivePacket.getLength();
	 	      if (len < 516){
	 	    	  	lastPacket = true;
	 	      }

		      /*
				TODO: store blocks of file in DB
		      */
		      System.out.println("Received Data:");
 		      System.out.println("Block: " + data);
 		      System.out.println("Destination Server port: " + sendReceivePacket.getPort());
 		      System.out.print("Containing: \n");
 		      String received = new String(sendReceivePacket.getData()); 
 		      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");
   
 		      
 		      
 		      // Send ACK of DATA to client before waiting to receive next block
 		      byte ackToSend[];
 		      ackToSend = createAckRequest(zero,ACK,blockNumberHolder[0],blockNumberHolder[1]);
 		      sendPacket = new DatagramPacket(ackToSend, ackToSend.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());
 		      
 		      try {
 			         sendReceiveSocket.send(sendPacket);
 			      } catch (IOException e) {
 			         e.printStackTrace();
 			         System.exit(1);
 			      }

 			  System.out.println("Server: ACK sent.\n"); 
 		      
	      }
	           
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
	   
	   public byte[] createDataRequest(byte firstByte, byte secondByte, byte block1, byte block2, byte[] data){
		   ByteArrayOutputStream output = new ByteArrayOutputStream();

		   output.write(firstByte);
		   output.write(secondByte);
		   output.write(block1);
		   output.write(block2);
		   try {
			output.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		   
		   byte msg[] = output.toByteArray();
		   return msg;
	   }

}
