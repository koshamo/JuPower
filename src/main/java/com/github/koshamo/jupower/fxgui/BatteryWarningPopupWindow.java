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


import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @author jochen
 *
 */
public class BatteryWarningPopupWindow extends Stage {

	public enum WarningType { EARLY, URGENT };
	
	private int batteryLoad;
	private WarningType type;
	
	public BatteryWarningPopupWindow() {
		this.setAlwaysOnTop(true);
		this.initModality(Modality.NONE);
		this.initStyle(StageStyle.UNDECORATED);
	}
	
	private void buildContent() {
		StackPane pane = new StackPane();
		pane.setAlignment(Pos.CENTER);
		Rectangle2D screen = Screen.getPrimary().getVisualBounds();
		double maxWidth = screen.getWidth() * 0.7;
		double maxHeight = screen.getHeight() * 0.2;
		Rectangle border;
		if (type == WarningType.EARLY)
			border = new Rectangle(maxWidth, maxHeight, Color.ORANGE);
		else
			border = new Rectangle(maxWidth, maxHeight, Color.RED);
		Rectangle inlay = new Rectangle(maxWidth - 20, maxHeight - 20, Color.WHITE);
		String text = "Battery Power is low: " + batteryLoad + "%";
		Label lblWarning = new Label(text);
		lblWarning.setFont(new Font(maxHeight * 0.2));
		pane.getChildren().addAll(border, inlay, lblWarning);
		
		FadeTransition ft = new FadeTransition(Duration.millis(5000), pane);
		ft.setFromValue(1.0);
		ft.setToValue(0.2);
		ft.play();
		ft.setOnFinished(p -> hide());

		Scene scene = new Scene(pane, maxWidth, maxHeight);
		this.setScene(scene);
	}
	
	public void show(int batteryLoad, WarningType type) {
		this.batteryLoad = batteryLoad;
		this.type = type;
		buildContent();
		show();
	}
	
	
}
