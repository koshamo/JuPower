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
 * This enum describes the keys for Events sent by the Upower connector
 * 
 * @author jochen
 *
 */
public enum EventKeys {
	BATTERY ("Battery"), //$NON-NLS-1$
	CHARGING ("Charging"), //$NON-NLS-1$
	SUPPLYING ("Supplying"); //$NON-NLS-1$
	
	private final String key;
	
	EventKeys (String key) {
		this.key = key;
	}
	
	/**
	 * access to the key
	 * @return	the key as String
	 */
	public String getKey() {
		return key;
	}
}
