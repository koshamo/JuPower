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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.koshamo.jupower.shared.Upower;

/**
 * @author jochen
 *
 */
public class UpowerConnector {

	private UpowerConnector() {
		// prevent instantiation 
	}
	
	
	public static String getVersion() {
		List<String> list = 
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
	
	public static List<String> getDevices() {
		return listInfos(Upower.UPOWER_CMD.getKey(), Upower.DEVICES_CMD.getKey());
	}
	
	public static void printInfo(String device) {
		List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), device);
		infos.forEach(System.out::println);
	}
	
	public static int getBatteryLoad(String battery) {
		List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), battery);
		Optional<String> opt = findValue(infos, Upower.BATTERY_LOAD_KEY.getKey());
		if (opt.isPresent())
			return Integer.valueOf(opt.get().substring(0, opt.get().length()-1)).intValue();
		return 0;
	}
	
	public static boolean isCharging(String battery) {
		List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), battery);
		Optional<String> opt = findValue(infos, Upower.CHARGING_KEY.getKey());
		if (opt.isPresent() && opt.get().equals(Upower.CHARGING_VALUE.getKey()))
			return true;
		return false;
	}

	public static boolean isSupplying(String linePower) {
		List<String> infos = 
				listInfos(Upower.UPOWER_CMD.getKey(), Upower.DETAILS_CMD.getKey(), linePower);
		Optional<String> opt = findValue(infos, Upower.SUPPLYING_KEY.getKey()); 
		if (opt.isPresent() && opt.get().equals(Upower.SUPPLYING_VALUE.getKey()))
			return true;
		else
			return false;
	}
	
	private static Optional<String> findValue(List<String> list, String key) {
		if (list.isEmpty())
			return Optional.empty();
		return list.stream()
				.filter(s -> s.contains(key))
				.flatMap(s -> Stream.of(s.split(Upower.SPLITTER.getKey())))
				.skip(1)
				.map(s -> s.trim())
				.findAny();
	}
	
	private static List<String> listInfos(String... cmdarray) {
		ProcessBuilder pb = new ProcessBuilder(cmdarray);
		List<String> info = new ArrayList<>();
		Process pro = null;
		try {
			pro = pb.start();
			pro.waitFor();
			BufferedReader bis = new BufferedReader(
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
