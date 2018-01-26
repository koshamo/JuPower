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

import java.util.Objects;

import com.github.koshamo.fiddler.DataEvent;
import com.github.koshamo.fiddler.EventHandler;

/**
 * This class provides a concrete DataEvent based on a String key and a 
 * Boolean value, such as to describe, if the battery is charging etc.
 * 
 * @author jochen
 *
 */
public class StrBoolDataEvent extends DataEvent<String, Boolean> {

	/**
	 * constructor inherited from super class to consrtuct a data event
	 * 
	 * @param source	the sender of this event
	 * @param target	the target of this event, may be null
	 * @param meta		the the meta-data description, which gives information 
	 * about the delivered data 
	 * @param data		the actual data as Boolean value
	 */
	public StrBoolDataEvent(EventHandler source, EventHandler target, String meta, Boolean data) {
		super(source, target, meta, data);
	}

}
