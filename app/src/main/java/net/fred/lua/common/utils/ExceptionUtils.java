package net.fred.lua.common.utils;
import java.io.StringWriter;
import java.io.PrintWriter;

public class ExceptionUtils {
    
    public static String getThrowableMessage(Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw);
		pw.close();
		return sw.toString();//StringWriter 可以不用close
	}
    
}
