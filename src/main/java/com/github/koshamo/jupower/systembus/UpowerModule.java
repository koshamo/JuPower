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
package com.github.koshamo.jupower.systembus;

import java.util.List;

import com.github.koshamo.fiddler.DataEvent;
import com.github.koshamo.fiddler.Event;
import com.github.koshamo.fiddler.EventHandler;
import com.github.koshamo.fiddler.ExitEvent;
import com.github.koshamo.fiddler.MessageBus;
import com.github.koshamo.fiddler.MessageBus.ListenerType;

/**
 * @author jochen
 *
 */
public class UpowerModule implements EventHandler {

	final MessageBus messageBus;
	private final int DEVICE_CHECK = 60 * 1000;
	private final int BATTERY_CHECK = 10 * 1000;
	private final int CHARGE_CHECK = 5 * 1000;
	
	private DeviceChecker deviceChecker;
	private BatteryChecker batteryChecker;
	private ChargingChecker chargingChecker;
	List<String> devices;
	
	public UpowerModule(MessageBus messageBus) {
		this.messageBus = messageBus;
		// check if Upower is available
		if (UpowerConnector.getVersion() == null) {
			System.out.println("Upower not available. Shutting down");
			messageBus.postEvent(new ExitEvent(this, null));
		}
		// register to message bus
		messageBus.registerRequestEvents(this, ListenerType.TARGET);
		devices = UpowerConnector.getDevices();
		deviceChecker = new DeviceChecker();
		new Thread(deviceChecker).start();
		batteryChecker = new BatteryChecker();
		new Thread(batteryChecker).start();
		chargingChecker = new ChargingChecker();
		new Thread(chargingChecker).start();
	}
	
	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#handle(com.github.koshamo.fiddler.Event)
	 */
	@Override
	public void handle(Event event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.github.koshamo.fiddler.EventHandler#shutdown()
	 */
	@Override
	public void shutdown() {
		deviceChecker.stop();
		batteryChecker.stop();
		chargingChecker.stop();
		messageBus.unregisterRequestEvents(this);
	}

	private class DeviceChecker implements Runnable {

		private boolean run = true;
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (run) {
				devices = UpowerConnector.getDevices();
				try {
					Thread.sleep(DEVICE_CHECK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void stop() {
			run = false;
		}
		
	}

	private class BatteryChecker implements Runnable {

		private boolean run = true;
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (run) {
				devices.stream()
					.filter(d -> d.contains(UpowerConnector.BATTERY))
					.forEach(this::checkBattery);
				try {
					Thread.sleep(BATTERY_CHECK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private void checkBattery(String device) {
			int load = UpowerConnector.getBatteryLoad(device);
			messageBus.postEvent(
					new DataEvent<String, Integer>(
							UpowerModule.this, null, "Battery", Integer.valueOf(load)));
		}
		
		public void stop() {
			run = false;
		}
		
	}

	private class ChargingChecker implements Runnable {

		private boolean run = true;
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (run) {
				devices.stream()
					.filter(d -> d.contains(UpowerConnector.LINE_POWER))
					.forEach(this::checkSupplying);
				devices.stream()
				.filter(d -> d.contains(UpowerConnector.BATTERY))
				.forEach(this::checkCharging);
				try {
					Thread.sleep(CHARGE_CHECK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private void checkSupplying(String device) {
			boolean supplying = UpowerConnector.isSupplying(device);
			messageBus.postEvent(
					new DataEvent<String, Boolean>(
							UpowerModule.this, null, "Supplying", Boolean.valueOf(supplying)));
		}

		private void checkCharging(String device) {
			boolean charging = UpowerConnector.isCharging(device);
			messageBus.postEvent(
					new DataEvent<String, Boolean>(
							UpowerModule.this, null, "Charging", Boolean.valueOf(charging)));
		}

		public void stop() {
			run = false;
		}
		
	}

}
