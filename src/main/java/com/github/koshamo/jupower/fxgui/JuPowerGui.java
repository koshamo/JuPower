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

import com.github.koshamo.fiddler.Event;
import com.github.koshamo.fiddler.MessageBus.ListenerType;
import com.github.koshamo.fiddler.jfx.FiddlerFxApp;
import com.github.koshamo.jupower.shared.EventKeys;
import com.github.koshamo.jupower.shared.StrBoolDataEvent;
import com.github.koshamo.jupower.shared.StrIntDataEvent;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * JuPowerGui is the actual GUI for the JuPower application.
 * 
 * Notice that this class is extended from FiddlerFxApp, not javafx.application
 * and the applications entry point is in JuPower.java
 * 
 * @author jochen
 *
 */
/**
 * @author jochen
 *
 */
public class JuPowerGui extends FiddlerFxApp {

	SystemTrayIntegration systemTray;
	IntegerProperty onBatteryLoad;
	BooleanProperty onSupplying;
	BooleanProperty onCharging;
	
	private final int EARLY_WARNING = 20;
	private final int URGENT_WARNING = 7;
	

	/* (non-Javadoc)
	 * general GUI definition in start...
	 * 
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		javax.swing.SwingUtilities.invokeLater(
				() -> { systemTray = new SystemTrayIntegration(primaryStage, this, getMessageBus()); 
				});
		
		HBox hbox = new HBox();
		hbox.getChildren().addAll(buildMainPane());
		
		primaryStage.setScene(new Scene(hbox));
		primaryStage.setTitle("JuPower");
		primaryStage.sizeToScene();
		// prevent application to be closed, when last window is closed
		Platform.setImplicitExit(false);
		
		createProperties();
		getMessageBus().registerAllEvents(this, ListenerType.TARGET);
	}

	
	/**
	 * this method creates the main content pane for the GUI
	 * 
	 * @return the content pane
	 */
	private Node buildMainPane() {
		Label lbl = new Label("currently no content available");
		return lbl;
	}


	/**
	 * this method needs to be called in start and creates the properties
	 * as well as the functionality for their listeners
	 */
	private void createProperties() {
		onBatteryLoad = new SimpleIntegerProperty(0);
		onSupplying = new SimpleBooleanProperty(false);
		onCharging = new SimpleBooleanProperty(false);
		BatteryWarningPopupWindow bwpw = new BatteryWarningPopupWindow();

		onBatteryLoad.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				systemTray.updateIcon(newValue.intValue(), onSupplying.get(), onCharging.get());
				// battery load falling beneath EARLY_WARNING
				if (newValue.intValue() == EARLY_WARNING && 
						oldValue.intValue() == EARLY_WARNING + 1)
					if (!bwpw.isShowing())
						bwpw.show(onBatteryLoad.get(), BatteryWarningPopupWindow.WarningType.EARLY);
				// battery load falling beneath URGENT_WARNING
				if (newValue.intValue() == URGENT_WARNING && 
						oldValue.intValue() == URGENT_WARNING + 1)
					if (!bwpw.isShowing())
						bwpw.show(onBatteryLoad.get(), BatteryWarningPopupWindow.WarningType.URGENT);
			}
		});
		
		onSupplying.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				systemTray.updateIcon(onBatteryLoad.get(), newValue.booleanValue(), onCharging.get());
			}
		});

		onCharging.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				systemTray.updateIcon(onBatteryLoad.get(), onSupplying.get(), newValue.booleanValue());
			}
		});
	}
	
	
	/* (non-Javadoc)
	 * 
	 * shutdown is called by the message bus, if a module sends a application
	 * exit signal. Thus we need to clear things up and prepare for application
	 * exit
	 * 
	 * @see com.github.koshamo.fiddler.EventHandler#shutdown()
	 */
	@Override
	public void shutdown() {
		getMessageBus().unregisterAllEvents(this);
		Platform.exit();
	}
	
	/* (non-Javadoc)
	 * 
	 * handle cares for incoming signals via message bus
	 * 
	 * @see com.github.koshamo.fiddler.EventHandler#handle(com.github.koshamo.fiddler.Event)
	 */
	@Override
	public void handle(Event event) {
		if (event instanceof StrBoolDataEvent) {
			StrBoolDataEvent de = (StrBoolDataEvent) event;
			if (de.getMetaInformation().equals(EventKeys.SUPPLYING.getKey()))
				Platform.runLater(()-> onSupplying.set(de.getData().booleanValue()));
			if (de.getMetaInformation().equals(EventKeys.CHARGING.getKey()))
				Platform.runLater(()-> onCharging.set(de.getData().booleanValue()));
		}
		if (event instanceof StrIntDataEvent) {
			StrIntDataEvent de = (StrIntDataEvent) event;
			if (de.getMetaInformation().equals(EventKeys.BATTERY.getKey()))
				Platform.runLater(()-> onBatteryLoad.set(de.getData().intValue()));
		}
	}


}
