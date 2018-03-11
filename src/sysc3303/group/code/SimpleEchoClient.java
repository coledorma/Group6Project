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
	boolean errorReceived = false;

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

			if (requestFileName != null){
				System.out.println("Sending read request...");

				//Send read request and receive response
				filename = requestFileName;
				sendReadRequest(filename); 
			}

		} else if (requestAnswer.equals("W") || requestAnswer.equals("w")){ 
			//Wait for filename
			boolean validInput = false;
			//will keep prompting for a filename until a valid one is given
			do {
				Scanner readFileInput = new Scanner(System.in);
				System.out.println("What is the name of the file you would like to write? ");
				String requestFileName = readFileInput.next(); // Scans the next token of the input as an int.


				filename = requestFileName;
				File checkFile = new File(filename);
				if (!checkFile.exists()) {
					System.out.println("That file does not exist.");
				}else {
					validInput = true;
				}
			}while(!validInput);


			System.out.println("Sending write request...");

			//Send write request and receive response
			sendWriteRequest(filename);
		}


		//Read input from terminal
		Scanner readInput = new Scanner(System.in);
		System.out.println("Would you like to shutdown this client (Y/N)? ");
		String shutdownAnswer = readInput.next(); // Scans the next token of the input as an int.

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

		byte[] blockNumber = {zero, zero};  //zeroize blockNumber for new DATA transfer 			
		boolean overwrite = false;
		int mode = 0;
		//   ByteArrayOutputStream storeData = new ByteArrayOutputStream();
		while (!lastPacket) {
			boolean dataReceived = false;
			long start = 0;
			final long end;
			blockNumber = incrementBN(blockNumber); 
			int TIMER = 45000;
			while(!dataReceived) {
				try {
					// Block until a datagram is received via sendReceiveSocket.
					System.out.println("Waiting for Data:");
					start = System.currentTimeMillis();
					sendReceiveSocket.setSoTimeout(TIMER);
					sendReceiveSocket.receive(receivePacket);
					dataReceived = checkAckData(receivePacket, blockNumber);

				}catch (SocketTimeoutException ste) {
					//This is where u should recent the datagram
					System.out.println("Caught, timed out.");
					try {
						System.out.println("Resending Packet!");
						sendReceiveSocket.send(sendPacket);
						TIMER = 5000;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			System.out.println("Client: Packet received:");
			block++;
			end = System.currentTimeMillis();
			System.out.println("The program was running: " + (end-start) + "ms.");

			// Process the received datagram.

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
			System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String form:" + received + "\n");
			System.out.println("DATA: "+ new String(data));
			//ByteArrayOutputStream output = new ByteArrayOutputStream();

			//Check what type of response was received
			//DATA
			if (receivePacket.getData()[1] == 3) {
				//Check to see if file exists, if so, prompt user to either overwrite or not
				if (!newReadFile.exists()) {
					overwrite = true;
					try {
						receivedFile = new FileOutputStream(newReadFile);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						newReadFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (!overwrite && newReadFile.exists()){
					//Read input from terminal
					Scanner readOverwriteInput = new Scanner(System.in);
					System.out.println("ERROR: File already exists. Would you like to overwrite it (Y/N)?");
					String overwriteAnswer = readOverwriteInput.next(); // Scans the next token of the input as an int.

					if (overwriteAnswer.equals("Y") || overwriteAnswer.equals("y")){
						overwrite = true;
						try {
							receivedFile = new FileOutputStream(newReadFile);
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							System.out.println("Overwriting...");
							newReadFile.createNewFile();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
					} else {
						System.out.println("Not Overwriting...");
						errorReceived = true;
						break;
					}
				} else {
					try {
						newReadFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				blockNumber[0] = receivePacket.getData()[2];
				blockNumber[1] = receivePacket.getData()[3];
				
				String usb = "F:\\";		//checking F drive: usb is connected
				File f1 = new File(usb);
				boolean detectUSB = f1.canRead();
				boolean isRoom = true;

				if(detectUSB) {
					long checkDisk = f1.getFreeSpace();	//record amount of free space 
					if(checkDisk < receivePacket.getLength() - 4) {	//check if the size of the file to read is greater than available room on USB
						String errStr = "Yout do not have enough space on your disk to read this file.";
						isRoom = false;
						System.out.println(errStr);
						return;
					}else {
						isRoom = true;
					}
				}

				if(isRoom) {
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
					//		 		     if(mode != 2) {
					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}

					System.out.println("Client: Packet sent.\n");
					//		 	 		}
				}
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
		byte[] writeFileBytes = new byte[(int) writeFile.length()+1];
		try {
			FileInputStream outWriteFile = new FileInputStream(writeFile);
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
		System.out.println("Client: sending a WRQ packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");

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

		//-----------SENDING REQUEST----------------WRQ
		// Send the datagram packet to the server via the send/receive socket. 
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Client: WRQ Packet sent.\n");


		// Construct a DatagramPacket for receiving packets up 
		// to 4 bytes long (the length of the byte array).
		int block = 0;
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);  
		boolean lastPacket = false;
		int count = 0;

		try {
			// Block until a datagram is received via sendReceiveSocket.  
			System.out.println("Waiting for WRQ ACK from server");
			sendReceiveSocket.setSoTimeout(50000);
			sendReceiveSocket.receive(receivePacket);
			block++;
		} catch(SocketTimeoutException toe) {
			System.out.println("Timed out, exiting.");
			System.exit(1);
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
		System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String form: " + received + "\n");

		byte[] blockNumber = {zero, zero};  //zeroize blockNumber for new DATA transfer 

		//Check to see if ERROR request received
		if (receivePacket.getData()[1] == 5) {
			errorReceived = true;
		}		
		
		while (!lastPacket && !errorReceived) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			blockNumber = incrementBN(blockNumber); //increase blockNumber for next DATA packet to be sent
			byte[] dataBlock;

			//Create and send DATA request of ACK received
			//Create block of data to send
			if (writeFileBytes.length-count >= 512){
				dataBlock = Arrays.copyOfRange(writeFileBytes, count, count+512);
				//Last block to send
			} else {
				dataBlock = Arrays.copyOfRange(writeFileBytes, count, writeFileBytes.length);
				System.out.println("Last block to send...");
				lastPacket = true;
			}

			msg = createDataRequest(zero,DATA,blockNumber[0],blockNumber[1],dataBlock);
			message = new String(msg); 
			System.out.println("Client: sending a Data packet containing:\n" + "Byte Form: " + msg + "\n" + "String Form: " + message + "\n");

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


			count = count+512;
			//-----------RECEIVING REQUEST----------------

			boolean resent = false; //tracker for if we have already resent the Data packet for this block
			boolean ackReceived = false;

			while(!ackReceived) {
				try {
					// Block until a datagram is received via sendReceiveSocket.
					System.out.println("Waiting for ACK.");
					sendReceiveSocket.setSoTimeout(30000);
					sendReceiveSocket.receive(receivePacket);
					ackReceived = checkAckData(receivePacket, blockNumber);

				} catch (SocketTimeoutException timeoutEx){ 
					//					timeoutEx.printStackTrace();
					/*if we want to limit the amount of resends, we can add a tracker var (resent) 
						  and exit if we've already resent a packet */
					if (resent) {
						System.out.println("Socket timed out twice, exiting.");
						System.exit(1);
					} 
					System.out.println("Caught");
					//resend and go through while loop again for waiting on Ack
					System.out.println("Socket timed out, resending Data packet.");
					try {
						sendReceiveSocket.send(sendPacket);
						System.out.println("Data is now sent");
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
					//uncomment if we want to limit amount of timeouts allowed 
					resent = true;
				} catch(IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				if (ackReceived) {
					// Process the received datagram.
					System.out.println("Client: Ack received:");
					System.out.println("From Server: " + receivePacket.getAddress());
					System.out.println("Server port: " + receivePacket.getPort());
					len = receivePacket.getLength();
					System.out.println("Length: " + len);
					System.out.print("Containing: \n");
					// Form a String from the byte array.
					received = new String(receivePacket.getData());   
					System.out.println("--> Byte Form: " + receivePacket.getData() + "\n" + "--> String form:" + received + "\n");

				}
			}

			block++;

		}
		errorReceived = false;
	}

	public boolean checkAckData(DatagramPacket packet, byte[] blkNum)
	{
		//check for valid 04xx / 03xx format
		if ((packet.getData()[0] == zero) && ((packet.getData()[1] == ACK) || (packet.getData()[1] == DATA))) { //valid 04xx Ack
			//check if it's for the right block of Data
			if ((packet.getData()[2] == blkNum[0]) && (packet.getData()[3] == blkNum[1])) 
			{ //valid block# for the ACK/DATA block that was just sent
				System.out.println("checkAckData = true.");
				return true;  
			}else {//it's a valid ACK/DATA, but likely a duplicate so discard/false
				System.out.println("checkAckData = false. \n Duplicate discarded.");
				return false;
			}
		}else  {//not a valid 03xx/04xx packet
			System.out.println("checkAckData = false. \n invalid ACK/DATA.");
			return false;
		}
	}
	public byte[] incrementBN(byte[] blkNum){
		byte nine = 9;
		byte one = 1;
		byte zero = 0;
		if (blkNum[1] == nine)
		{
			if (blkNum[0] == nine) //blkNum = {9,9} so recycle back to {0,1}
			{
				blkNum[0] = zero;
				blkNum[1] = one;
			}else {
				blkNum[0] += 1;
				blkNum[1] = zero;
			}
		}else 
			blkNum[1] += 1;
		System.out.println("Block# incremented to: " + blkNum[0] + blkNum[1]);
		return blkNum;
	}
	public static void main(String args[]){
		while (true) {
			SimpleEchoClient c = new SimpleEchoClient();
			c.sendAndReceive();
		}
	}
}
