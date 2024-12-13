package byzzbench.simulator.protocols.hbft.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ScheduleLogger {
    private final String LOG_FOLDER_PATH = "../schedule-logs";
    private int currentRunNumber = 1;
    private File logFile = null;

    // Clears and/or creates a folder every 
    public void initialize() {
        File folder = new File(LOG_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdirs(); // Ensure the folder exists
        }

        File logFile;
        do {
            logFile = new File(folder, "scenario-" + currentRunNumber + ".txt");
            this.logFile = logFile;
            currentRunNumber++;
        } while (logFile.exists());
    }

    // Write a log to a new file
    public void writeLog(String logContent) {
        if (logFile == null) {
            throw new IllegalStateException("Log file not initialized");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logContent + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}