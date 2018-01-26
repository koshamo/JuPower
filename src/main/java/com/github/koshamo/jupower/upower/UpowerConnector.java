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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.koshamo.jupower.shared.Upower;

/**
 * This class is a pure utility class designed to interact with the upower
 * console tool to parse system attributes, such as current battery load
 * 
 * @author jochen
 *
 */
public class UpowerConnector {

	/**
	 * private constructor, to prevent users to instantiate this class
	 */
	private UpowerConnector() {
		// prevent instantiation 
	}
	
	
	/**
	 * parses the current version of upower.
	 * This method can be used to check whether upower is available
	 * 
	 * @return	upower version as String
	 */
	public static String getVersion() {
		final List<String> list = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.VERSION_CMD.getKey());
		int start = 0;
		for (String line : list) {
			if (line.contains(Upower.VERSION_KEY.getKey())) {
				for (int i = 0; i < line.length(); ++i) {
					if (Character.isDigit(line.charAt(i))) {
						start = i;
						break;
					}
				}
			}
			return line.substring(start);
		}
		return null;
	}
	
	/**
	 * upower works on devices, such as batters, line power etc. This method
	 * uses upower to return a list of detected devices to use for other 
	 * methods as input.
	 * 
	 * @return	the list of available upower devices
	 */
	public static List<String> getDevices() {
		return listInfos(Upower.UPOWER_CMD.getKey(), Upower.DEVICES_CMD.getKey());
	}
	
	/**
	 * printInfo is a debugging method to print all available information
	 * for a given device. Notice: this methods prints to standard output
	 * 
	 * @param device	the device of interest
	 */
	public static void printInfo(final String device) {
		final List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), device);
		infos.forEach(System.out::println);
	}
	
	/**
	 * This method parses the current battery load of a given battery device.
	 * If the device is not found, this method returns 0!
	 * 
	 * @param battery	the battery device of interest
	 * @return			the battery load as integer
	 */
	public static int getBatteryLoad(final String battery) {
		final List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), battery);
		final Optional<String> opt = findValue(infos, Upower.BATTERY_LOAD_KEY.getKey());
		if (opt.isPresent())
			return Integer.valueOf(opt.get().substring(0, opt.get().length()-1)).intValue();
		return 0;
	}
	
	/**
	 * This method parses the charging value for the given battery device
	 * If the device is not found, this method returns false!
	 * 
	 * @param battery	the battery device of interest
	 * @return			true, if the batters is Charging
	 */
	public static boolean isCharging(final String battery) {
		final List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), battery);
		final Optional<String> opt = findValue(infos, Upower.CHARGING_KEY.getKey());
		if (opt.isPresent() && opt.get().equals(Upower.CHARGING_VALUE.getKey()))
			return true;
		return false;
	}

	/**
	 * This method parses the supplying value for the given device.
	 * If the device is not found, this method returns false!
	 * 
	 * @param linePower	the line power device of interest (mostly only on available)
	 * @return			true, if line power supply is available
	 */
	public static boolean isSupplying(final String linePower) {
		final List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), linePower);
		final Optional<String> opt = findValue(infos, Upower.SUPPLYING_KEY.getKey()); 
		if (opt.isPresent() && opt.get().equals(Upower.SUPPLYING_VALUE.getKey()))
			return true;
		else
			return false;
	}
	
	/**
	 * helper method for the parsing methods: 
	 * the device information is a list in form of key: value
	 * this method checks, if the given key is in the device information, 
	 * which has to be provided as a list. If the key has been found, this
	 * method returns the value.
	 * This method uses Optional as return type to be null save 
	 * 
	 * @param list	the device information string as list
	 * @param key	the key of interest
	 * @return		the value of the provided key, if found, otherwise 
	 * Optional.empty
	 */
	private static Optional<String> findValue(
			final List<String> list, final String key) {
		if (list.isEmpty())
			return Optional.empty();
		return list.stream()
				.filter(s -> s.contains(key))
				.flatMap(s -> Stream.of(s.split(Upower.SPLITTER.getKey())))
				.skip(1)
				.map(s -> s.trim())
				.findAny();
	}
	
	/**
	 * this method interacts with the upower console tool.
	 * The input is the upower command with parameters, the return value is
	 * the return value of the upower tool as a list of string, containing all
	 * lines of the uupower return value.
	 * 
	 * @param cmdarray	upower command and all arguments
	 * @return			the upower return values as list of string (all clines)
	 */
	private static List<String> listInfos(final String... cmdarray) {
		ProcessBuilder pb = new ProcessBuilder(cmdarray);
		final List<String> info = new ArrayList<>();
		try {
			final Process pro = pb.start();
			pro.waitFor();
			final BufferedReader bis = new BufferedReader(
					new InputStreamReader(pro.getInputStream()));
			String line;
			while ((line = bis.readLine()) != null)
				info.add(line);
			pro.destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info;
	}

}
