package net.fred.lua.io;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import net.fred.lua.common.Flag;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.PathConstants;
import android.util.Log;
import net.fred.lua.App;
import net.fred.lua.common.utils.ExceptionUtils;
import java.io.PrintWriter;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
* 该类用来扫描 Log 类输出的日志并存储到文件中
*/

public class LogScanner {
    
	private static LogScanner instance;
	private Flag flag;
	
	
	private LogScanner() {
		flag = new Flag(true);
	}
	
    public static LogScanner getInstance() {
		if (instance == null) {
			instance = new LogScanner();
		}
		return instance;
	}
	
	public void setFlag(boolean flag) {
		this.flag.setFlag(flag);
	}
	
	public boolean getFlag() {
		return this.flag.getFlag();
	}
	
	public void start() {
		new Thread(new DoScan()).start();
	}
	
	private class DoScan implements Runnable {

		@Override
		public void run() {
			PrintWriter outputStream = null;
			try {
				outputStream = new PrintWriter(PathConstants.LOG_FILE_PATH);
				while(flag.getFlag()) {
					Process process = new ProcessBuilder("logcat").redirectErrorStream(true).start();
					Process pro = new ProcessBuilder().command("logcat", "-c").redirectErrorStream(true).start();
					System.out.println("Log scanner started");
					InputStream is = process.getInputStream();
					int len = -1;
					byte[] buffer = new byte[1024];
					while(flag.getFlag() && (len = is.read(buffer)) > -1) {
						outputStream.print(new String(buffer, 0, len));
						outputStream.flush();
					}
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				Log.e("Scanner", ExceptionUtils.getThrowableMessage(e));
			} finally {
				outputStream.close();
			}
		}

	}
    
}
