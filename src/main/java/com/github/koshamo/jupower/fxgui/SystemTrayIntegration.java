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

import com.github.koshamo.fiddler.EventHandler;
import com.github.koshamo.fiddler.ExitEvent;
import com.github.koshamo.fiddler.MessageBus;

import javafx.application.Platform;

/**
 * @author jochen
 *
 */
public class SystemTrayIntegration {

	private final javafx.stage.Stage stage;
	private final SystemTray systemTray;
	private final TrayIcon trayIcon;
	private final EventHandler eventSource;
	private final MessageBus messageBus;
	private final int WIDTH = 32;
	private final int HEIGHT = 32;
	private final int YELLOW_UPPER_BOUND = 74;
	private final int RED_UPPER_BOUND = 24;
	private final int LOAD_LOW = 5;
	private final int LOAD_MEDIUM_LOW = 24;
	private final int LOAD_MEDIUM_HIGH = 49;
	private final int LOAD_FULL = 79;
	private final int LOAD_COMPLETE = 97;

	public SystemTrayIntegration (final javafx.stage.Stage stage, 
			final EventHandler eventSource, final MessageBus messageBus) {
		this.stage = stage;
		this.eventSource = eventSource;
		this.messageBus = messageBus;

		// initialize AWT toolkit
		Toolkit.getDefaultToolkit();
		
		if (!SystemTray.isSupported()) {
//			throw new UnsupportedLookAndFeelException("System Tray is not supported on this system!");
			System.out.println("System Tray is not supported on this system!");
			messageBus.postEvent(new ExitEvent(eventSource, null));
		}
		systemTray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB));
		
		addAppToTray();
	}
	
	private void addAppToTray()  {
		updateIcon(0, false, false);
		trayIcon.addActionListener(event -> Platform.runLater(this::toggleStageShowing));
		
		MenuItem itmShowHide = new MenuItem("Show / Hide");
		itmShowHide.addActionListener(event -> Platform.runLater(this::toggleStageShowing));
		MenuItem itmExit = new MenuItem("Exit");
		itmExit.addActionListener(event -> {
			systemTray.remove(trayIcon);
			messageBus.postEvent(new ExitEvent(eventSource, null));
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
	
	public void updateIcon(final int capacity, final boolean supplying, 
			final boolean charging) {
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
		Rectangle rectBody = new Rectangle(12, 6, 15, 23);
		Rectangle rectSchnippel = new Rectangle(16, 3, 8, 3);
		graphics.setColor(Color.lightGray);
		graphics.draw(rectBody);
		graphics.draw(rectSchnippel);

		// draw battery fillings depending on capacity
		if (capacity > YELLOW_UPPER_BOUND)
			graphics.setColor(Color.GREEN);
		else if (capacity > RED_UPPER_BOUND)
			graphics.setColor(Color.YELLOW);
		else
			graphics.setColor(Color.RED);
		if (capacity > LOAD_COMPLETE)
			graphics.fillRect(18, 4, 5, 2);
		if (capacity > LOAD_FULL)
			// full
			graphics.fillRect(14, 8, 12, 4);
		if (capacity > LOAD_MEDIUM_HIGH) 
			// more than half
			graphics.fillRect(14, 13, 12, 4); 
		if (capacity > LOAD_MEDIUM_LOW) 
			// less than half
			graphics.fillRect(14, 18, 12, 4);
		if (capacity > LOAD_LOW) 
			// low
			graphics.fillRect(14, 23, 12, 4);
		// print always
		graphics.fillRect(14, 28, 12, 1);

		// draw charging
		graphics.setColor(Color.GRAY);
		if (supplying) {
			graphics.fillRect(5, 6, 2, 20);
			graphics.fillRect(3, 14, 6, 4);
		}
		else {
			graphics.fillRect(5, 6, 2, 7);
			graphics.fillRect(3, 11, 6, 2);
			graphics.fillRect(5, 18, 2, 7);
			graphics.fillRect(3, 18, 6, 2);
		}

		trayIcon.setImage(image);
		String tooltip;
		if (supplying)
			if (charging)
				tooltip = capacity + "%, charging";
			else
				tooltip = "on line power, battery: " + capacity + "%";
		else
			tooltip = "on battery: " + capacity + "%";
		trayIcon.setToolTip(tooltip);
	}

}
