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

import com.github.koshamo.fiddler.DataEvent;
import com.github.koshamo.fiddler.EventHandler;

/**
 * This class provides a concrete DataEvent based on a String key and a 
 * Integer value, such as to describethe battery load, etc.
 * 
 * @author jochen
 *
 */
public class StrIntDataEvent extends DataEvent<String, Integer> {

	/**
	 * constructor inherited from super class to consrtuct a data event
	 * 
	 * @param source	the sender of this event
	 * @param target	the target of this event, may be null
	 * @param meta		the the meta-data description, which gives information 
	 * about the delivered data 
	 * @param data		the actual data as Integer value
	 */
	public StrIntDataEvent(EventHandler source, EventHandler target, String meta, Integer data) {
		super(source, target, meta, data);
		// TODO Auto-generated constructor stub
	}

}
