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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

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
	private final int WIDTH = 32;
	private final int HEIGHT = 32;
	

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
//		Image image = null;
//		try {
//			System.out.println(System.getProperty("user.home"));
//			image = ImageIO.read(new File(System.getProperty("user.home") + "/SwProjects/JuPower/charging.png"));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		trayIcon = new TrayIcon(image);
		trayIcon = new TrayIcon(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB));
		updateIcon(0, false);
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
	
	private void updateIcon(final int capacity, final boolean charging) {
		if (capacity < 0 || capacity > 100) {
			// TODO: seriously handle capacity check
			System.out.println("Capacity error");
		}
		// create image, fill background black
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
		
		// draw battery outlines
		Rectangle rectBody = new Rectangle(12, 4, 15, 23);
		Rectangle rectSchnippel = new Rectangle(16, 1, 8, 3);
		graphics.setColor(Color.lightGray);
		graphics.draw(rectBody);
		graphics.draw(rectSchnippel);
		
		// draw battery fillings depending on capacity
		if (capacity > 80)
			graphics.setColor(Color.GREEN);
		else if (capacity > 25)
			graphics.setColor(Color.YELLOW);
		else
			graphics.setColor(Color.RED);
		if (capacity > 97)
			graphics.fillRect(18, 2, 5, 2);
		if (capacity > 80)
			// full
			graphics.fillRect(14, 6, 12, 4);
		if (capacity > 50) 
			// more than half
			graphics.fillRect(14, 11, 12, 4); 
		if (capacity > 25) 
			// less than half
			graphics.fillRect(14, 16, 12, 4);
		if (capacity > 5) 
			// low
			graphics.fillRect(14, 21, 12, 4);
		// print always
		graphics.fillRect(14, 26, 12, 1);
		
		// draw charging
		graphics.setColor(Color.GRAY);
		if (charging) {
			graphics.fillRect(5, 5, 2, 20);
			graphics.fillRect(3, 13, 6, 4);
		}
		else {
			graphics.fillRect(5, 5, 2, 7);
			graphics.fillRect(3, 10, 6, 2);
			graphics.fillRect(5, 17, 2, 7);
			graphics.fillRect(3, 17, 6, 2);
		}
		
		trayIcon.getImage().flush();
		trayIcon.setImage(image);
		String tooltip;
		if (charging)
			tooltip = capacity + "%, charging";
		else
			tooltip = "on battery: " + capacity + "%";
		trayIcon.setToolTip(tooltip);
	}

}
