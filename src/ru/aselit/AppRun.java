package ru.aselit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppRun {

	private static final Logger log = LogManager.getLogger(AppRun.class);
	
	public static void main(String[] args) {
		
		try {
			
			ServerSocket srvSocket = new ServerSocket(3128, 0, InetAddress.getByName("localhost"));
			
			while (true) {
				
				Socket socket = srvSocket.accept();
				FileConnect connect = new FileConnect(socket);
			}
		
		} catch (UnknownHostException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Program is finished.");
	}

}
