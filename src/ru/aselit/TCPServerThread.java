package ru.aselit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.aselit.MD5;
import ru.aselit.FileTransferInputBuffer;
import ru.aselit.FileTransferOutputBuffer;
import static ru.aselit.FileModeEnum.*;
import static ru.aselit.FileTransferCommandEnum.*;

public class TCPServerThread extends Thread {

	
	public interface TCPServerThreadInterface {

		public void showTCPServerThread(long id, String ip, String fileName, String state);
	}
	
	
	private Socket socket;
	private FileTransferCommandEnum state = ftcAuthorize;
	private FileTransferInputBuffer buffer = new FileTransferInputBuffer();
	private long id;
	private TCPServerThreadInterface tcpSrvThreadInterfaceImpl;
	private String fileName = "";
	private String storageDirectory;
	
	
	private static final Logger log = LogManager.getLogger(TCPServerThread.class);
	
	/**
	 * Constructor.
	 * @param socket
	 */
	public TCPServerThread(long id, String storageDirectory, Socket socket,
			TCPServerThreadInterface tcpSrvThreadInterfaceImpl) {
		
		this.id = id;
		this.storageDirectory = storageDirectory;
		this.socket = socket;
		this.tcpSrvThreadInterfaceImpl = tcpSrvThreadInterfaceImpl;
		
		start();
	}
	
	/**
	 * Returns the unique thread id.
	 * @return 
	 */
	public long getId() {
		
		return id;
	}
	
	/**
	 * Returns the IP address.
	 * @return
	 */
	public String getIP() {
		
		return socket.getInetAddress().getHostAddress();
	}
	
	
	/**
	 * Returns the name of file.
	 * @return
	 */
	public String getFileName() {
		
		return fileName;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private String getTempFileName(String fileName) {
		
		return fileName.concat(".tmp");
	}
	
	/**
	 * 
	 * @param fileName
	 * @param completed
	 * @return
	 */
	private String getFullFileName(String fileName, boolean temporary) {
		
		String fullFileName = (storageDirectory.length() == 0) ? "./" : storageDirectory;
		return fullFileName.concat((!temporary) ? getTempFileName(fileName) : fileName);
	}
	
	/**
	 * 
	 * @param fileName Name of file.
	 * @param completed
	 * @return
	 * @throws Exception
	 */
	private File lookForFile(String fileName, boolean temporary) {
		
		String fullFileName = getFullFileName(fileName, temporary);
		File file = new File(fullFileName);
		return (!file.exists()) ? null : file;
	}
	
	/**
	 * 
	 * @param block Data block from socket.
	 */
	private void handleCommand(byte[] block) {
		
		String fullFileName;
		JSONParser parser = new JSONParser();
		
		try {
			
//			String data = new String(block);
			JSONObject obj = (JSONObject) parser.parse(new String(block));
			Long value = new Long((long) obj.get("command"));
					
			FileTransferCommandEnum command = FileTransferCommandEnum.fromInt(value.intValue());
			
			switch (command) {
			
			case ftcAuthorize:
				
//				need to check login and password
				String login = (String) obj.get("login");
				String password = (String) obj.get("password");
				if (!login.equals("1") || !password.equals("1"))
					throw new Exception("Authorization failed! Wrong login or password.");
				
				if (log.isDebugEnabled())
					log.debug(String.format("Thread ID %d was authorizated succesfully.", id));
				
//				response
				obj = new JSONObject();
				obj.put("command", command.toInt());
				FileTransferOutputBuffer.sendBlock(socket, obj.toString());
				
//				ready for commands
				state = ftcFileInfo;
				break;
			
			case ftcFileInfo:
			
				fileName = (String) obj.get("fileName");
//				long fileLen = (long) obj.get("fileLen");
				
//				value = new Long((long) obj.get("fileMode"));
//				FileModeEnum fileMode = FileModeEnum.fromInt(value.intValue());
				
//				response
				obj = new JSONObject();
				obj.put("command", command.toInt());
//				obj.put("fileName", fileName);
				
				File file = lookForFile(fileName, true/*!fileMode.equals(fmUpload)*/);
				if (null != file) {
					
//					if ((fileLen < 0) || (fileLen > file.length()))
//						fileLen = -1;
					obj.put("fileExists", true);
					obj.put("fileSize", file.length());
					obj.put("fileMD5", MD5.getFileHash(file));
				} else
					obj.put("fileExists", false);
				FileTransferOutputBuffer.sendBlock(socket, obj.toString());
				
				state = ftcUpload;
				break;	
			}
			
		} catch (ParseException ex) {
		
			if (log.isDebugEnabled())
				log.debug(ex);
			
		} catch (Exception ex) {
		
			if (log.isDebugEnabled())
				log.debug(ex);
		}
	}
	
	@Override
	public void run() {

		byte inBuf[] = new byte[1024]; 
		
		while (!Thread.interrupted()) {
			
			try {
				
				tcpSrvThreadInterfaceImpl.showTCPServerThread(id,
					socket.getInetAddress().getHostAddress(),
					fileName,
					FileTransferCommandEnum.toString(state));
				
				InputStream stream = socket.getInputStream();
				
				
				int size = stream.read(inBuf);
				buffer.write(inBuf, size);
				
//				try to read next block from buffer	
				byte[] block = buffer.readBlock();
				if (null != block) {
										
//					fcAuthorize - read authorize block
//					fcCommand - read command block
//					fcReceive - receive file data
//					fcsSend - send requested file
					switch (state) {
					case ftcAuthorize:
//					case tcptSend:
					case ftcFileInfo: handleCommand(block);
						break;
//					case fcReceive: handleReceive(block);
					}
				}
			} catch (IOException e) {
			
//				state = tcptError;
			}

			try {
				
				Thread.sleep(10);
			} catch(InterruptedException e) {
				
				break;
			}
		}
		
		try {
			
			socket.close();
		} catch (IOException e) {
		
		}
	}
}
