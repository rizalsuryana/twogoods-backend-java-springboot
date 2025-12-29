package com.finpro.twogoods.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class EnvLoader {
	public static void load(String path) {
		File file = new File(path);
		if (!file.exists()) return;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				String[] parts = line.split("=", 2);
				if (parts.length == 2) {
					System.setProperty(parts[0], parts[1]);
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to load .env file: " + e.getMessage());
		}
	}
}