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
package com.github.koshamo.jupower.shared;

/**
 * This enum provides keys to control the upower tool.
 * First, the actual upower comand  is given
 * Second, the possible parameters to get system information is given
 * Third, parsing strings to get the information from the return values are given
 * 
 *  currently used upower version: 0.99.4
 *  these options may be changed with a different version!
 *  
 * @author jochen
 *
 */
public enum Upower {
	UPOWER_CMD ("upower"), //$NON-NLS-1$
	VERSION_CMD ("-v"), //$NON-NLS-1$
	VERSION_KEY ("client"), //$NON-NLS-1$
	DEVICES_CMD ("-e"), //$NON-NLS-1$
	DETAILS_CMD ("-i"), //$NON-NLS-1$
	BATTERY_LOAD_KEY ("percentage"), //$NON-NLS-1$
	CHARGING_KEY ("state"), //$NON-NLS-1$
	CHARGING_VALUE ("charging"), //$NON-NLS-1$
	SUPPLYING_KEY ("online"), //$NON-NLS-1$
	SUPPLYING_VALUE ("yes"), //$NON-NLS-1$
	SPLITTER (":"), //$NON-NLS-1$
	BATTERY ("battery"), //$NON-NLS-1$
	LINE_POWER ("line_power"); //$NON-NLS-1$
	
	private final String key;
	
	Upower(String key) {
		this.key = key;
	}
	
	/**
	 * get the key in its String representation
	 * @return	the key as String
	 */
	public String getKey() {
		return key;
	}
}
