/*
 * Copyright [2017] [Dr. Jochen Raßler]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.koshamo.jupower.fxgui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.koshamo.fiddler.MessageBus;

import javafx.application.Platform;

/**
 * @author jochen
 *
 */
public class SystemTrayIntegration {

	private final javafx.stage.Stage stage;
	private SystemTray systemTray;
	private TrayIcon trayIcon;
	private final MessageBus messageBus;
	

	public SystemTrayIntegration (final javafx.stage.Stage stage, final MessageBus messageBus) {
		this.stage = stage;
		this.messageBus = messageBus;
		addAppToTray();
	}
	
	private void addAppToTray()  {
		// initialize AWT toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		if (!SystemTray.isSupported()) {
//			throw new UnsupportedLookAndFeelException("System Tray is not supported on this system!");
			System.out.println("System Tray is not supported on this system!");
			Platform.exit();
			// TODO: send exit message to message bus
		}
		systemTray = SystemTray.getSystemTray();
		Image image = null;
		try {
			System.out.println(System.getProperty("user.home"));
			image = ImageIO.read(new File(System.getProperty("user.home") + "/SwProjects/JuPower/charging.png"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		trayIcon = new TrayIcon(image);
		trayIcon.addActionListener(event -> Platform.runLater(this::toggleStageShowing));
		
		MenuItem itmShowHide = new MenuItem("Show / Hide");
		itmShowHide.addActionListener(event -> Platform.runLater(this::toggleStageShowing));
		MenuItem itmExit = new MenuItem("Exit");
		itmExit.addActionListener(event -> {
			Platform.exit();
			systemTray.remove(trayIcon);
			// TODO: send exit message to message bus
		});
		final PopupMenu popup = new PopupMenu();
		popup.add(itmShowHide);
		popup.add(itmExit);
		trayIcon.setPopupMenu(popup);
		
		
		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void toggleStageShowing() {
		if (stage.isShowing()) {
			stage.setIconified(true);
			stage.hide();
		}
		else if (!stage.isShowing() || stage.isIconified()) {
			stage.setIconified(false);
			stage.show();
			stage.toFront();
		}
	}

}
