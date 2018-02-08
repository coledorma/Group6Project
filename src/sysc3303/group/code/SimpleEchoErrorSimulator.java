package sysc3303.group.code;
// SimpleEchoServer.java
// This class is the server side of a simple echo server based on
// UDP/IP. The server receives from a client a packet containing a character
// string, then echoes the string back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;

public class SimpleEchoErrorSimulator {

   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendReceiveSocket, receiveSocket;
   
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
//         System.out.println(receiveSocket.getPort());
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
      System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String Form: " + received + "\n");
      
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
      System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");
      System.out.println("DATA: "+ new String(data));
        
      // Send the datagram packet to the client via the send socket. 
      try {
         sendReceiveSocket.send(sendPacket);
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Intermediate Host: packet sent");
      
      
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
      
      // Slow things down (wait 5 seconds)
      try {
          Thread.sleep(5000);
      } catch (InterruptedException e ) {
          e.printStackTrace();
          System.exit(1);
      }

   }
   

   public static void main( String args[] )
   {
     SimpleEchoErrorSimulator c = new SimpleEchoErrorSimulator();
     while(true){
         c.receiveAndEcho(); 
      }
   }
}

