import java.util.Scanner;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

class MyServer extends Thread{
	
	ServerSocket serv;
	Socket s;
	int n;
	int ID;
	static int count = 0;
	int sentCount = 0;
	int receivedCount = 0;
	
	OutputStream os;
	OutputStreamWriter osw;
	BufferedWriter bw;
	InputStream is;
	InputStreamReader isr;
	BufferedReader br;
	
	String toBePrinted,incoming;
	boolean check = true;
	
	public void Assigner(ServerSocket serverSocket, Socket endSocket, int ID, int numberOfClients){
		this.serv = serverSocket;
		this.s = endSocket;
		this.ID = ID;
		this.n = numberOfClients;
	}
	
	public int getSentMessageCount(){
		return sentCount;
	}
	
	public int getReceivedMessageCount(){
		return receivedCount;
	}
	
	static ArrayList<String> message = new ArrayList<String>();
	
	public void run(){
		
		try{
			count++;
			message.add(null);
			is = s.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			os = s.getOutputStream();
			osw = new OutputStreamWriter(os);
			bw = new BufferedWriter(osw);
			
			while(count != n){
				try{
					Thread.sleep(500);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			bw.write("INIT " + Integer.toString(ID) + " " + Integer.toString(n) + "\r\n\r\n");
			bw.flush();
			if(ID == n){
				System.out.println("INIT messages are sent to all clients.");
			}
			
			int i = 0;
			while(check == true){
				while(i < n+1){
					message.set(i, null);
					if(i == ID-1){			
						toBePrinted = "";
						incoming = br.readLine();
						if(incoming.contains("QUIT")){
							message.set(i,"QUIT\r\n");
						}else{
							incoming = br.readLine();
							while(!incoming.equals("")){
								toBePrinted += incoming + "\r\n";
								incoming = br.readLine();
							}
							message.set(i, toBePrinted);
						}
						if(!message.get(i).contains("QUIT")){
							System.out.println("Received message from client " + Integer.toString(ID) + ": CHAT");
							System.out.print(message.get(i));
							receivedCount++;
						}else{
							System.out.println("Received message from client " + Integer.toString(ID) + ": QUIT");
							receivedCount++;
							break;
						}
					}
					else if(i != ID-1){
						while(message.get(i) == null){
							try{
								Thread.sleep(5000);
							}catch(InterruptedException e){
								e.printStackTrace();
							}
						}
						if(!message.get(i).contains("QUIT")){
							bw.write(message.get(i) + "\r\n");
							bw.flush();
							sentCount++;
						}else{
							bw.write(message.get(i) + "\r\n");
							bw.flush();
							break;
						}
					}
					if(ID == n){
						System.out.println("Message is forwarded to the other clients.");
					}
					i++;
					if(i == n){
						i = 0;
					}
				}
				break;
			}
			s.close();
			serv.close();
		}catch(IOException e){
			System.out.println("ERROR");
		}
	}
}

public class TurnBasedGroupChat{
	
	public static void main(String[] args) throws IOException 
	{
		ServerSocket serverSocket;
		int numberOfClients = Integer.parseInt(args[2]);
		int ID = 0;
		int count = 0;
		int groupSize = 0;
		int totalSentCount = 0;
		int totalReceivedCount = 0;
		Socket endSocket[] = new Socket[numberOfClients];
		Socket clientSocket;
		
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		OutputStream os;
		OutputStreamWriter osw;
		BufferedWriter bw;
		
		MyServer server;
		MyServer serverList[] = new MyServer[numberOfClients];
		
		boolean check = true;
		String incoming,toBePrinted;
		String message,messageType,userInput,input;
		Scanner scan;
		Scanner read1 = new Scanner(System.in);
		Scanner read2 = new Scanner(System.in);
		
		try{
			
			if(args[0].equals("-server")){	
				serverSocket = new ServerSocket(Integer.parseInt(args[1]));
				
				for(int i=0; i<numberOfClients; i++){
					endSocket[i] = serverSocket.accept();
					ID = i+1;
					server = new MyServer();
					server.Assigner(serverSocket, endSocket[i], ID, numberOfClients);
					serverList[i] = server;
					serverList[i].start();
				}	

				for(int i=0; i<numberOfClients; i++){
					try{
						serverList[i].join();
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					totalSentCount += serverList[i].getSentMessageCount();
					totalReceivedCount += serverList[i].getReceivedMessageCount();
				}	
				System.out.println(totalSentCount +" messages are sent, " + totalReceivedCount + " messages are received.");
				
			}
			else if(args[0].equals("-client")){
				clientSocket = new Socket(InetAddress.getByName(args[1]),Integer.parseInt(args[2]));
				
				is = clientSocket.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				os = clientSocket.getOutputStream();
				osw = new OutputStreamWriter(os);
				bw = new BufferedWriter(osw);
				
				while(check == true){
					
					toBePrinted = "";		
					incoming = br.readLine();
					while(!incoming.equals("")){
						toBePrinted += incoming + "\r\n";
						incoming = br.readLine();
					}
					
					scan = new Scanner(toBePrinted);
					messageType = scan.nextLine();
					
					if(messageType.contains("QUIT")){
						break;
					}
					
					if(messageType.contains("INIT")){
						ID = Integer.parseInt(toBePrinted.substring(5,6));
						groupSize = Integer.parseInt(toBePrinted.substring(7,8));
						System.out.print("Received message: " + toBePrinted);
						System.out.println("ID is set to " + ID);
						count++;
					}else{
						System.out.print("Received from client " + count + ": \r\n" + toBePrinted);
						if(count == groupSize){
							count = 1;
						}else{
							count++;
						}
					}
					
					if(count == ID){
						System.out.print("Enter message type (C: Chat, Q: Quit): ");
						userInput = read1.next();
					
						if(userInput.equals("C")){
							message = "CHAT\r\n";
							System.out.println("Enter message content: ");
							input = read2.nextLine();
							
							while(!input.equals("")){	
								message += input + "\r\n";
								input = read2.nextLine();
							}
							message += "\r\n";
							bw.write(message);
							bw.flush();
							if(count == groupSize){
								count = 1;
							}
							else{
								count ++;
							}
						}
						
						else if(userInput.equals("Q")){
							message = "QUIT\r\n";
							message += "\r\n";
							bw.write(message);
							bw.flush();
							break;
						}
					}	
				}
				clientSocket.close();
			}
		}catch(IOException e){
			System.out.println("ERROR");
		}
	}
}