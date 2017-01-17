package ru.aselit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.aselit.TCPServerThread.TCPServerThreadInterface;

public class ReceiveThread extends Thread {

	private ServerSocket srvSocket;
	private long socketId = -1;
	private TCPServerThreadInterface tcpSrvThreadInterfaceImpl;
	private String storageDirectory = "";
	
	private List<TCPServerThread> threads = new ArrayList<TCPServerThread>();
	
	private static final Logger log = LogManager.getLogger(ReceiveThread.class);
	
	/**
	 * 
	 */
	public ReceiveThread(String storageDirectory, ServerSocket srvSocket,
		TCPServerThreadInterface tcpSrvThreadInterfaceImpl) {
		
		this.storageDirectory = storageDirectory;
		this.srvSocket = srvSocket;
		this.tcpSrvThreadInterfaceImpl = tcpSrvThreadInterfaceImpl;
		start();
	}
	
	@Override
	public void run() {
		
		while (!Thread.interrupted()) {
			
			try {
			
				try {
					
					if (null != srvSocket) {
					
						Socket socket = srvSocket.accept();
						threads.add(new TCPServerThread(++socketId, storageDirectory,
								socket, tcpSrvThreadInterfaceImpl));
					}
				
				} catch (UnknownHostException ex) {
					
					log.error(ex);
				} catch (IOException ex) {
					
					log.error(ex);
				}
				
				Thread.sleep(1000);
			
			} catch(InterruptedException e) {
			
				break;
			}
		}
		
		try {
			
			for (TCPServerThread thread : threads) {
				
				thread.interrupt();
			}
			srvSocket.close();
		
		} catch (IOException e) {
		}
	}
}
