/*	* Name:- Gandhali Girish Shastri
 * ID: 1001548562
 * Lab Assignment - 1
 * -----------------------------------------------------
 * 
 * References:	https://www.jmarshall.com/easy/http/#http1.1c1
 * 				https://www.geeksforgeeks.org/split-string-java-examples/
 * 				https://mark.koli.ch/remember-kids-an-http-content-length-is-the-number-of-bytes-not-the-number-of-characters
 * 				https://www.youtube.com/watch?v=8lXI4YIIR9k&t=578s
 * */

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.awt.BorderLayout;

public class Server {
	
	//Declarations and Initializations
	
	int port;
	static ServerSocket server= null;
	static Socket client=null;
	static ExecutorService pool=null;
	
	static ArrayList<String> users= new ArrayList<String>(3);	//stores the usernames to check whether username exists or not
	private static int MAXCLIENTS = 3;	//Only 3 clients can connect at a time
	private static clientThread[] threads = new clientThread[MAXCLIENTS];		//Creates diff threads for each client
	
	//GUI
	public static JTextArea textArea;
	private JFrame frame;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frame.setVisible(true);
				} catch (Exception e) {
					//System.out.println(e);
				}
			}
		});
		
		//
		try {
			server=new ServerSocket(5000);	//Socket for server with port number
		} catch (Exception e) {
			//System.out.println(e);
		}
		
		int i=0;	
		while(true) {
			try {
				client=server.accept();		//creating new threads as clients join
				for (i = 0; i <= MAXCLIENTS; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(client, threads)).start();	//starts a new thread
                        break;
                    }
                }
                if (i == MAXCLIENTS) {
                    PrintStream os = new PrintStream(client.getOutputStream());
                    os.println("Server too busy. Try later.");
                    textArea.append("Maximum number of clients have already joined.");
                    os.close();
                    client.close();		//close client thread
                }
			} catch(Exception e) {
				//System.out.println(e);
			}
		}
				
	}

	
	public Server() {
		initialize();	//used to build the GUI
	}
	
	//Builds the GUI
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		textArea = new JTextArea();
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		
		
		 JScrollPane scroll = new JScrollPane (textArea);
		 scroll.setBounds(10, 74, 259, 230);
		 scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		 scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		 frame.getContentPane().add(scroll);
	}

//Client thread class
public static class clientThread extends Thread {
		
	    private PrintStream os = null;
	    private Socket clientSocket = null;
	    private final clientThread[] threads;
	    private int maxClientsCount;
	   

	    public clientThread(Socket clientSocket, clientThread[] threads) {
	        this.clientSocket = clientSocket;
	        this.threads = threads;
	        maxClientsCount = threads.length;
	    }
		
	    public void run() {
	    		        
	        try {
	        	        	
	        	BufferedReader is = new BufferedReader(new InputStreamReader(client.getInputStream()));
	            os = new PrintStream(clientSocket.getOutputStream());

	            Date today = new Date();	//Func to get the date
	    		int length=0;
	    		Date current_time = new Date();		//Func to get the time
                String time_to_string = new SimpleDateFormat("k:m:s").format(current_time);
                
                byte[] responseBytes = null;	//this is to calculate the content-length
	    		String response=null;
	    		
	    		//the HTTP response
	    		String httpResponse1 = "GET HTTP / 1.1 200 OK#" + today + time_to_string
	                    + "#Connection Host : " + clientSocket.getLocalAddress() + ":" + clientSocket.getLocalPort()
	                    + "#Content-Type: text/html" 		
	                    + "#User-Agent : User-Agent: Mozilla/4.0";
	    		
	    		String httpResponse = "GET HTTP / 1.1 200 OK\n" + today + time_to_string
	                    + "\nConnection Host : " + clientSocket.getLocalAddress() + ":" + clientSocket.getLocalPort()
	                    + "\nContent-Type: text/html" 		
	                    + "\nUser-Agent : User-Agent: Mozilla/4.0";

	            String username = is.readLine();	//accept username from client
                           
				while(true) {
						//Checks if username exists
						if(users.contains(username)) {
							os.println("re-enter");		//client side handles accepting many usernames until its unique
						}
						else {
								response= "Registeration successfull.\n" + username + " has been connected.";
							
							try {
				    			responseBytes=response.getBytes();		//Content-length
				    		}catch(Exception e) {}
			                
							length=responseBytes.length;
							
							//prints on server side
							textArea.append(httpResponse + "Content-Length : " + length + "\n" + response);
							textArea.append(response+"\n");
							//sends to client
							os.println(response);
	          				os.println(httpResponse1 + "#Content-Length : " + length +  response);
						
							users.add(username);	//stores usernames
							break;
						}
					
						username=is.readLine();		//accepts username after it is flagged as unique
				}
				while(true) {
					
					int time= Integer.parseInt(is.readLine());		//stores the random int 
					int t1=time/1000;	//converts the int to s
					
					response= "Server will wait for " + t1 + " seconds\n";
					length=responseBytes.length;
					
					textArea.append(httpResponse + "Content-Length : " + length + "\n" + response);
					textArea.append(response+"\n");
					os.println(response);
					os.println(httpResponse + "Content-Length : " + length + "\n" + response);
      				
					try {
						Thread.sleep(time);		//thread is put to sleep for the given time
						
						response= "Server waited " + t1 + " seconds for client -" + username +"\n";
						length=responseBytes.length;
												
						textArea.append(httpResponse + "Content-Length : " + length + "\n" + response);
						textArea.append(response+"\n");
						os.println(response);
						os.println(httpResponse + "Content-Length : " + length + "\n" + response);
					}
					catch (InterruptedException e) {
						//System.out.println("Error : "+e);
					}	
				}
	        				
			} 
		    catch(IOException ex){
		    	//System.out.println("Error : "+ex);
		    }
		 
		}
	}
}
