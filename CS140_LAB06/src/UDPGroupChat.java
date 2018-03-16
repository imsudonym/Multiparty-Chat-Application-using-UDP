/*	Created: 2018-03-16
 * 	Author: Edcel Balite 
 * 			mdbalite@up.edu.ph
 *  
 *  Input an unknown Group ID to create a new Group Chat.
 * */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class UDPGroupChat {
	private HashMap<String, ArrayList<InetAddress>> GroupChats = new HashMap<String, ArrayList<InetAddress>>();
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	private byte[] buf = new byte[1500];
	
	private InetAddress address;
		
	private Scanner sc = new Scanner(System.in);
	
	private String message = "";
	private boolean running = true;
	
	private int portNumber;
	
    public static void main(String[] args) throws IOException {
    	if (args.length != 1) {
            System.err.println(
                "Usage: java UDPGroupChat <port number>");
            System.exit(1);
        }
        
        int portNumber = Integer.parseInt(args[0]);    	
        new UDPGroupChat(portNumber);
    }
    
    public UDPGroupChat(int port) throws IOException {
       this.portNumber = port;
 	   String choice, ip, groupid; 	   	  	   
	   
	   this.socket = new DatagramSocket(portNumber);
	   recieve.start();
		  
    	while(true) {
 		   System.out.print("Send/Quit: ");
 		   choice = sc.nextLine();
 		   
 		   if(choice.equalsIgnoreCase("Send")) {
 			   boolean send = true;
 			   while(send){
	 			   System.out.print("Group or IP: ");
	 			   choice = sc.nextLine();
	 			   
	 			   if(choice.equalsIgnoreCase("Group")){
	 				  System.out.print("Indicate Group ID: ");
	 				  groupid = sc.nextLine();
	 				  
	 				  if(GroupChats.containsKey(groupid)){
	 					getMessage();
	 					send(GroupChats.get(groupid)); 	 					
	 					send = false;
	 				  }else{
	 					
	 					  boolean create = true;
	 					  while(create){
		 					  System.out.print("\n> Group \"" + groupid + "\" does not exist. Create now? [Y/N]: ");
		 					  choice = sc.nextLine();
		 					  
		 					  if(choice.equalsIgnoreCase("Y")){
		 						  //create new group
		 						  System.out.print("Number of GC members: ");
		 						  String members_count = sc.nextLine();
		 						  
		 						  if(isNumeric(members_count)){		 							  
		 							  int max = Integer.parseInt(members_count);
		 							  if(max < 10){
		 								  
		 								  ArrayList<InetAddress> membersAddresses = new ArrayList<InetAddress>(max);
		 								 
		 								  for(int i = 0; i < max; i++){
		 									 System.out.print("[" + i + "] Indicate IP address: ");
		 									 ip = sc.nextLine();
		 									 
		 									 try{
		 										 address = InetAddress.getByName(ip);
		 										 membersAddresses.add(address);
		 									 }catch(UnknownHostException e){
		 										 System.out.println("IP address could not be determined.");
		 										 System.out.println("membersAddresses.count = " + membersAddresses.size());
		 										 i--;
		 									 }
		 								  }
		 								  
		 								  
		 								  do{
		 									 System.out.print("Group Chat ID: ");
			 								  groupid = sc.nextLine();
		 								  }
		 								  while(isStringNullOrWhiteSpace(groupid));
		 								
		 								  GroupChats.put(groupid, membersAddresses);  	
		 								  System.out.println("> Group chat \"" + groupid + "\" successfully created!");
		 							  }		 							  
		 						  }
		 						  
		 						  create = false;
		 					  }else if (choice.equalsIgnoreCase("N")){
		 						  create = false;
		 					  }
	 					  }
	 				  }	 					  
	 				  	 				  
	 			   }else if(choice.equalsIgnoreCase("IP")){
	 				  System.out.print("Indicate IP address: ");
		 			  ip = sc.nextLine();
		 			  address = InetAddress.getByName(ip);
		 			  
		 			  getMessage();
		 			  send();
		 			  send = false;
	 			   }	 			  	 			    			  	 			   	 			   	 			   	 			   	 			   
 			   }
			   
 		   }else if(choice.equalsIgnoreCase("Quit")) {
 			   if(socket != null) {
 				   recieve.interrupt();
				   running = false;
 				   break;
 			   }
 		   }
 	   }
    	
    	System.out.println("Connection closed.");
    	socket.close();
    	System.exit(1);

    }
    
    Thread recieve = new Thread() {
		public void run() {
			while(running) {
				// get response
				packet = new DatagramPacket(buf, buf.length);
				
				try {
					socket.receive(packet);
				} catch (IOException e) {					
					e.printStackTrace();
				}
				
				// display response
				String received = new String(packet.getData(), 0, packet.getLength());
				System.out.print("\n" + packet.getAddress() + ": " + received + "\t\t\tSend/Quit: ");
			}
		}
	};
	
	public void getMessage(){
		System.out.print("Input message to Send: ");
		message = sc.nextLine();
		buf = message.getBytes();
	}
	
	public void send() {
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, portNumber);
		try {
			socket.send(packet);
		} catch (IOException e) {	
			e.printStackTrace();
		}
	}
	
	public void send(ArrayList<InetAddress> groupMembers) {
		
		int max = groupMembers.size();
		for(int i = 0; i < max; i++) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length, groupMembers.get(i), portNumber);
			try {
				socket.send(packet);
			} catch (IOException e) {	
				e.printStackTrace();
			}
		}						
	}
	
	public boolean isNumeric(String str){		
		char[] c = str.toCharArray();
	    for (int i = 0; i < c.length-1; i++){	    	
	        if (!Character.isDigit(c[i])) return false;
	    }
	    return true;
	}
	
	public static boolean isStringNullOrWhiteSpace(String value) {
	    if (value == null) {
	        return true;
	    }

	    for (int i = 0; i < value.length(); i++) {
	        if (!Character.isWhitespace(value.charAt(i))) {
	            return false;
	        }
	    }

	    return true;
	}
}
