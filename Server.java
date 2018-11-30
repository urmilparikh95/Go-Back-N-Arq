import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.ByteBuffer;

class Server {
   public static void main(String args[]) throws Exception {
      // Initialize Receiver
      int port = 0;
      String filepath = "";
      double p = 0.0;
      if(args.length >= 3){
         port = Integer.parseInt(args[0]);
         filepath = args[1];
         p = Double.parseDouble(args[2]);
      } else {
         System.out.println("Insufficient Arguments");
         System.exit(0);
      }
      // Initialize the file writer
      FileOutputStream f = null;
      try{
         f = new FileOutputStream(filepath);
      } catch(Exception e) {
         e.printStackTrace();
      }
      // Initialize Datagram Server at the port
      DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
      }
      // Initialize the sequence index
      int idx = 0;

      // Listen to request from sender
      while(true) {
         try {
            byte[] buffer = new byte[5000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            // get sequence number
            byte[] seq_no_buf = new byte[4];
            System.arraycopy(seq_no_buf, 0, packet.getData(), 0, seq_no_buf.length);
            int seq_no = ByteBuffer.wrap(seq_no_buf).getInt();
            // Drop packet based on probablity
            if(Math.random() < p) {
               System.out.println("Packet loss, sequence number = " + seq_no);
               continue;
            }
            // get data from packet
            byte[] data = new byte[packet.getData().length-8];
            System.arraycopy(data, 0, packet.getData(), 8, data.length);
            // generate ACK only if
            // 1. checksum is valid
            // 2. sequence is in order
            // 3. is a data packet
            if((checksum(data)[0] == packet.getData()[4] && checksum(data)[1] == packet.getData()[5]) && (idx == seq_no) && ())

         } catch(Exception e) {
            e.printStackTrace();
         }
      }
      // DatagramSocket serverSocket = new DatagramSocket(9876);
      byte[] receiveData = new byte[1024];
      byte[] sendData = new byte[1024];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      String sentence = new String(receivePacket.getData());
      System.out.println("RECEIVED: " + sentence);
      InetAddress IPAddress = receivePacket.getAddress();
      int port = receivePacket.getPort();
      String capitalizedSentence = sentence.toUpperCase();
      sendData = capitalizedSentence.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      serverSocket.send(sendPacket);
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

      return null;
   }

}