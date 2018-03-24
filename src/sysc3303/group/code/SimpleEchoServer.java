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
	DatagramPacket errorPacket;
	byte ERROR = 5;
	byte tftpError = 4;
	byte zero = 0;
	String reqType;

	public SimpleEchoServer()
	{
		try {
			receiveSocket = new DatagramSocket(6969);
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		} 
	}

	public void receiveAndEcho()
	{
		Thread thread = null;
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		System.out.println("Server: Waiting for Packet.\n");

		// Wait until packet is received containing original WRQ or RRQ request from client
		try {        
			System.out.println("Waiting...");
			receiveSocket.receive(receivePacket);
			if (checkRequest(data) != "") {
				String errStr = checkRequest(data);
				System.out.println(errStr);
				byte[] errMsg = errStr.getBytes();
				byte errToSend[];
				errToSend = createErrorRequest(zero,ERROR,zero,tftpError,errMsg,zero);
				errorPacket = new DatagramPacket(errToSend, errToSend.length, receivePacket.getAddress(), receivePacket.getPort());
				try {
					sendSocket.send(errorPacket);
					System.out.println("Server: ERROR request sent.");
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				// Request is valid so create a new client connection thread and pass original request to it as data
				thread = new Thread(new ClientConnection(data, receivePacket, receiveSocket));
				thread.start();
				thread.join();
			}
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public String checkRequest(byte msg[]) {
		byte zero = 0;
		byte one = 1;
		byte two = 2;
		if (!(msg[0] == zero && msg[1] == one || msg[0] == zero && msg[1] == two)) {
			return "Opcode Error";
		}

		byte temp[] = new byte[100];
		int i = 0;
		while (msg[2+i] != zero) {
			temp[i] = msg[2+i];
			i++;
		}

		String file = new String(temp,0,temp.length);
		if (file == "") {
			return "No File Name";
		}

		if (msg[2+i] != zero) {
			return "TFTP Error";
		}

		byte temp2[] = new byte[100];
		int j = 0;
		while (msg[3+i] != zero) {
			temp2[j] = msg[3+i];
			j++;
			i++;
		}

		String type = new String(temp2,0,temp2.length);
		type = type.trim();
		if (type == "") {
			return "No Mode"; 
		} 
		else if (!( type.equalsIgnoreCase("netascii") ||
				type.equalsIgnoreCase("octet") ||
				type.equalsIgnoreCase("mail") )){
			return "Invalid Mode";

		}

		if (msg[3+i] != zero) {
			return "TFTP Error";
		} 

		return "";
	}
	
	public byte[] createErrorRequest(byte firstByte, byte secondByte, byte errCode1, byte errCode2, byte[] errMsg, byte lastByte){
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		output.write(firstByte);
		output.write(secondByte);
		output.write(errCode1);
		output.write(errCode2);
		try {
			output.write(errMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output.write(lastByte);

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

