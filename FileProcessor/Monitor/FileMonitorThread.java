package com.FileProcessor.Monitor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.FileProcessor.Executor.FileProcessorThread;


public class FileMonitorThread implements Runnable{
	private static String paths;
	
	public FileMonitorThread(String path) {
		
		this.paths = path;
	}
	
	
	@Override
	public void run() {
		WatchService watchService = null; 
		
		try {
			watchService = FileSystems.getDefault().newWatchService();
			Path path = Paths.get(paths);
			path.register( watchService, 
		            StandardWatchEventKinds.ENTRY_CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		WatchKey key;
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

		try {
			while ((key = watchService.take()) != null) {
				
				for (WatchEvent<?> event : key.pollEvents()) {
					System.out.println("Hello");
					//paths +=  event.context();
					FileProcessorThread entry = new FileProcessorThread((Path) event.context());
					executor.execute(entry);
				}
				
				key.reset();
				
				try {
					Thread.currentThread().sleep(60000);
				}
				catch(Exception e) {
					
				}
			}
		} catch (InterruptedException e) {

		}
		
	}

}
