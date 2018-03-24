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
	Boolean invalidTFTPOpcode = false, changeMode = false, changeFileName = false, invalidBlockNum = false, invalidErrCode = false; 
	Boolean runSim = false;
	byte zero = 0;
	byte RRQ = 1;
	byte WRQ = 2;
	byte DATA = 3;
	byte ACK = 4;
	byte[] packetNumByteArray = {zero,zero};
	byte[] opCodeOrBlockChange = {zero, zero};
	String newMode, fileName;
	
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

		//for duplicate packet thread
		Thread thread = null;

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
			
			if(invalidTFTPOpcode) {

				data[0] = opCodeOrBlockChange[0];
				data[1] = opCodeOrBlockChange[1];

				
			} else if(changeMode) {
				data = changeMode(data, newMode);
				
			} else if(changeFileName) {
				data = changeFileName(data, fileName);
			
			} else if(invalidBlockNum) {
				System.out.println("HOOOOOOOOOOOOOOOOW");
				data[2] = opCodeOrBlockChange[0];
				data[3] = opCodeOrBlockChange[1];
				
			}else if (lostSim) {
				runSim = false;
				//    		  receivePacket = new DatagramPacket(firstLostAck, firstLostAck.length);
				System.out.println("1. This packet equals the LOST simulation entered packet.");
				//TODO: Lose packet implementation

				if(data[1] == 4) {
					try {

						sendReceiveSocket.receive(receivePacket);
						//	    			receiveSocket.receive(receivePacket);
						sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
								clientAddress, clientPort);
						//					receiveSocket.send(sendPacket);
						//					receivePacket = new DatagramPacket(secondAck, secondAck.length);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(data[1] == 3) {
					try {
						receiveSocket.receive(receivePacket);
						sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
								clientAddress, clientPort);
						System.out.println(receivePacket.getData());
						System.out.println(receivePacket.getLength());

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (delaySim) {
				runSim = false;
				isSimPacket = false;
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
			//runSim = false;
			//isSimPacket = false;
		} 

		if (ccPort == 0) {
			sendPacket = new DatagramPacket(data, data.length,
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

		// Send the datagram packet to the server via the send socket. 
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Intermediate Host: packet sent");
		//      }

		// Slow things down (wait 5 seconds)
		/*  try {
          Thread.sleep(5000);
      } catch (InterruptedException e ) {
          e.printStackTrace();
          System.exit(1);
      } 
		 */

		/*//Check if this is a new RRQ/WRQ and forwards message to the listener port for the server
        byte zero = 0;
	    byte one = 1;
	    byte two = 2;
	    if (!(msg[0] == zero && msg[1] == one || msg[0] == zero && msg[1] == two)) {
		    ccPort = 0;
	    }
		 */

		if(isSimPacket){
			if (duplicateSim){
				runSim = false;
				isSimPacket = false;
				System.out.println("This packet equals the simulation entered packet.");

				//create datagram with packet information, length, address, and port number
				if (ccPort == 0) {
					sendPacket = new DatagramPacket(data, receivePacket.getLength(),
							receivePacket.getAddress(), 6969);
				} else {
					sendPacket = new DatagramPacket(data, receivePacket.getLength(),
							receivePacket.getAddress(), ccPort);
				}

				// Duplicate packet implementation
				System.out.println("Creating new Duplicate Packet Connection Thread...");

				//create Duplicate packet connection thread, and pass it datagram information and time delay 
				thread = new Thread(new DuplicateConnection(sendPacket, packetDelayTime));
				thread.start();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e ) {
					e.printStackTrace();
					System.exit(1);
				} 
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
			isSimPacket = checkIfSimPacket(secReceivedByteArray,packetType, packetNumByteArray);  
		}

		if (isSimPacket){
			if(invalidTFTPOpcode) {
				data2[0] = opCodeOrBlockChange[0];
				data2[1] = opCodeOrBlockChange[1];
			} else if (invalidBlockNum) {
				data2[2] = opCodeOrBlockChange[0];
				data2[3] = opCodeOrBlockChange[1];
			} else if (lostSim) {
				runSim = false;
				if((packetType.equals("DATA") && data2[1] == 3) || (packetType.equals("ACK") && data2[1] == 4)) {
					System.out.println("2. This packet equals the LOST simulation entered packet.");

					if(data2[1] == 4) {
						try {
							if (data2[3] == 0) {
								sendReceiveSocket.receive(receivePacket);
							} else {
								receiveSocket.receive(receivePacket);
							}
							sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
									clientAddress, clientPort);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if(data2[1] == 3) {
						try {
							sendReceiveSocket.receive(receivePacket);
							sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
									clientAddress, clientPort);
							System.out.println(receivePacket.getData());
							System.out.println(receivePacket.getLength());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (delaySim) {
				runSim = false;
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
			//runSim = false;
			//isSimPacket = false;
		}

		//      if (!lostSim) {
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

		//      }

		if(isSimPacket){
			if (duplicateSim){
				runSim = false;
				System.out.println("\nThis packet equals the simulation entered packet.");

				//create datagram with packet information, length, address, and port number
				sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
						clientAddress, clientPort);

				// Duplicate packet implementation
				System.out.println("Creating new Duplicate Packet Connection Thread...");

				//create Duplicate packet connection thread, and pass it datagram information and time delay 
				thread = new Thread(new DuplicateConnection(sendPacket, packetDelayTime));
				thread.start();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e ) {
					e.printStackTrace();
					System.exit(1);
				} 
			}
			// runSim = false;
			// isSimPacket = false;
		}

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
			System.out.println("Which of the following would you like to simulate?\n(1) - Lose A Packet\n(2) - Delay A Packet\n(3) - Duplicate A Packet\n(4) - Illegal TFTP operation ");
			String simNum = readSimInput.next(); // Scans the next token of the input as an int.

			if (simNum.equals("1")){
				lostSim = true;

				//Wait for packet type
				Scanner readPacketType = new Scanner(System.in);
				System.out.println("What type of packet would you like to lose? (WRQ/RRQ/DATA/ACK/ERROR)\n");
				packetType = readPacketType.next(); // Scans the next token of the input as an int.

				if (packetType.equals("DATA") || packetType.equals("ACK")){
					//Wait for packet number
					Scanner readPacketNum = new Scanner(System.in);
					System.out.println("What is the first of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
					byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.

					//Wait for packet number
					Scanner readPacketSecondNum = new Scanner(System.in);
					System.out.println("What is the second of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
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
					System.out.println("What is the first of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
					byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.

					//Wait for packet number
					Scanner readPacketSecondNum = new Scanner(System.in);
					System.out.println("What is the second of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
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
					System.out.println("What is the first of the two bytes of the block number? (1/2/3/etc... i.e. 0 of 01, 2 of 23)\n");
					byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.
					//Wait for packet number
					Scanner readPacketSecondNum = new Scanner(System.in);
					System.out.println("What is the second of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
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
			} else if(simNum.equals("4")) {
				Scanner getInvalidTFTPType = new Scanner(System.in);
				System.out.println("Choose one of the following:\n(1) Invalid TFTP opcode\n"
						+ "(2) Invalid Mode on RRQ/WRQ\n(3) Invalid Filename\n(4) Invalid Block Number\n(5) Invalid Error");
				String invalidType = getInvalidTFTPType.nextLine();
				
				if(invalidType.equals("1")) {
					invalidTFTPOpcode = true;
					Scanner readPacketType = new Scanner(System.in);
					System.out.println("What type of packet would you like to change opcode on? (WRQ/RRQ/DATA/ACK)");
					packetType = readPacketType.nextLine();
					
					if(packetType.equals("WRQ") || packetType.equals("RRQ")) {
						Scanner readOpcodeNum = new Scanner(System.in);
						System.out.println("What is the first of the two bytes you would like to change the opcode to?");
						byte tempFirst = readOpcodeNum.nextByte();
						Scanner readOpcodeSecondNum = new Scanner(System.in);
						System.out.println("What is the second of the two bytes you would like to change the opcode to?");
						byte tempSecond = readOpcodeSecondNum.nextByte();
						opCodeOrBlockChange[0] = tempFirst;
						opCodeOrBlockChange[1] = tempSecond;
						packetNumByteArray[0] = 0;
						if(packetType.equals("WRQ")) {
							packetNumByteArray[1] = 2;
						}else{
							packetNumByteArray[1] = 1;
						}
						
					}else if(packetType.equals("DATA") || packetType.equals("ACK") || packetType.equals("ERROR")) {
						Scanner readPacketNum = new Scanner(System.in);
						System.out.println("What is the first of the two bytes of the block number? (1/2/3/etc... i.e. 0 of 01, 2 of 23)\n");
						byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.
						//Wait for packet number
						Scanner readPacketSecondNum = new Scanner(System.in);
						System.out.println("What is the second of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
						byte tempSecond = readPacketSecondNum.nextByte(); // Scans the next token of the input as an int.
						packetNumByteArray[0] = tempFirst;
						packetNumByteArray[1] = tempSecond;
						
						Scanner readOpcodeNum = new Scanner(System.in);
						System.out.println("What is the first of the two bytes you would like to change the opcode to?");
						byte first = readOpcodeNum.nextByte();
						Scanner readOpcodeSecondNum = new Scanner(System.in);
						System.out.println("What is the second of the two bytes you would like to change the opcode to?");
						byte second = readOpcodeSecondNum.nextByte();
						opCodeOrBlockChange[0] = first;
						opCodeOrBlockChange[1] = second;
					}
					
				}else if (invalidType.equals("2")) {
					changeMode = true;
					Scanner readPacketType = new Scanner(System.in);
					System.out.println("What type of packet would you like to change mode on? (WRQ/RRQ)");
					packetType = readPacketType.nextLine();
					Scanner readMode = new Scanner(System.in);
					System.out.println("Please enter the new mode:");
					newMode = readMode.nextLine();
					
				}else if(invalidType.equals("3")) {
					changeFileName = true;
					Scanner readPacketType = new Scanner(System.in);
					System.out.println("What type of packet would you like to change filename on? (WRQ/RRQ)");
					packetType = readPacketType.nextLine();
					Scanner readFilename = new Scanner(System.in);
					System.out.println("Please enter the new fileName:");
					fileName = readFilename.nextLine();
				
				}else if(invalidType.equals("4")) {
					invalidBlockNum = true;
					Scanner readPacketType = new Scanner(System.in);
					System.out.println("What type of packet would you like to change block number on? (DATA/ACK)");
					packetType = readPacketType.nextLine();
					
					Scanner readPacketNum = new Scanner(System.in);
					System.out.println("What is the first of the two bytes of the block number? (1/2/3/etc... i.e. 0 of 01, 2 of 23)\n");
					byte tempFirst = readPacketNum.nextByte(); // Scans the next token of the input as an int.
					//Wait for packet number
					Scanner readPacketSecondNum = new Scanner(System.in);
					System.out.println("What is the second of the two bytes of the block number? (1/2/3/etc... i.e. 1 of 01, 3 of 23)\n");
					byte tempSecond = readPacketSecondNum.nextByte(); // Scans the next token of the input as an int.
					packetNumByteArray[0] = tempFirst;
					packetNumByteArray[1] = tempSecond;
					
					Scanner readNewBlockNum = new Scanner(System.in);
					System.out.println("What is the first of the two bytes you would like to change the opcode to?");
					byte first = readNewBlockNum.nextByte();
					Scanner readSecondBlockNum = new Scanner(System.in);
					System.out.println("What is the second of the two bytes you would like to change the opcode to?");
					byte second = readSecondBlockNum.nextByte();
					opCodeOrBlockChange[0] = first;
					opCodeOrBlockChange[1] = second;	
				}
				
			}

		}
	}
	
	public byte[] changeFileName(byte arr[], String filename) {
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		temp.write(arr[0]);
		temp.write(arr[1]);
		try {
			temp.write(filename.getBytes());
			temp.write(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count = 0;
		for(int i=0; i<arr.length;i++) {
			if(count == 1) {
				temp.write(arr[i]);
			}else if(arr[i] == 0) {
				count++;
			}
		}
		byte newData[] = temp.toByteArray();
		return newData;
	}

	public byte[] changeMode(byte arr[], String mode) {
		ByteArrayOutputStream temp = new ByteArrayOutputStream();
		temp.write(arr[0]);
		temp.write(arr[1]);
		
		for(int i=2; i<arr.length;i++) {
			if(arr[i] != 0) {
				temp.write(arr[i]);
			}else{
				break;
			}
		}
		temp.write(0);
		try {
			temp.write(mode.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		temp.write(0);
		byte newData[] = temp.toByteArray();
		return newData;
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

