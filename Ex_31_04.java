package com.example.bekzhan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Server for a network programming proof of concept.
 * 
 * @author Zoran Zarić <zz@zoranzaric.de>
 *
 */
public class Server {
	
	/**
	 * The default port the server binds to.
	 */
	private static int PORT = 8888;
	
	/**
	 * The size of a chunk.
	 */
	private static int CHUNKSIZE = 16384;
	
	/**
	 * The socket clients connect to.
	 */
	private ServerSocket serverSocket;
	
	/**
	 * The port the server binds to.
	 */
	private int port;
	
	/**
	 * The file-path of the served file.
	 */
	private String filePath;
	
	/**
	 * A table of chunk-tables for more than one file.
	 * At the moment this isn't really used, in the future it is planned  that more than one file can be served.
	 */
	private HashMap<String, HashMap<String, Integer>> fileMap;
	
	/**
	 * The constructor for the server.
	 * 
	 * @param port The port to bind to.
	 * @param filePath The filepath for the file to be served.
	 */
	public Server(int port, String filePath) {
		this.port = port;
		this.filePath = filePath;
		this.fileMap = new HashMap<String, HashMap<String,Integer>>();
		
		// Bind to the given port.
		this.openPort(this.port);
	}
	
	/**
	 * Hashes a index. We have this abstraction-method so we can replace the hash-function.
	 * 
	 * @param index
	 * @return The hashed index. If the chosen hash-function doesn't exist the index as a string is returned.
	 */
	private String hash(int index) {
		byte[] defaultBytes = String.valueOf(index).getBytes();
		try{
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();
		            
			StringBuffer hexString = new StringBuffer();
			for (int i=0;i<messageDigest.length;i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			
			return hexString.toString();
		}catch(NoSuchAlgorithmException nsae){
			// If the hash-function we chose doesn't exist we just return the index as a string.
			return String.valueOf(index);
		}
	}
	
	/**
	 * Gets a chunk-table. If it doesn't exist yet it is generated.
	 * 
	 * @param file
	 * @return
	 */
	private HashMap<String, Integer> getHashMap(File file) {
		if (this.fileMap.containsKey(file.getAbsolutePath())) {
			// If the chunk-table already exists return it
			return this.fileMap.get(file.getAbsolutePath());
		} else {
			// Initialize a new chunk-table
			HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
			
			// Calculate how many chunks we need
			long chunks = file.length() / CHUNKSIZE;
			long lastChunkSize = file.length() % CHUNKSIZE;
			if ( lastChunkSize != 0) {
				chunks++;
			}
			
			// Fill the chunk-table
			for (int i = 1; i <= chunks; i++) {
				hashMap.put(hash(i), i);
			}
			
			this.fileMap.put(file.getAbsolutePath(), hashMap);
			return hashMap;
		}
	}
	
	/**
	 * Open the port.
	 * 
	 * @param port
	 */
	private void openPort(int port) {
		try {
			this.serverSocket = new ServerSocket(port);
		} catch(IOException e) {
			System.out.println("Couldn't listen on Port " + port + "!");
			System.exit(-1);
		}
	}
	
	/**
	 * Handles a client-request.
	 * Possible requests are:
	 *   ALL - send a whole file
	 *   LIST - send a chunk-list
	 *   GET:hash - send a chunk
	 */
	public void handleRequest() {
		Socket client = null;
		try {
			// Accept a client-connection
			client = this.serverSocket.accept();
			
			// We want to read strings from the client, so we need a Scanner.
			Scanner in  = new Scanner(client.getInputStream());
			//in.useDelimiter(".");
			
			// Read the request
			String request = in.nextLine();
			
			// handle the request
			if (request.equals("ALL")) {
				this.sendFile(client);
			} else if (request.equals("LIST")) {
				this.sendHashMap(client);
			} else if (request.startsWith("GET:")) {
				String[] requestParts = request.split(":");
				this.sendFilePart(client, requestParts[1]);
			}else {
				System.out.println("Unknown request: " + request);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close the client-connection
			if (client!=null) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Send a whole file to the client.
	 * 
	 * @param client
	 * @throws IOException
	 */
	private void sendFile(Socket client) throws IOException {
		Scanner      in       = new Scanner(client.getInputStream());
		PrintWriter  printOut = new PrintWriter(client.getOutputStream(), true);
		
		// We want to write bytes to the client, so we need a OutputStream
		OutputStream out      = client.getOutputStream();
		
		// Initialize the file an check for existance
		File file = new File(this.filePath);
		if (!file.exists()) {
			System.out.println("The file doesn't exist!");
			System.exit(-1);
		}
		
		// initialize the FileInputStream
		InputStream fileInput = null;
		try {
			 fileInput = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("The file doesn't exist!");
			System.exit(-1);
		}
		
		byte[] buffer = new byte[CHUNKSIZE];
		
		int readData;
		
		System.out.println("Sending file...");
		// Read the file and send it to the client
		int sendcounter = 0;
		while ( (readData = fileInput.read(buffer)) != -1 ) {
			out.write(buffer, 0, readData);
			System.out.print(".");
			sendcounter++;
		}
		
		System.out.println("\nfinished (" + sendcounter +")!");
	}
	
	/**
	 * Send a chunk to the client.
	 * 
	 * @param client
	 * @param hash
	 * @throws IOException
	 */
	private void sendFilePart(Socket client, String hash) throws IOException {
		Scanner      in       = new Scanner(client.getInputStream());
		
		// We want to write strings to the client, so we need a PrintWriter
		PrintWriter  printOut = new PrintWriter(client.getOutputStream(), true);
		
		// We want to write bytes to the client, so wee need a OutputStream
		OutputStream out      = client.getOutputStream();
		
		// Initialize the file and check for existance
		File file = new File(this.filePath);
		if (!file.exists()) {
			System.out.println("The file doesn't exist!");
			System.exit(-1);
		}
		
		// Calculate how many chunks the file consists of
		long chunks = file.length() / CHUNKSIZE;
		long lastChunkSize = file.length() % CHUNKSIZE;
		if ( lastChunkSize != 0) {
			chunks++;
		}
		
		// Get the chunk-table
		HashMap<String, Integer> hashMap = this.getHashMap(file);
		
		// Get the index for the requested chunk
		int index = hashMap.get(hash).intValue();
		
		System.out.println("Hash: " +  hash + " Index: " + index);
		
		// Calculate the offset in the file for the requested chunk
		long offset = (index-1) * CHUNKSIZE;
		
		// Initialize the FileInputStream
		FileInputStream fileInput = null;
		try {
			 fileInput = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("The file doesn't exist!");
			System.exit(-1);
		}
		
		byte[] buffer = new byte[CHUNKSIZE];
		
		System.out.println("Reading from file at offset: " + offset);
		// Move the pointer to the offset
		fileInput.skip(offset);
		
		// Read the chunk
		int len = fileInput.read(buffer, 0, CHUNKSIZE);
		System.out.println("Sending to client");
		
		// Send the offset to the client
		printOut.println(String.valueOf(offset));
		
		// Send the chunk to the client
		out.write(buffer, 0, len);
	}
	
	/**
	 * Send a hash-list to the client.
	 * 
	 * @param client
	 * @throws IOException
	 */
	private void sendHashMap(Socket client) throws IOException {
		Scanner      in       = new Scanner(client.getInputStream());
		OutputStream out      = client.getOutputStream();
		
		// We want to write strings to the client, so we need a PrintWriter
		PrintWriter  printOut = new PrintWriter(client.getOutputStream(), true);
		
		// Initialize the file and check for existance
		File file = new File(this.filePath);
		if (!file.exists()) {
			System.out.println("The file doesn't exist!");
			System.exit(-1);
		}
		
		// Get the chunk-table
		HashMap<String, Integer> hashMap = this.getHashMap(file);
		
		// Send the hashes to the client
		for (String hash : hashMap.keySet()) {
			printOut.println(hash);
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		Server s = new Server(PORT, ((args.length>0) ? args[0] : "/tmp/testfile"));
		
		while(true) {
			s.handleRequest();
		}
	}

}
