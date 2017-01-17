package ru.aselit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

import ru.aselit.TCPServerThread.TCPServerThreadInterface;

import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class MainWindow implements TCPServerThreadInterface {

	protected Shell shell;
	private Table tableTransferList;
	private Label lblMessage;
	
	private ReceiveThread receiveThread = null;
	private ServerSocket srvSocket = null;
	
	private Properties settings = new Properties();
	
	private final String SETTINGS_FILE_NAME = "./settings.xml"; 
	private FormData fd_lblMessage;

	private static final Logger log = LogManager.getLogger(MainWindow.class);
	
	/**
	 * Show the state of server socket.
	 * @param state
	 * @param error
	 */
	private void setMessage(String message, boolean error) {
		
		if (error)
			message = String.format("%s Fix error and restart application.", message);
		lblMessage.setText(message);
		lblMessage.setForeground(SWTResourceManager.getColor(error ? SWT.COLOR_RED : SWT.COLOR_BLACK));
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		
		Display display = Display.getDefault();
		createContents();

		try {
			
			settings.loadFromXML(new FileInputStream(SETTINGS_FILE_NAME));
			
			int port = new Integer(settings.getProperty("port", "3128"));
			int timeout = new Integer(settings.getProperty("accessTimeout", "10000"));
			String host = settings.getProperty("host", "localhost");
			String storageDirectory = settings.getProperty("storageDirectory", "");
			
			srvSocket = new ServerSocket(port, 0, InetAddress.getByName(host));
			srvSocket.setSoTimeout(timeout);
			setMessage(String.format("Address \"%s\" and port %d have been bound successfully.", host, port), false);
			
//			start the file receive thread
			if (null != srvSocket)
				receiveThread = new ReceiveThread(storageDirectory, srvSocket, this);
		
		} catch (UnknownHostException ex) {
			
			setMessage(ex.getMessage(), true); 
		} catch (IOException ex) {
			
			setMessage(ex.getMessage(), true);
		}
		
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		
		FormLayout layout = new FormLayout();
		
		shell = new Shell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
				
				if (null != receiveThread)
					receiveThread.interrupt();
			}
		});
		shell.setSize(750, 450);
		shell.setText("File transfer server");
		shell.setLayout(layout);

		lblMessage = new Label(shell, SWT.NONE);
		lblMessage.setText("");
		
		tableTransferList = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tableTransferList.setHeaderVisible(true);
		tableTransferList.setLinesVisible(true);
		
		TableColumn tblclmnID = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnID.setWidth(100);
		tblclmnID.setText("ID");
		
		TableColumn tblclmnIP = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnIP.setWidth(100);
		tblclmnIP.setText("IP");
		
		TableColumn tblclmnFile = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnFile.setWidth(300);
		tblclmnFile.setText("File");
		
		TableColumn tblclmnState = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnState.setWidth(100);
		tblclmnState.setText("State");
		
		TableColumn tblclmnProgress = new TableColumn(tableTransferList, SWT.RIGHT);
		tblclmnProgress.setWidth(100);
		tblclmnProgress.setText("Progress");
		
		Button btnClose = new Button(shell, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			
				shell.close();
			}
		});
		btnClose.setText("Close");
		
		Button btnSettings = new Button(shell, SWT.NONE);
		btnSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			
				SettingsDialog form = new SettingsDialog(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				if (form.open(settings)) {
					
						try {
							
							settings.storeToXML(new FileOutputStream(SETTINGS_FILE_NAME), "");
							
						} catch (IOException ex) {
							
							log.error(ex);
						}
				}
				form = null;
			}
		});
		btnSettings.setText("Settings");
		
		FormData fd;
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.right = new FormAttachment(100, -6);
		fd.top = new FormAttachment(0, 6);
		lblMessage.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.bottom = new FormAttachment(100, -6);
		btnSettings.setLayoutData(fd);
				
		fd = new FormData();
		fd.right = new FormAttachment(100, -6);
		fd.bottom = new FormAttachment(100, -6);
		btnClose.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.right = new FormAttachment(100, -6);
		fd.top = new FormAttachment(lblMessage, 6);
		fd.bottom = new FormAttachment(btnSettings, -6);
		tableTransferList.setLayoutData(fd);
	}

	@Override
	public void showTCPServerThread(long id, String ip, String fileName, String state) {
		
		Display.getDefault().asyncExec(new Runnable() {
		    
			public void run() {
				
				int i;
				TableItem tableItem = null;
				
				i = 0;
				while (i < tableTransferList.getItemCount()) {
					
					tableItem = tableTransferList.getItem(i);
					if (new Long(tableItem.getText(0)).equals(id))
						break;
					i++;
				}
				
				if (i >= tableTransferList.getItemCount()) {
					
					tableItem = new TableItem(tableTransferList, SWT.NONE);
					tableItem.setText(0, String.format("%d", id));
					tableItem.setText(1, ip);
				}
					
				if (null == tableItem)
					return;
				
				tableItem.setText(2, fileName);
				tableItem.setText(3, state);
			}
		});
	}
}
