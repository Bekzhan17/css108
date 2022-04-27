import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Client for a network programming proof of concept..
 * 
 * @author Zoran Zarić <zz@zoranzaric.de>
 *
 */
public class Client {

	/**
	 * The client's id.
	 */
	private String id;
	
	/**
	 * The server's hostname or ip-address.
	 */
	private String host;
	
	/**
	 * The server's port to connect to.
	 */
	private int port;
	
	/**
	 * The path to the output file.
	 */
	private String outputFilePath;
	
	/**
	 * A table that holds the status of all chunks of the file.
	 */
	private HashMap<String, Boolean> recieved;

	/**
	 * Constructor for the client.
	 * 
	 * @param id The client's id.
	 * @param host The server's hostname or ip-address.
	 * @param port The server's port to connect to.
	 */
	public Client(String id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;

		this.outputFilePath = "/tmp/testfile_" + this.id + ".out";

		try {
			//this.recieveFile();
			
			// Retrieve the list of all chunks for the file.
			this.recieved = this.recieveList();
			
			//this.recieveFilePart("c4ca4238a0b92382dcc509a6f75849b");	// Offset: 0
			//this.recieveFilePart("c9e174f5b3f9fc8ea15d152add07294");	// Offset: 1687552
			
			// Retrieve all chunks
			for (String hash : this.recieved.keySet()) {
				this.recieveFilePart(hash);
			}
			
		} catch(IOException e) {
			// Doh!
			System.err.println("Something went wrong!");
		}
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
	 * Creates a zero-filled file with a given size.
	 * 
	 * @param size
	 */
	private void createFile(long size) {
		File file = new File(this.outputFilePath);
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			
			for (long i = 0; i < size; i++) {
				try {
					fileOut.write(0);
				} catch (IOException ioe) {
				}
			}
			
		} catch (FileNotFoundException fnfe) {
		}
	}

	/**
	 * Retrieves the list of chunks from the server.
	 * 
	 * @return The table of chunks, all marked as not recieved
	 * @throws IOException
	 */
	private HashMap<String, Boolean> recieveList() throws IOException {
		try {
			// Connect to the server
			Socket serverSocket = new Socket(this.host, this.port);

			// We want to read strings from the server, so we need a Scanner.
			Scanner in = new Scanner(serverSocket.getInputStream());
			
			// We want to write strings to the server, so we need a PrintWriter.
			PrintWriter out = new PrintWriter(serverSocket.getOutputStream());
	
			// Send the LIST-request to the server
			out.println("LIST");
			out.flush();
			
			HashMap<String, Boolean> recieved = new HashMap<String, Boolean>();
			
			// Retrieve all hashes, puts them into the table and marks them as not received.
			String hash = "";
			while(in.hasNext()) {
				hash = in.nextLine();
				recieved.put(hash, false);
			}
			
			// Disconnect form the server
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Creates the dummy file
			//TODO retrieve the correct file-size
			this.createFile(16384 * 192);
			
			return recieved;
		} catch (UnknownHostException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Receives a whole file.
	 * 
	 * @throws IOException
	 */
	private void recieveFile() throws IOException {
		// Connect to the server
		Socket serverSocket = new Socket(this.host, this.port);
		
		// We want to recieve bytes from the server, so we need a InputStream
		InputStream in = serverSocket.getInputStream();
		
		// We want to write strings to the server, so we need a PrintWriter
		PrintWriter out = new PrintWriter(serverSocket.getOutputStream());

		// Send the ALL-request to the server
		out.println("ALL");
		out.flush();
		
		// Initialize the file
		File file = new File(this.outputFilePath);
		
		// Initialize the FileOutputStream
		FileOutputStream fileOut = new FileOutputStream(file);

		int count;
		byte[] buffer = new byte[16384];
		while ((count = in.read(buffer)) > 0) {
			fileOut.write(buffer, 0, count);
			fileOut.flush();
		}
		
		// Close the FileOutputStream
		fileOut.close();
		
		// Disconnect from server
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Receives a file-chunk by index.
	 * 
	 * @param index
	 * @throws IOException
	 */
	private void recieveFilePart(int index) throws IOException {
		String hash = this.hash(index);
		this.recieveFilePart(hash);
	}
	
	/**
	 * Receives a file-chunk by hash.
	 * 
	 * @param hash
	 * @throws IOException
	 */
	private void recieveFilePart(String hash) throws IOException {
		// Connect to the server
		Socket serverSocket = new Socket(this.host, this.port);
		
		// We want to receive bytes from the server, so we need a InputStream
		InputStream in = serverSocket.getInputStream();
		
		// We want to read strings from the server, so we need a Scanner.
		Scanner     inScanner = new Scanner(in);
		
		// We want to write strings to the server, so we need a PrintWriter
		PrintWriter out = new PrintWriter(serverSocket.getOutputStream());

		System.out.println("Recieving " + hash);
		
		// Send the GET-request to the server
		out.println("GET:" + hash);
		out.flush();
		
		// Retrieve the offset in the file
		long offset = new Long(inScanner.nextLine());
		
		System.out.println("Offset: " + offset);
		
		// Initialize the file
		File file = new File(this.outputFilePath);
		
		// We want to write to a given place in the file so we need a RandomAccessFile
		RandomAccessFile fileOut = new RandomAccessFile(file, "rw");

		// Set the pointer to the offset
		fileOut.seek(offset);
		
		// Retrieve the chunk
		byte[] buffer = new byte[16384];
		int len = in.read(buffer, 0, 16384);
		//TODO the received Chunk is to small and the data is corrupt
		
		// Write the chunk to the file
		fileOut.write(buffer, 0, len);
		
		// Close the RandomAccessFile
		fileOut.close();
		
		// Mark the Chunk as received
		this.recieved.put(hash, true);
		
		// Disconnect from the server
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Client c = new Client(((args.length>0) ? args[0] : "default-client"), "localhost", 8888);
	}

}
