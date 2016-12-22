package ru.aselit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileConnect extends Thread {

	public static enum FileContentEnum {
		
		fcAuthorize, fcCommand, fcReceive, fcSend, fcDone, fcError;
	}
	
	Socket socket;
	FileContentEnum state = FileContentEnum.fcAuthorize;
	byte[] buffer = null;
	private static final Logger log = LogManager.getLogger(FileConnect.class);
	
	/**
	 * Constructor.
	 * @param socket
	 */
	public FileConnect(Socket socket) {
		
//		super();
		this.socket = socket;
		start();
	}
	
	
	

	@Override
	public void run() {

		byte buf1[] = new byte[1024]; 
		
		do {
			
			if (!Thread.interrupted()) {
			
				try {
					
					InputStream stream = socket.getInputStream();
					int size = stream.read(buf1);
					if (size > 0) {
						
						if (null == buffer) {
							
							buffer = new byte[size];
							System.arraycopy(buf1, 0, buffer, 0, size);
						} else {
							
							byte[] buf2 = new byte[buffer.length + buf1.length];
							System.arraycopy(buffer, 0, buf2, 0, buffer.length);
							System.arraycopy(buf1, 0, buf2, buffer.length, buf1.length);
							buffer = buf2;
						}
					}
					
				} catch (IOException e) {
				}
			} else {
				
				System.out.println("Thread was interrupted.");
				break;
			}

			try {
				
				Thread.sleep(10);
			} catch(InterruptedException e) {
				
				break;
			}
		} while(true);
		
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
