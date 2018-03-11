package sysc3303.group.code;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ClientConnection implements Runnable {
	byte[] data;
	DatagramPacket sendReceivePacket;
	DatagramSocket sendReceiveSocket;
	DatagramPacket receivePacket;
	int packetSize = 516;
	byte zero = 0;
	byte fileExistErrCode = 6;
	byte fileNotFoundErrCode = 1;
	byte accessViolationErrCode = 2;
	byte diskFullErrCode = 3;
	byte RRQ = 1;
	byte WRQ = 2;
	byte DATA = 3;
	byte ACK = 4;
	byte ERROR = 5;
	byte msg[];
	boolean errorSent = false;

	public ClientConnection(byte[] fileData, DatagramPacket packet,DatagramSocket socket){
		data = fileData;
		sendReceivePacket = packet;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public String getTextName(byte[] data) {
		int count = 2;
		byte[] temp = new byte[50];
		while(data[count] != 0) {
			temp[count - 2] = data[count];
			count++;
		}
		return new String(temp);
	}

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

		//Create datagram for sending back error request
		DatagramPacket sendPacket1;
		sendPacket1 = new DatagramPacket(temp, temp.length,
				sendReceivePacket.getAddress(), sendReceivePacket.getPort());

		//Check if we have read permissions
		String currDir = System.getProperty("user.dir");
		File f2 = new File(currDir + "/Database/");
		Path readPath = f2.toPath();		//create path to database
		//System.out.println("current directory: " + readPath);

		boolean isReadable = Files.isReadable(readPath);		//check database read permissions

		if(!isReadable) {
			String errStr = "ERROR: Access violation - read permissions denied.";
			System.out.println(errStr);
			byte[] errMsg = errStr.getBytes();
			byte ackToSend[];
			System.out.println("Server: ERROR request created.");
			ackToSend = createErrorRequest(zero,ERROR,zero,accessViolationErrCode,errMsg,zero);
			sendPacket1 = new DatagramPacket(ackToSend, ackToSend.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());

			//Sending ERROR request
			try {
				sendReceiveSocket.send(sendPacket1);
				System.out.println("Server: ERROR request sent.");
				errorSent = true;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}


		/*  ***
	    file is the filename that the user wants to read
		 */

		if(!errorSent) {
			String workingDir = System.getProperty("user.dir");
			String file = workingDir +"/Database/"+ new String(temp,0,temp.length);

			File writeFile = new File(file.trim());

			byte[] writeFileBytes = new byte[(int) writeFile.length()];
			try {
				FileInputStream outWriteFile = new FileInputStream(writeFile);
				outWriteFile.read(writeFileBytes);
			} catch (FileNotFoundException e1) {
				errorSent = true;
				String errStr = "ERROR: File not found.";
				System.out.println(errStr);
				byte[] errMsg = errStr.getBytes();
				byte errToSend[];
				System.out.println("Server: ERROR request created.");
				errToSend = createErrorRequest(zero,ERROR,zero,fileNotFoundErrCode,errMsg,zero);
				DatagramPacket sendPacket = new DatagramPacket(errToSend, errToSend.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());

				//Sending ERROR request
				try {
					sendReceiveSocket.send(sendPacket);
					System.out.println("Server: ERROR request sent.");
					errorSent = true;
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

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
			// int check = 0;
			byte[] blockNumber = {zero, zero};  //zeroize blockNumber for new DATA transfer 
			int mode = 0;
			// While there is still data to send (ie the packet is not the last block), keep sending data and then waiting for ACK
			while (!lastPacket && !errorSent) {
				ByteArrayOutputStream output = new ByteArrayOutputStream();  

				blockNumber = incrementBN(blockNumber); //increase blockNumber for next DATA packet to be sent
				byte[] dataBlock;

				//Create and send DATA request of ACK received
				//Create block of data to send
				if (writeFileBytes.length-count >= 512){
					dataBlock = Arrays.copyOfRange(writeFileBytes, count, count+512);
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




//		 		if(mode != 0) {
		 		try {
		 		    sendReceiveSocket.send(sendReceivePacket);
		 		} catch (IOException e) {
		 		    e.printStackTrace();
		 		    System.exit(1);
		 		}

		 		 System.out.println("Server: Block of DATA sent.\n"); 
//		 	}
				
				count = count+512;	  

				// Once first block of data as been sent, wait for ACK from client before sending another block
				boolean resent = false;
				boolean ackReceived = false;
				
				while (!ackReceived){	
					// Once block of data has been sent, wait for ACK from client before sending another block
					try {
						System.out.println("WAITING FOR ACK");
						sendReceiveSocket.setSoTimeout(30000);
						sendReceiveSocket.receive(receivePacket);
						System.out.println("ACK Received!");
						ackReceived = checkAckData(receivePacket, blockNumber);
					} catch (SocketTimeoutException timeoutEx){ 
//						timeoutEx.printStackTrace();
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
							sendReceiveSocket.send(sendReceivePacket);
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
				}
				block++;
			}
		}
		//errorSent = false;       
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
	public void writeRequest() {
		// First send ACK of WRQ request
		byte zero = 0;
		byte ACK = 4;
		byte msg[];
		msg = createAckRequest(zero,ACK,zero,zero);
		DatagramPacket sendPacket;
		sendPacket = new DatagramPacket(msg, msg.length,
				sendReceivePacket.getAddress(), sendReceivePacket.getPort());

		String currDir = System.getProperty("user.dir");

		//Check for same file in database, if so, create and send ERROR request
		File f = new File(currDir + "/Database/" + getTextName(data).trim());

		//Check if we have write permissions
		File f2 = new File(currDir + "/Database/");
		Path writePath = f2.toPath();		//create path to database
		//System.out.println("current directory: " + writePath);

		boolean isWritable = Files.isWritable(writePath);		//check database write permissions
		//System.out.println("is writable returns: " + isWritable);


		if(!isWritable) {
			String errStr = "ERROR: Access violation - write permissions denied.";
			System.out.println(errStr);
			byte[] errMsg = errStr.getBytes();
			byte ackToSend[];
			System.out.println("Server: ERROR request created.");
			ackToSend = createErrorRequest(zero,ERROR,zero,accessViolationErrCode,errMsg,zero);
			sendPacket = new DatagramPacket(ackToSend, ackToSend.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());

			//Sending ERROR request
			try {
				sendReceiveSocket.send(sendPacket);
				System.out.println("Server: ERROR request sent.");
				errorSent = true;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}  

		if (f.exists()) {
			String errStr = "ERROR: File already exists.";
			System.out.println(errStr);
			byte[] errMsg = errStr.getBytes();
			byte errToSend[];
			System.out.println("Server: ERROR request created.");
			errToSend = createErrorRequest(zero,ERROR,zero,fileExistErrCode,errMsg,zero);
			sendPacket = new DatagramPacket(errToSend, errToSend.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());

			//Sending ERROR request
			try {
				sendReceiveSocket.send(sendPacket);
				System.out.println("Server: ERROR request sent.");
				errorSent = true;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		boolean lastPacket = false;
		
		while(!errorSent && !lastPacket){

			System.out.println("Sending ACK of WRQ");
			try {
				sendReceiveSocket.send(sendPacket);
				System.out.println("Server: ACK Sent.\n");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

		/*	try {
				Thread.sleep(5000);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}
	*/
			byte[] blockNumber = {zero, zero};  //zeroize blockNumber for new DATA transfer 			
			
			ByteArrayOutputStream storeData = new ByteArrayOutputStream();

			while(!lastPacket) {
				// Now was for first block of file to be sent
				byte data1[] = new byte[516];
				sendReceivePacket = new DatagramPacket(data1, data1.length);
				blockNumber = incrementBN(blockNumber); 
				System.out.println("Server: Waiting for DATA.\n");
				boolean dataReceived = false;
				
				while(!dataReceived) {
				      try {        
				    	 sendReceiveSocket.setSoTimeout(45000);
				         System.out.println("Waiting for file"); 
				         sendReceiveSocket.receive(sendReceivePacket);
				         System.out.println("Packet Received");
				         dataReceived = checkAckData(sendReceivePacket, blockNumber);
				      } catch (SocketTimeoutException ste) {
							 //This is where u should recent the datagram
							 System.out.println("Caught");
							 try {
								 System.out.println("Resending Packet!");
								 sendReceiveSocket.send(sendPacket);
								 System.out.println(sendPacket.getData().toString());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					 	} catch (IOException e) {
				         System.out.print("IO Exception: likely:");
				         System.out.println("Receive Socket Timed Out.\n" + e);
				         e.printStackTrace();
				         System.exit(1);
				      }
			      
			      }

				// If block size is under 516 it is the last block of data to receive
				int len = sendReceivePacket.getLength();

				if (len < 516){
					System.out.println("Last packet received.\n");
					lastPacket = true;
				}

				System.out.println("Received Data:");
				System.out.println("Block: " + sendReceivePacket.getData()[2] + sendReceivePacket.getData()[3]);
				System.out.println("Destination Server port: " + sendReceivePacket.getPort());
				System.out.print("Containing: \n");
				String received = new String(sendReceivePacket.getData());
				byte temp[] = new byte[512];
				temp = Arrays.copyOfRange(sendReceivePacket.getData(), 4, 516);
				received = new String(temp);
				System.out.println("--> Byte Form: " + sendPacket.getData() + "\n" + "--> String Form: " + received + "\n");

				try {
					//add all the contents into an array of bytes 
					storeData.write(temp);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				if(lastPacket) {

					String usb = "F:\\";		//checking F drive: usb is connected
					File f1 = new File(usb);
					boolean detectUSB = f1.canRead();
					//System.out.println("detect usb is: " + detectUSB );

					if(detectUSB) {
						long checkDisk = f1.getFreeSpace();	//record amount of free space 
						//System.out.println("free space: " + checkDisk);

						if(checkDisk < storeData.size()) {	//check if the size of the file to write is greater than available room on USB
							String errStr = "ERROR: Free space = " + checkDisk + ". Disk is full.";
							System.out.println(errStr);
							byte[] errMsg = errStr.getBytes();
							byte ackToSend[];
							System.out.println("Server: ERROR request created.");
							ackToSend = createErrorRequest(zero,ERROR,zero,diskFullErrCode,errMsg,zero);
							sendPacket = new DatagramPacket(ackToSend, ackToSend.length, sendReceivePacket.getAddress(), sendReceivePacket.getPort());

							//Sending ERROR request
							try { 
								sendReceiveSocket.send(sendPacket);
								System.out.println("Server: ERROR request sent.");
								errorSent = true;
							} catch (IOException e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
					}  
				}

				if(!errorSent) {
					// Send ACK of DATA to client before waiting to receive next block
					byte ackToSend[];
					ackToSend = createAckRequest(zero,ACK,sendReceivePacket.getData()[2],sendReceivePacket.getData()[3]);
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


			byte[] out = storeData.toByteArray();
			//System.out.println("OUT: " + out[0]);
			//System.out.println("OUT1: "+ new String(out));

			String workingDir = System.getProperty("user.dir");

			//now write to the file
			if(!errorSent) {
				try {
					System.out.println("Writing File to Database...");
					FileOutputStream fout=new FileOutputStream(workingDir + "/Database/" + getTextName(data).trim());
					fout.write(out);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	  
		}
		// errorSent = false;

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

}
