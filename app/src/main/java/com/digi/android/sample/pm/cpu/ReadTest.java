package com.digi.android.sample.pm.cpu;

import com.digi.android.pm.cpu.exception.CPUException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by diescalo on 16/03/2016.
 */
public class ReadTest {

	// Constants.
	private static final int LAST_WORK_TIME_COLUMN_INDEX = 3;

	private static final String TAG = "CPUUtils";

	private static final String FILE_SYSTEM_STAT = "/proc/stat";

	private static final String PATTERN_END_LINE = "\n";

	/**
	 * Returns a list with all the CPU usage percentages (from CPU and cores).
	 * The CPU usage is located at index 0 of the list, followed by the core
	 * usages for the available number of cores of the device.
	 *
	 * <p>
	 *   <ul>
	 *     <li>Index 0: CPU usage</li>
	 *     <li>Index 1: Core 0 usage</li>
	 *     <li>Index 2: Core 1 usage</li>
	 *     <li>Index 3: Core 2 usage</li>
	 *     <li>Index 4: Core 3 usage</li>
	 *   </ul>
	 * </p>
	 *
	 * @param elapse The time in milliseconds between reads.
	 *
	 * @return The list with all the CPU usage percentages (from CPU and cores)
	 *         or {@code null} if the he values are not available.
	 *
	 * @throws CPUException If there is an error during the configuration
	 *                      process.
	 */
	public static ArrayList<Float> getUsage(long elapse) throws CPUException {
		ArrayList<Float> usages = new ArrayList<Float>();

		String stats1 = readFile("proc/stat");
		if (stats1 == null)
			return null;

		try {
			Thread.sleep(elapse);
		} catch (Exception e) { }

		String stats2 = readFile("proc/stat");
		if (stats2 == null)
			return null;

		String[] lines1 = stats1.split(PATTERN_END_LINE);
		String[] lines2 = stats2.split(PATTERN_END_LINE);

		int linesIndex = 0;
		for (int i = 0; i < 4 + 1; i++) {
			// If a core is disabled it won't be present in the stat file, so
			// it won't have a line to get its usage. CPU and core0 will always
			// have an entry (i = 0 and i = 1).
			//
			// Just make sure the core has a line, otherwise set 0 as usage.
			if (i < 2) {
				usages.add(getSystemCpuUsage(lines1[i].trim(), lines2[i].trim()));
				linesIndex += 1;
			} else if (linesIndex < lines1.length
					&& (lines1[linesIndex].startsWith("cpu" + (i-1)))) {
				usages.add(getSystemCpuUsage(lines1[linesIndex].trim(), lines2[linesIndex].trim()));
				linesIndex += 1;
			} else
				usages.add(0f);
		}

		return usages;
	}

	/**
	 * Computes and returns the total CPU/Core usage, in percent.
	 *
	 * @param start First read of /proc/stat.
	 * @param end Second read of /proc/stat.
	 *
	 * @return The total CPU/Core usage percentage or -1 if the value is not
	 *         available.
	 */
	public static float getSystemCpuUsage(String start, String end) {
		String[] stat = start.split(" ");
		long work1 = getSystemWorkTime(stat);
		long total1 = getSystemTotalTime(stat);

		stat = end.split(" ");
		long work2 = getSystemWorkTime(stat);
		long total2 = getSystemTotalTime(stat);

		float cpu = -1f;
		if (work1 >= 0 && total1 >= 0 && work2 >= 0 && total2 >= 0) {
			cpu = (float)(work2 - work1) / (float)(total2 - total1);
			cpu *= 100.0f;
		}
		return cpu;
	}

	/**
	 * Returns the sum of times read from /proc/stat.
	 *
	 * @param stat List of stats corresponding to a core or to the CPU.
	 *
	 * @return The total sum of times taken from the given stats.
	 */
	private static long getSystemTotalTime(String[] stat) {
		long l = 0L;

		for (int i = 1; i < stat.length; i++) {
			try {
				l += Long.parseLong(stat[i]);
			} catch (NumberFormatException ex) {
				return -1L;
			}
		}
		return l;
	}

	/**
	 * Returns the sum of work times read from /proc/stat.
	 *
	 * @param stat List of stats corresponding to a core or to the CPU.
	 *
	 * @return The sum of work times taken from the given stats.
	 */
	private static long getSystemWorkTime(String[] stat) {
		long l = 0L;

		for (int i = 1; i < (LAST_WORK_TIME_COLUMN_INDEX + 1); i++) {
			try {
				l += Long.parseLong(stat[i]);
			} catch (NumberFormatException ex) {
				return -1L;
			}
		}
		return l;
	}

	private static String readFile(String filePath) {
		// Verify file exists.
		File file = new File(filePath);
		if (!file.exists())
			return null;

		// Read the value from the file.
		BufferedReader reader = null;
		String line = null;
		String value = "";
		try {
			reader = new BufferedReader(new FileReader(file), 8);
			while ((line = reader.readLine()) != null)
				value += line + PATTERN_END_LINE;
			return value.trim();
		} catch (IOException e) {
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) { }
			}
		}
	}
}

