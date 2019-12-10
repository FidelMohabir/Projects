package com.FileProcessor.Main;

import java.util.Scanner;

import com.FileProcessor.Monitor.FileMonitorThread;

public class Main {

		public static void main(String[] args) {
			Scanner scanner = new Scanner(System.in);
			try {
				System.out.println("Type in source path for folder being monitored");
				String path = scanner.nextLine();
				FileMonitorThread monitor = new FileMonitorThread(path);
				Thread t = new Thread(monitor);
				t.start();
				scanner.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
}
