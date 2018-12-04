import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.ByteBuffer;

public class Client {
	public static void main(String[] args) throws IOException {
		String hostname = "";
		int port = 0;
		String filename= "";
		int N = 0;
		int MSS = 0;
		if(args.length >= 5 ){
			hostname = args[0];
			port = Integer.parseInt(args[1]);
			filename = args[2];
			N = Integer.parseInt(args[3]);
			MSS = Integer.parseInt(args[4]);
		} else {
			System.out.println("Insufficient Arguments");
			System.exit(0);
		}

		// Create client socket
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(0);
		}

	  	// Get IP address of Receiver from the hostname
		InetAddress IPAddress = null;
		try {
			IPAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			System.out.println("Host not found!");
			System.exit(0);
		}

		// Buffer data being read from files in byte array
		byte[] buffer = readFile(filename);

		// Generate Datagram Packets for buffer
		byte[][] packets = generatePackets(buffer, MSS);

		// Maintain a window index to know the current index of window until where ACK received
		int idx = 0;
		int pointer = 0;
		int seqAck = -1;

		long start = System.currentTimeMillis();
		while ((idx * MSS) < buffer.length) {
			// send packets - multiple at once
			while (pointer < N && (idx* MSS) < buffer.length) {
				DatagramPacket sendPacket = new DatagramPacket(packets[idx], packets[idx].length, IPAddress, port);
				try {
					clientSocket.send(sendPacket);
					System.out.println("Sending packet Seq No : " + idx);
					idx++;
					pointer++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// receive ACK
			byte[] receive = new byte[MSS + 8];
			DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
			boolean flag = true;

			int tempIndex = idx;
			try {
				clientSocket.setSoTimeout(1000);
				while (flag) {
					clientSocket.receive(receivePacket);
					seqAck = ProcessACK(receivePacket.getData());
					System.out.println("ACK received for Seq No : " + seqAck);
					if (seqAck == tempIndex - 1) { 
						// if proper seq no received
						pointer = 0;
						idx = tempIndex;
						flag = false;
					} else if (seqAck != -1) {
						pointer = idx- seqAck - 1;
						idx = seqAck + 1;
					}
				}
			} catch (SocketTimeoutException e) {
				// timeout occurs
				System.out.println("Timeout, sequence number = " + seqAck);
				idx= seqAck + 1;
				pointer = 0;
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken: " + (end - start));
	}

	public static byte[] readFile(String path){
		byte[] a = null;
		try {
		   a = Files.readAllBytes(new File(path).toPath());
		} catch(Exception e) {
		   System.out.println("No such file exists!");
		   System.exit(0);
		}
		return a;
	 }
  
	public static byte[] checksum(byte[] data) {
		// checksum calculation. Ref - StackOverflow
		int sum = 0;
		for(int i = 0; i < data.length;i++){
			sum += (data[i]&0xff) << 8;
			if(++i == data.length) break;
			sum += (data[i]&0xff);
		}
		sum = (~((sum & 0xffff)+(sum >> 16)))&0xffff;
		byte[] buffer = ByteBuffer.allocate(4).putInt(sum).array();
		// return only last 2 bytes of int as the first 2 are not used
		// checksum is 16 bits = 2 bytes
		byte[] res = new byte[2];
		res[0] = buffer[2];
		res[1] = buffer[3];
		return res;
	}
  
	 public static byte[][] generatePackets(byte[] data, int MSS) {
		int start_idx, end_idx, idx;
		start_idx = end_idx = idx = 0;
		byte[][] packets = new byte[data.length/MSS + 1][];
  
		while(end_idx < data.length){
		   end_idx += MSS;
		   if(end_idx > data.length) end_idx = data.length;
  
		   // get packet data
		   byte[] packet_data = new byte[end_idx - start_idx];
		   System.arraycopy(data, start_idx, packet_data, 0, packet_data.length);
		   // generate Header fields for packet
		   // 4 byte seq number
		   byte[] seq_no = ByteBuffer.allocate(4).putInt(idx).array();
		   // 2 byte checksum
		   byte[] chksum = checksum(packet_data);
		   // 2 byte fixed value for data packet
		   short number = 21845;
		   byte[] identifier = ByteBuffer.allocate(2).putShort(number).array();
  
		   // concatenate header and data
		   byte[] packet = new byte[seq_no.length + chksum.length + identifier.length + packet_data.length];
		   System.arraycopy(seq_no, 0, packet, 0, seq_no.length);
		   System.arraycopy(chksum, 0, packet, seq_no.length, chksum.length);
		   System.arraycopy(identifier, 0, packet, seq_no.length + chksum.length, identifier.length);
		   System.arraycopy(packet_data, 0, packet, seq_no.length + chksum.length + identifier.length, packet_data.length);
  
		   // store it in packet list
		   packets[idx] = packet;
  
		   idx++;
		   start_idx = end_idx;
		}
  
		return packets;
	 }
  
	public static int ProcessACK(byte[] data) {
		// check if it's an ACK by checking its identifier field
		if(data[6] != -43 && data[7] != 86) return -1;
		// get the sequence number from header of packet
		byte[] seq_no = new byte[4];
		System.arraycopy(data, 0, seq_no, 0, seq_no.length);
		return ByteBuffer.wrap(seq_no).getInt();
	}

}
