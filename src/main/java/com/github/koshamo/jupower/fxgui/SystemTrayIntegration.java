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
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.Objects;

import com.github.koshamo.fiddler.EventHandler;
import com.github.koshamo.fiddler.ExitEvent;
import com.github.koshamo.fiddler.MessageBus;

import javafx.application.Platform;

/**
 * SystemTrayIntegration creates the system tray icon and adds it to the 
 * system tray - if supported
 * the update method draws the icon using the given data to decide which
 * state the battery symbol should show.
 * 
 * @author jochen
 *
 */
public class SystemTrayIntegration {

	private final javafx.stage.Stage stage;
	private SystemTray systemTray;
	private TrayIcon trayIcon;
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
	
	/**
	 * The constructor checks, if system tray is supported and creates the 
	 * system tray icon.
	 * 
	 * As this class can show / hide the application window, the stage object
	 * for the application must be provided.
	 * 
	 * The class provides a context menu, which can perform transactions.
	 * These transactions use the message bus to connect to other modules,
	 * but SystemTrayIntegration by itself is no message bus module. Thus
	 * this class needs the reference to its message bus module and the 
	 * message bus, to send requests to the message bus (which need the 
	 * senders source)  
	 * 
	 * @param stage			the stage connected to this class with the
	 * application window to show / hide
	 * @param eventSource	the message module, where this class belongs to
	 * @param messageBus	the message bus to send signals
	 */
	public SystemTrayIntegration (final javafx.stage.Stage stage, 
			final EventHandler eventSource, final MessageBus messageBus) {
		this.stage = Objects.requireNonNull(stage);
		this.eventSource = Objects.requireNonNull(eventSource);
		this.messageBus = Objects.requireNonNull(messageBus);

		// initialize AWT toolkit
		Toolkit.getDefaultToolkit();
		
		if (!SystemTray.isSupported()) {
//			throw new UnsupportedLookAndFeelException("System Tray is not supported on this system!");
			System.out.println("System Tray is not supported on this system!");
			messageBus.postEvent(new ExitEvent(eventSource, null));
		}
		GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice()
				.getDefaultConfiguration();
		
	}

	/**
	 * 
	 */
	public boolean initTrayIcon() {
		systemTray = SystemTray.getSystemTray();
		if (systemTray == null) 
			return false;
		trayIcon = new TrayIcon(new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB));
		addAppToTray();
		return true;
	}
	
	/**
	 * this method creates the pop-up menu for the system tray icon and 
	 * calls the method (update) to create a default view for the icon.
	 * Finally the tray icon is added to the system tray.
	 */
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
	
	/**
	 *	show / hide the application window 
	 */
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
	
	/**
	 * as soon as a battery parameter changes, this method should be called
	 * to redraw the system tray icon to show the current state.
	 *  
	 * @param load		the current battery load in percent
	 * @param supplying	is the battery on line (using line power or battery power)
	 * @param charging	is the battery charging (if not supplied, it can not 
	 * be charged, but if the battery is fully charged, we do not charge further)
	 */
	public void updateIcon(final int load, final boolean supplying, 
			final boolean charging) {
		if (load < 0 || load > 100) {
			// TODO: seriously handle capacity check
			System.out.println("Capacity error");
		}
		// create image, fill background black
		Image offscrnImage = trayIcon.getImage();
		Graphics graphics = offscrnImage.getGraphics();
		drawIcon32x32(graphics, load, supplying);
	
		Toolkit.getDefaultToolkit().sync();
		
		// need this line of code to secure update method is called
		// and image is redrawn immediately
		trayIcon.setImageAutoSize(false);
		
		String tooltip;
		if (supplying)
			if (charging)
				tooltip = load + "%, charging";
			else
				tooltip = "on line power, battery: " + load + "%";
		else
			tooltip = "on battery: " + load + "%";
		trayIcon.setToolTip(tooltip);
	}

	/**
	 * draws the Icon for a 32x32 pixel image 
	 * @param graphics	the graphics context to be drawn on
	 * @param load		the current battery load 
	 * @param supplying	the current supplying /in-line status
	 */
	private void drawIcon32x32(Graphics graphics, final int load, final boolean supplying) {
		drawImageBackground(graphics);

		drawBattery32x32(graphics, load);

		drawCharging32x32(graphics, supplying);
	}

	/**
	 * draws the image background
	 * @param graphics	the graphics context to be drawn on
	 */
	private void drawImageBackground(Graphics graphics) {
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
	}

	/**
	 * draws the charging area of the image
	 * @param graphics	the graphics context to be drawn on
	 * @param supplying	the current supplying / on-line status
	 */
	private void drawCharging32x32(Graphics graphics, final boolean supplying) {
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
	}

	/**
	 * draws the battery area of the image
	 * @param graphics	the graphics context to be drawn on
	 * @param load		the current battery load
	 */
	private void drawBattery32x32(Graphics graphics, final int load) {
		// draw battery outlines
		graphics.setColor(Color.lightGray);
		graphics.drawRect(12, 6, 15, 23);
		graphics.drawRect(16, 3, 8, 3);

		// draw battery fillings depending on capacity
		if (load > YELLOW_UPPER_BOUND)
			graphics.setColor(Color.GREEN);
		else if (load > RED_UPPER_BOUND)
			graphics.setColor(Color.YELLOW);
		else
			graphics.setColor(Color.RED);
		if (load > LOAD_COMPLETE)
			graphics.fillRect(18, 4, 5, 2);
		if (load > LOAD_FULL)
			// full
			graphics.fillRect(14, 8, 12, 4);
		if (load > LOAD_MEDIUM_HIGH) 
			// more than half
			graphics.fillRect(14, 13, 12, 4); 
		if (load > LOAD_MEDIUM_LOW) 
			// less than half
			graphics.fillRect(14, 18, 12, 4);
		if (load > LOAD_LOW) 
			// low
			graphics.fillRect(14, 23, 12, 4);
		// print always
		graphics.fillRect(14, 28, 12, 1);
	}

}
