import com.github.koshamo.fiddler.MessageBus;
import com.github.koshamo.fiddler.jfx.FiddlerFxApp;
import com.github.koshamo.fiddler.jfx.FiddlerFxAppRunner;
import com.github.koshamo.jupower.fxgui.JuPowerGui;
import com.github.koshamo.jupower.systembus.UpowerModule;

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

/**
 * @author jochen
 *
 */
public class JuPower {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MessageBus messageBus = new MessageBus();
		new Thread(new FiddlerFxAppRunner(JuPowerGui.class, args)).start();
		FiddlerFxApp.setMessageBus(messageBus);
		new UpowerModule(messageBus);
	}

}
