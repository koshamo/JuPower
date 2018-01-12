/*
 * Copyright [2017] [Dr. Jochen Raﬂler]
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
package com.github.koshamo.jupower.shared;

/**
 * @author jochen
 *
 */
public enum Upower {
	UPOWER_CMD ("upower"),
	VERSION_CMD ("-v"),
	VERSION_KEY ("client"),
	DEVICES_CMD ("-e"),
	DETAILS_CMD ("-i"),
	BATTERY_LOAD_KEY ("percentage"),
	CHARGING_KEY ("state"),
	CHARGING_VALUE ("charging"),
	SUPPLYING_KEY ("online"),
	SUPPLYING_VALUE ("yes"),
	SPLITTER (":"),
	BATTERY ("battery"),
	LINE_POWER ("line_power");
	
	private final String key;
	
	Upower(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
