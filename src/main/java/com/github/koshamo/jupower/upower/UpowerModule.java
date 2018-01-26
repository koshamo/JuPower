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
package com.github.koshamo.jupower.upower;

import java.util.List;
import java.util.Objects;

import com.github.koshamo.fiddler.Event;
import com.github.koshamo.fiddler.EventHandler;
import com.github.koshamo.fiddler.ExitEvent;
import com.github.koshamo.fiddler.MessageBus;
import com.github.koshamo.fiddler.MessageBus.ListenerType;
import com.github.koshamo.jupower.shared.EventKeys;
import com.github.koshamo.jupower.shared.StrBoolDataEvent;
import com.github.koshamo.jupower.shared.StrIntDataEvent;
import com.github.koshamo.jupower.shared.Upower;

/**
 * The class is the actual Upower Module, conncted to the message bus.
 * This class comminicates to other system modules via the system bus and
 * uses the util classes as backend.
 * 
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
	private ChargingAndSupplyChecker chargingChecker;
	List<String> devices;
	
	/**
	 * To instantiate this class, a valid message bus object is required to
	 * insecure, a communication to the bus is possible
	 * 
	 * @param messageBus	the message bus
	 */
	public UpowerModule(final MessageBus messageBus) {
		this.messageBus = Objects.requireNonNull(messageBus);
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
		chargingChecker = new ChargingAndSupplyChecker();
		new Thread(chargingChecker).start();
	}
	
	/* (non-Javadoc)
	 * this method handles incoming signal
	 * 
	 * @see com.github.koshamo.fiddler.EventHandler#handle(com.github.koshamo.fiddler.Event)
	 */
	@Override
	public void handle(final Event event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * 
	 * this method handles the shutdown signal and thus insecures a 
	 * save system exit
	 *  
	 * @see com.github.koshamo.fiddler.EventHandler#shutdown()
	 */
	@Override
	public void shutdown() {
		deviceChecker.stop();
		batteryChecker.stop();
		chargingChecker.stop();
		messageBus.unregisterRequestEvents(this);
	}

	/**
	 * The runnable class Device Checker is to be used in a seperate thread
	 * to poll the system for devices attached / detached
	 * 
	 * @author jochen
	 *
	 */
	private class DeviceChecker implements Runnable {

		private boolean run = true;
		
		/**
		 *	currently nothing required for the constructor 
		 */
		public DeviceChecker() {
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * 
		 * polling the connected devices
		 * 
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
		
		/**
		 * stop this thread
		 */
		public void stop() {
			run = false;
		}
		
	}

	/**
	 * The runnable class Battery Checker is to be used in a seperate thread
	 * to poll the battery for its load state
	 * 
	 * @author jochen
	 *
	 */
	private class BatteryChecker implements Runnable {

		private boolean run = true;
		/**
		 *	currently nothing required for the constructor 
		 */
		public BatteryChecker() {
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * 
		 * polling the battery load value
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (run) {
				devices.stream()
					.filter(d -> d.contains(Upower.BATTERY.getKey()))
					.forEach(this::checkBattery);
				try {
					Thread.sleep(BATTERY_CHECK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * helper method to get the battery value
		 * 
		 * @param device
		 */
		private void checkBattery(final String device) {
			int load = UpowerConnector.getBatteryLoad(device);
			messageBus.postEvent(
					new StrIntDataEvent(
							UpowerModule.this, null, EventKeys.BATTERY.getKey(), Integer.valueOf(load)));
		}
		
		/**
		 * stop this thread
		 */
		public void stop() {
			run = false;
		}
		
	}

	/**
	 * The runnable class ChargingAndSupplyChecker is to be used in a seperate 
	 * thread to poll the battery for its charging state and the system for
	 * power supply state
	 * 
	 * @author jochen
	 *
	 */
	private class ChargingAndSupplyChecker implements Runnable {

		private boolean run = true;
		/**
		 *	currently nothing required for the constructor 
		 */
		public ChargingAndSupplyChecker() {
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * 
		 * polling for charging and power supply
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (run) {
				devices.stream()
					.filter(d -> d.contains(Upower.LINE_POWER.getKey()))
					.forEach(this::checkSupplying);
				devices.stream()
				.filter(d -> d.contains(Upower.BATTERY.getKey()))
				.forEach(this::checkCharging);
				try {
					Thread.sleep(CHARGE_CHECK);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * helper method to get the supplying value
		 * 
		 * @param device	the power supply device
		 */
		private void checkSupplying(final String device) {
			boolean supplying = UpowerConnector.isSupplying(device);
			messageBus.postEvent(
					new StrBoolDataEvent(
							UpowerModule.this, null, EventKeys.SUPPLYING.getKey(), Boolean.valueOf(supplying)));
		}

		/**
		 * helper method the get the charging value
		 * 
		 * @param device	the battery device
		 */
		private void checkCharging(final String device) {
			boolean charging = UpowerConnector.isCharging(device);
			messageBus.postEvent(
					new StrBoolDataEvent(
							UpowerModule.this, null, EventKeys.CHARGING.getKey(), Boolean.valueOf(charging)));
		}

		/**
		 * stop this thread
		 */
		public void stop() {
			run = false;
		}
		
	}

}
