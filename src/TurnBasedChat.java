import java.util.Scanner;
import java.net.*;
import java.io.*;

public class TurnBasedChat {

	public static void main(String[] args) throws IOException 
	{
		String userInput, input, message, incoming, toBePrinted;
		int countSent = 0;
		int countReceived = 0;
		Scanner scan = new Scanner(System.in);
		Scanner read = new Scanner(System.in);
		ServerSocket serverSocket;
		Socket endSocket;
		InputStream is;
		InputStreamReader isr;
		BufferedReader br;
		OutputStream os;
		OutputStreamWriter osw;
		BufferedWriter bw;
		boolean check = true;
				
		try{
			// server case
			if(args.length == 1){
				serverSocket = new ServerSocket(Integer.parseInt(args[0]));
				endSocket = serverSocket.accept();
				
				is = endSocket.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				os = endSocket.getOutputStream();
				osw = new OutputStreamWriter(os);
				bw = new BufferedWriter(osw);
				
				while(check == true){				
					// server receives
					toBePrinted = "";
					incoming = br.readLine();
					
					if(incoming.equals("QUIT")){
						System.out.println("Received Message: "+ incoming);
						System.out.print(countSent + " messages are sent; " + countReceived + " messages are received.");
						break;
					}
					
					countReceived++;
					
					while(!incoming.equals("")){
						toBePrinted += incoming + "\r\n";
						incoming = br.readLine();
					}
					
					System.out.println("Received Message: " + toBePrinted);	
					// server sends
					System.out.print("Enter message type (C: Chat, Q: Quit): ");
					userInput = scan.next();
					
					while(!(userInput.equals("C")||userInput.equals("Q"))){
						System.out.println("You entered an invalid type! Please enter again.");
						System.out.print("Enter message type (C: Chat, Q: Quit): ");
						userInput = scan.next();
					}
					
					if(userInput.equals("C")){
						message = "CHAT\r\n";
						System.out.println("Enter message content (Empty line to end message): ");
						input = read.nextLine();
						
						while(!input.equals("")){	
							message += input +"\r\n";
							input = read.nextLine();
						}
						message += "\r\n";
						bw.write(message);
						bw.flush();
						countSent++;
					}
					
					else if(userInput.equals("Q")){
						message = "QUIT\r\n";
						message += "\r\n";
						bw.write(message);
						bw.flush();
						break;
					}
				
				}
				serverSocket.close();
				endSocket.close();
			}
			// client case
			else if(args.length == 2){
				endSocket = new Socket(InetAddress.getByName(args[0]),Integer.parseInt(args[1]));
				
				os = endSocket.getOutputStream();
				osw = new OutputStreamWriter(os);
				bw = new BufferedWriter(osw);
				is = endSocket.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				
				while(check == true){
					// client sends
					System.out.print("Enter message type (C: Chat, Q: Quit): ");
					userInput = scan.next();
					
					while(!(userInput.equals("C")||userInput.equals("Q"))){
						System.out.println("You entered an invalid type! Please enter again.");
						System.out.print("Enter message type (C: Chat, Q: Quit): ");
						userInput = scan.next();
					}
					
					if(userInput.equals("C")){
						message = "CHAT\r\n";
						System.out.println("Enter message content (Empty line to end message): ");
						input = read.nextLine();
						
						while(!input.equals("")){	
							message += input +"\r\n";
							input = read.nextLine();
						}
						message += "\r\n";
						bw.write(message);
						bw.flush();
						countSent++;
					}

					else if(userInput.equals("Q")){
						message = "QUIT\r\n";
						message += "\r\n";
						bw.write(message);
						bw.flush();
						break;
					}
					// client receives
					toBePrinted = "";
					incoming = br.readLine();
										
					if(incoming.equals("QUIT")){
						System.out.println( "Received Message: "+incoming);
						System.out.print(countSent + " messages are sent; " + countReceived + " messages are received.");
						break;
					}
					
					countReceived++;
					
					while(!incoming.equals("")){
						toBePrinted += incoming + "\r\n";
						incoming = br.readLine();
					}
					
					System.out.println("Received Message: " + toBePrinted);
				}
				endSocket.close();
			}
			
		} catch(IOException e){
			System.out.println("ERROR");
		}	
	}
}