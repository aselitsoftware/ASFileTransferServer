package ru.aselit;

import java.util.Properties;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;

public class SettingsDialog extends Dialog {

	protected boolean result;
	protected Shell shell;
	private Text textHost;
	private Text textPort;
	private Text textAccessTimeout;
	private Text textStorageDirectory;
	
	private Properties settings;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SettingsDialog(Shell parent, int style) {
		
		super(parent, style);
		setText("Settings");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public boolean open(Properties settings) {
		
		createContents();
		
		this.settings = settings;
		
		textHost.setText(settings.getProperty("host", "localhost"));
		textPort.setText(settings.getProperty("port", "3128"));
		textAccessTimeout.setText(settings.getProperty("accessTimeout", "10000"));
		textStorageDirectory.setText(settings.getProperty("storageDirectory", ""));
		
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		
		FormLayout layout = new FormLayout();
		
		shell = new Shell(getParent(), getStyle());
		shell.setSize(418, 344);
		shell.setText(getText());
		
		shell.setLayout(layout);
		
		Label lblHost = new Label(shell, SWT.NONE);
		lblHost.setLayoutData(new FormData());
		lblHost.setText("Host:");
		
		Label lblPort = new Label(shell, SWT.NONE);
		lblPort.setText("Port:");
		
		Label lblAccessTimeout = new Label(shell, SWT.NONE);
		lblAccessTimeout.setText("Socket access timeout (in milliseconds):");

		Label lblStorageDirectory = new Label(shell, SWT.NONE);
		lblStorageDirectory.setText("Storage directory:");
		
		textHost = new Text(shell, SWT.BORDER);
		
		textPort = new Text(shell, SWT.BORDER);
		
		textAccessTimeout = new Text(shell, SWT.BORDER);
		
		textStorageDirectory = new Text(shell, SWT.BORDER);
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				try {
					if (textHost.getText().length() == 0)
						throw new Exception("Host is not specified.");
					
					try {
						
						new Integer(textPort.getText());
					} catch (NumberFormatException ex) {
						
						throw new Exception("Port must be positive integer.");
					}
					try {
						
						new Integer(textAccessTimeout.getText());
					} catch (NumberFormatException ex) {
						
						throw new Exception("Access timeout must be positive integer.");
					}
					
					if (textStorageDirectory.getText().length() == 0)
						throw new Exception("Storage directory is not specified.");
					
					settings.setProperty("host", textHost.getText());
					settings.setProperty("port", textPort.getText());
					settings.setProperty("accessTimeout", textAccessTimeout.getText());
					settings.setProperty("storageDirectory", textStorageDirectory.getText());
					
					result = true;
					shell.close();
					
				} catch (Exception ex) {
					
					if (!ex.getMessage().isEmpty()) {
						
						MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
						mb.setText(getParent().getText());
						mb.setMessage(ex.getMessage());
						mb.open();
					}
				}
			}
		});
		btnOk.setText("OK");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				result = false;
				shell.close();
			}
		});
		btnCancel.setText("Cancel");
		
		
		FormData fd;

		fd = new FormData();
		fd.top = new FormAttachment(lblPort, 3);
		fd.right = new FormAttachment(100, -6);
		textPort.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(textPort, 0, SWT.LEFT);
		fd.top = new FormAttachment(0, 6);
		lblPort.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.top = new FormAttachment(0, 6);
		lblHost.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.top = new FormAttachment(lblHost, 3);
		fd.right = new FormAttachment(textPort, -6);
		textHost.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.top = new FormAttachment(textHost, 6);
		lblAccessTimeout.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.top = new FormAttachment(lblAccessTimeout, 3);
		textAccessTimeout.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.top = new FormAttachment(textAccessTimeout, 6);
		lblStorageDirectory.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.top = new FormAttachment(lblStorageDirectory, 3);
		fd.right = new FormAttachment(100, -6);
		textStorageDirectory.setLayoutData(fd);
		
		fd = new FormData();
		fd.width = 75;
		fd.right = new FormAttachment(100, -6);
		fd.bottom = new FormAttachment(100, -6);
		btnCancel.setLayoutData(fd);
		
		fd = new FormData();
		fd.width = 75;
		fd.right = new FormAttachment(btnCancel, -6);
		fd.bottom = new FormAttachment(100, -6);
		btnOk.setLayoutData(fd);
	}
}
