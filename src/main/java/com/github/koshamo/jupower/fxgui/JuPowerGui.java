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

import com.github.koshamo.fiddler.DataEvent;
import com.github.koshamo.fiddler.Event;
import com.github.koshamo.fiddler.MessageBus.ListenerType;
import com.github.koshamo.fiddler.jfx.FiddlerFxApp;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author jochen
 *
 */
public class JuPowerGui extends FiddlerFxApp {

	private Stage primaryStage;
	private SystemTrayIntegration systemTray;
	private int batteryLoad;
	private boolean charging;
	

	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		VBox vbox = new VBox();
		
		javax.swing.SwingUtilities.invokeLater(
				() -> { systemTray = new SystemTrayIntegration(primaryStage, this, getMessageBus()); 
				});
		
		primaryStage.setScene(new Scene(vbox, 200, 100));
		primaryStage.setTitle("JuPower");
//		primaryStage.show();
		// prevent application to be closed, when last window is closed
		Platform.setImplicitExit(false);
		getMessageBus().registerAllEvents(this, ListenerType.TARGET);
	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#shutdown()
	 */
	@Override
	public void shutdown() {
		getMessageBus().unregisterAllEvents(this);
		Platform.exit();
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#handle(com.github.koshamo.fiddler.Event)
	 */
	@Override
	public void handle(Event event) {
		if (event instanceof DataEvent) {
			DataEvent<String,?> de = (DataEvent<String,?>) event;
			if (de.getMetaInformation().equals("Battery")) {
				DataEvent<String, Integer> deBat = 
						(DataEvent<String, Integer>) event;
				batteryLoad = deBat.getData().intValue();
				systemTray.updateIcon(batteryLoad, charging);
			}
			if (de.getMetaInformation().equals("Charging")) {
				DataEvent<String, Boolean> deBat = 
						(DataEvent<String, Boolean>) event;
				charging = deBat.getData().booleanValue();
				systemTray.updateIcon(batteryLoad, charging);
			}
		
		}
	}


}
