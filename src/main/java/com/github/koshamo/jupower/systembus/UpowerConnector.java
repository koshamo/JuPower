/*
 * Copyright [2017] [Dr. Jochen Ra�ler]
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
import java.util.stream.Stream;

/**
 * @author jochen
 *
 */
public class UpowerConnector {

	private UpowerConnector() {
		// prevent instantiation 
	}
	
	public static String getVersion() {
		List<String> list = listInfos("upower", "-v");
		int start = 0;
		for (String line : list) {
			if (line.contains("client")) {
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
		return listInfos("upower", "-e");
	}
	
	public static void printInfo(String device) {
		List<String> infos = listInfos("upower", "-i", device);
		infos.forEach(System.out::println);
	}
	
	public static int getBatteryLoad(String battery) {
		List<String> infos = listInfos("upower", "-i", battery);
		String value = findValue(infos, "percentage");
		
		return Integer.valueOf(value.substring(0, value.length()-1)).intValue();
	}
	
	public static boolean isCharging(String linePower) {
		List<String> infos = listInfos("upower", "-i", linePower);
		String value = findValue(infos, "online");
		if (value.equals("yes"))
			return true;
		else
			return false;
	}
	
	private static String findValue(List<String> list, String key) {
		return list.stream()
				.filter(s -> s.contains(key))
				.flatMap(s -> Stream.of(s.split(":")))
				.skip(1)
				.map(s -> s.trim())
				.findAny()
				.get();

	}
	
	private static List<String> listInfos(String... cmdarray) {
		ProcessBuilder pb = new ProcessBuilder(cmdarray);
		List<String> info = new ArrayList<>();
		Process pro = null;
		try {
			pro = pb.start();
			pro.waitFor();
			BufferedReader bis = new BufferedReader(new InputStreamReader(pro.getInputStream()));
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