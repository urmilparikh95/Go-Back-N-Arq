import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.ByteBuffer;

public class Server {

	public static void main(String[] args) {
		int port = 0;
		String filename = "";
		double probablity = 0.0;
		if(args.length >= 3){
			port = Integer.parseInt(args[0]);
			filename = args[1];
			probablity = Double.parseDouble(args[2]);
		} else {
			System.out.println("Insufficient Arguments");
			System.exit(0);
		}

		// Initialize the file writer
		FileOutputStream f = null;
		try{
			f = new FileOutputStream(filename);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// Initialize the sequence index
		int idx = 0;

		// Initialize Datagram Server at the port
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Listen to request from sender
		DatagramPacket packet = null;
		boolean flag = true;
		while (flag) {
			try {
				byte[] buffer = new byte[1024];
				packet = new DatagramPacket(buffer, buffer.length);
				try {
					if(idx == 0){
						serverSocket.setSoTimeout(60000);
					} else {
						serverSocket.setSoTimeout(5000);
					}
					serverSocket.receive(packet);
				} catch (SocketTimeoutException e) {
					// timeout occurs
					System.out.println("Timeout!!! Transfer Complete");
					break;
				}

				// get packet content
				byte[] packet_content = packet.getData();
				// get sequence number
				byte[] seq_no_buf = new byte[4];
				System.arraycopy(packet_content, 0, seq_no_buf, 0, seq_no_buf.length);
				int seq_no = ByteBuffer.wrap(seq_no_buf).getInt();
				// Drop packet based on probablity
				if (Math.random() <= probablity) {
					System.out.println("Packet loss, sequence number = " + seq_no);
					continue;
				}
				// get data from packet
				byte[] data = new byte[packet.getLength() - 8];
				System.arraycopy(packet_content, 8, data, 0, data.length);
				// generate ACK only if
				// 1. checksum is valid
				// 2. sequence is in order
				// 3. is a data packet
				if((checksum(data)[0] == packet_content[4] && checksum(data)[1] == packet_content[5]) && (idx == seq_no || idx == seq_no + 1) && (packet_content[6] == 85 && packet_content[7] == 85)) {
					// write the data to file
					f.write(data);
					// Generate ACK
					InetAddress IP_addr = packet.getAddress();
					int portNumber = packet.getPort();
					byte[] acknowledgement = generateACK(idx);
					packet = new DatagramPacket(acknowledgement, acknowledgement.length, IP_addr, portNumber);
					serverSocket.send(packet);
					System.out.println("ACK sent for: " + idx);
					// Increment index
					idx++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		serverSocket.close();
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
  
	public static byte[] generateACK(int seq_no) {
		byte[] header = new byte[8];
		byte[] seq_no_buf = ByteBuffer.allocate(4).putInt(seq_no).array();
		System.arraycopy(seq_no_buf, 0, header, 0, seq_no_buf.length);
		// binary to decimal conversion for 1010101010101010
		header[4] = 0;
		header[5] = 0;
		header[6] = -43;
		header[7] = 86;
		return header;
	}
}
