package net.fred.lua.io;

import android.content.Context;
import android.util.Log;

import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;

public final class LogFileManager {
    private static final String TAG = "CacheDirectoryManager";

    private static volatile LogFileManager instance;
    private final File mSaveDir;
    private final CountDownLatch compressFinished;

    private LogFileManager(Context ctx) {
        this.mSaveDir = ctx.getExternalFilesDir("log");
        this.compressFinished = new CountDownLatch(1);
        net.fred.lua.common.utils.FileUtils.makeDirs(getNativeCrashDirectory().toString());
    }

    public static LogFileManager getInstance() {
        if (instance == null) {
            synchronized (LogFileManager.class) {
                if (instance == null) {
                    throw new RuntimeException("Not installed");
                }
            }
        }
        return instance;
    }

    public static void install(Context ctx) {
        if (instance != null) {
            Log.e(TAG, "Already installed.");
            return;
        }
        synchronized (LogFileManager.class) {
            if (instance == null) {
                instance = new LogFileManager(ctx);
            } else {
                Log.e(TAG, "Already installed.");
            }
        }
    }

    public File getLogScannerFile() {
        return new File(mSaveDir, "latest-scanner.log");
    }

    public File getCrashFile() {
        return new File(mSaveDir, "latest-crash.log");
    }

    public File getLogZipDirectory() {
        return new File(mSaveDir, "logs");
    }

    public File getLogErrorZipDirectory() {
        return new File(mSaveDir, "errors");
    }

    public File getNativeCrashDirectory() {
        return new File(mSaveDir, "nativeCrash");
    }

    public long sizeOfDirectory() {
        return FileUtils.sizeOfDirectory(mSaveDir);
    }

    public String sizeOfDirectoryString() {
        return FileUtils.byteCountToDisplaySize(sizeOfDirectory());
    }

    public void delete() {
        net.fred.lua.common.utils.FileUtils.deleteDirectory(mSaveDir);
        net.fred.lua.common.utils.FileUtils.makeDirs(getNativeCrashDirectory());
    }

    public void compressLatestLogs() {
        boolean hasError;
        ArrayList<File> compressFile = new ArrayList<>(3);

        hasError = addIfExists(compressFile, getCrashFile());
        addIfExists(compressFile, getLogScannerFile());

        //native crash part
        {
            Log.i(TAG, "Getting native crash dump");
            File latest = new File(getNativeCrashDirectory(), "latest");
            if (latest.exists() && latest.length() != 0) {
                try {
                    String realDumpPath = FileUtils.readFileToString(latest, "UTF-8");
                    Log.i(TAG, "Latest dump path " + realDumpPath);
                    hasError |= addIfExists(compressFile, new File(realDumpPath));
                    FileUtils.forceDelete(latest);
                } catch (IOException e) {
                    Log.e(TAG, "IOException: " + e.getMessage());
                }
            }
        }

        if (compressFile.size() == 0) {
            compressFinished.countDown();
            return;
        }

        for (int i = 0; i < compressFile.size(); ++i) {
            compressFile.set(i, renameFileAccordingDate(
                    compressFile.get(i)
            ));
        }

        File logZipDirectory = hasError ? getLogErrorZipDirectory() : getLogZipDirectory();
        net.fred.lua.common.utils.FileUtils.makeDirs(logZipDirectory.getAbsolutePath());

        // Now compress them into a zip file
        ZipArchiveOutputStream zos = null;
        try {
            zos = new ZipArchiveOutputStream(new File(logZipDirectory,
                    DateUtils.formatDate(compressFile.get(0).lastModified()) + ".zip"));
            zos.setLevel(8);
            zos.setMethod(ZipEntry.DEFLATED);

            for (File curr : compressFile) {
                ZipArchiveEntry zae = new ZipArchiveEntry(curr.getName());
                zos.putArchiveEntry(zae);
                zos.write(FileUtils.readFileToByteArray(curr));
                zos.closeArchiveEntry();
                // Remove saved file.
                FileUtils.forceDelete(curr);
            }
        } catch (IOException e) {
            if (zos != null) {
                try {
                    zos.closeArchiveEntry();// protect zip file
                } catch (IOException ignored) {
                }
            }
            if (e.getMessage() != null) {
                Log.e(TAG, e.getMessage());
            }
        } finally {
            ThrowableUtils.closeAll(zos);
            compressFinished.countDown();
        }
    }

    /**
     * Blocking current thread, waiting for compression to complete.
     */
    public void waitForCompressionFinish() throws InterruptedException {
        compressFinished.await();
    }

    private File renameFileAccordingDate(File file) {
        String name = DateUtils.formatDate(file.lastModified());

        if (file.getName().contains("crash")) {
            name += "-crash";
        } else if (file.getName().contains("scanner")) {
            name += "-scanner";
        }

        File dest = new File(file.getParent(), name + "." +
                StringUtils.getSuffix(file.getName()));
        Log.i(TAG, "rename " + file + " to " + dest);
        if (!file.renameTo(dest)) {
            Log.e(TAG, "Failed to rename " + file + " to " + dest);
        }
        return dest;
    }

    private boolean addIfExists(ArrayList<File> dest, File obj) {
        if (obj.exists()) {
            dest.add(obj);
            Log.i(TAG, obj.toString());
            return true;
        }
        return false;
    }
}
