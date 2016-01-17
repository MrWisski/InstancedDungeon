package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.time.DurationFormatUtils;

//Please only put server-agnostic functions here.
public class Util {
	
	/** Convert MS to human readable time display.
	 * 
	 * @param	timein	(long) A duration of time in Milliseconds.  
	 * @return	A string in the format of "Years:Months:Days:Hours:Minutes:Seconds"
	 * 
	 */
	public static String toHMSFormat(long timein){
		String in = DurationFormatUtils.formatDuration(timein,"y:M:d:H:m:s");
	
		String out = "";
		String[] sp = in.split(":");
		String[] t = {"Y","M","D","h","m","s"};
		
		for(int i = 0;i < sp.length;i++)
			if(Long.parseLong(sp[i]) != 0)
				out += sp[i] + t[i];
		
		if(out == "")
			out = "0s";
		return out;
	}
	
	/**Helper function to return a string from an InputStream.
	 * 
	 *  @param InputStream in - Input stream to convert to String
	 *  @return String - the contents of the input stream, as a String
	 */
	public static String getStringFromStream(InputStream in){
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        try {
			while ((line = reader.readLine()) != null) {
				
			    out.append(line + "\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
   
        return out.toString();
		
	}
}
