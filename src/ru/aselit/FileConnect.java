package ru.aselit;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.aselit.FileConnectCommandEnum;

public class FileConnect extends Thread {

	public static enum FileContentStateEnum {
		
		fcsAuthorize, fcsCommand, fcsReceive, fcsSend, fcsDone, fcsError;
	}
	
	Socket socket;
	FileContentStateEnum state = FileContentStateEnum.fcsAuthorize;
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
	
	/**
	 * 
	 * @param block Data block from socket.
	 */
	private void handleCommand(byte[] block) {
		
		JSONParser parser = new JSONParser();
		try {
			
//			String data = new String(block);
			JSONObject obj = (JSONObject )parser.parse(new String(block));
			Long commandCode = new Long((long) obj.get("command"));
					
			FileConnectCommandEnum command = FileConnectCommandEnum.fromInt(commandCode.intValue());
			
			if (FileContentStateEnum.fcsAuthorize == state) {
				
//				need to check login and password
				String login = (String )obj.get("login");
				String password = (String )obj.get("password");
				if (!login.equals("1") || !password.equals("1"))
					throw new Exception("Authorization failed! Wrong login or password.");
//				response
//				ready for commands
				state = FileContentStateEnum.fcsCommand;
				return;
			}
			
			
		} catch (ParseException ex) {
		
			log.error(ex);
			
		} catch (Exception ex) {
		
			log.error(ex);
		}
	}
	
	/**
	 * Reading data block from buffer. 
	 * @return
	 */
	private byte[] readBlock() {
	
		byte[] block;
		try {
		
			if (null == buffer)
				throw new Exception("Buffer is null.");
			if (buffer.length < Integer.BYTES)
				throw new Exception("Buffer has wrong length.");
//			get block size (decode first four bytes)
			int size = ((buffer[3] << 24) + (buffer[2] << 16) + (buffer[1] << 8) + (buffer[0] << 0));
			if ((size <= 0) || (buffer.length < (Integer.BYTES + size)))
				throw new Exception("Block has wrong length.");
			
//			get block data
			block = new byte[size];
			System.arraycopy(buffer, Integer.BYTES, block, 0, size);
			
//			delete block from buffer
			size += Integer.BYTES;
			if (buffer.length > size) {
				
				byte[] newBuf = new byte[buffer.length - size];
				System.arraycopy(buffer, size, newBuf, 0, buffer.length - size);
				buffer = newBuf;
			} else
				buffer = null;
				
		} catch (Exception ex) {
			
			block = null; 
			log.error(ex);
		}
		return block;
	}

	@Override
	public void run() {

		byte inBuf[] = new byte[1024]; 
		
		do {
			
			if (!Thread.interrupted()) {
			
				try {
					
					InputStream stream = socket.getInputStream();
					
					
					int size = stream.read(inBuf);
					if (size > 0) {
						
						if (null == buffer) {
							
							buffer = new byte[size];
							System.arraycopy(inBuf, 0, buffer, 0, size);
						} else {
							
							byte[] newBuf = new byte[buffer.length + inBuf.length];
							System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
							System.arraycopy(inBuf, 0, newBuf, buffer.length, inBuf.length);
							buffer = newBuf;
						}
					}
					
					while (true) {
						
						byte[] block = readBlock();
						if (null != block) {
							
//							handle block
//							fcAuthorize - read authorize block
//							fcCommand - read command block
//							fcReceive - receive file data
//							fcsSend - send requested file
							switch (state) {
							case fcsAuthorize:
							case fcsSend:
							case fcsCommand: handleCommand(block);
								break;
//							case fcReceive: handleReceive(block);
							}
						} else
							break;
					}
					
				} catch (IOException e) {
				
					state = FileContentStateEnum.fcsError;
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
