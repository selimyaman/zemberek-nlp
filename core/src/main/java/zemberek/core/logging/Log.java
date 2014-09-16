package zemberek.core.logging;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import zemberek.core.io.Strings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.logging.*;

/**
 * A convenient Log class.
 */
public final class Log {

    public static final Thread.UncaughtExceptionHandler EXCEPTION_HANDLER = new ExceptionLoggerHandler();

    static Level currentLevel = Level.INFO;

    private static final Logger logger = Logger.getLogger("zemberek-logger");

    static {
        logger.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new CUSTOM_FORMAT());
        ch.setLevel(currentLevel);
        logger.addHandler(ch);
        Thread.setDefaultUncaughtExceptionHandler(EXCEPTION_HANDLER);
    }

    private Log() {
    }

    private static class ExceptionLoggerHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.error("Exception occurred in thread :" + t.getName());
            Log.error(e.toString());
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                Log.error("  " + stackTraceElement.toString());
            }
            e.printStackTrace();
        }
    }

    private static void setLevel(Level level) {
        logger.setLevel(level);
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(level);
        }
        currentLevel = level;
    }

    /**
     * @return true if Log is in debug level.
     */
    public static boolean isDebug() {
        return checkLevel(Level.FINE);
    }

    /**
     * @return true if Log is in trace level.
     */
    public static boolean isTrace() {
        return checkLevel(Level.FINER);
    }

    /**
     * @return true if Log is in info level.
     */
    public static boolean isInfo() {
        return checkLevel(Level.INFO);
    }

    /**
     * @return true if Log is in warn level.
     */
    public static boolean isWarn() {
        return checkLevel(Level.WARNING);
    }

    private static boolean checkLevel(Level level) {
        return currentLevel.intValue() <= level.intValue();
    }

    /**
     * Trace level logging. This can be used for cases finer grained than debug level.
     *
     * @param message message to log. it can be in String.format(message) format.
     * @param params  if message is formatted according to String.format() params should contain necessary arguments.
     *                If last parameter is a throwable, it is handled specially.
     */
    public static void trace(String message, Object... params) {
        log(Level.FINER, message, params);
    }

    public static void trace(boolean condition, String message, Object... params) {
        if (condition)
            log(Level.FINER, message, params);
    }

    public static void trace(Object object) {
        log(Level.FINER, object.toString());
    }

    public static void debug(String message, Object... params) {
        log(Level.FINE, message, params);
    }

    public static void debug(boolean condition, String message, Object... params) {
        if (condition)
            log(Level.FINE, message, params);
    }

    public static void debug(Object object) {
        log(Level.FINE, object.toString());
    }

    public static void info(String message, Object... params) {
        log(Level.INFO, message, params);
    }

    public static void info(boolean condition, String message, Object... params) {
        if (condition)
            log(Level.INFO, message, params);
    }

    public static void info(Object object) {
        log(Level.INFO, object.toString());
    }

    public static void warn(String message, Object... params) {
        log(Level.WARNING, message, params);
    }

    public static void warn(boolean condition, String message, Object... params) {
        if (condition)
            log(Level.WARNING, message, params);
    }

    public static void warn(Object object) {
        log(Level.WARNING, object.toString());
    }

    public static void error(String message, Object... params) {
        log(Level.SEVERE, message, params);
    }

    public static void error(boolean condition, String message, Object... params) {
        if (condition)
            log(Level.SEVERE, message, params);
    }

    public static void error(Object object) {
        log(Level.SEVERE, object.toString());
    }

    public static void setWarn() {
        setLevel(Level.WARNING);
    }

    public static void setInfo() {
        setLevel(Level.INFO);
    }

    public static void setDebug() {
        setLevel(Level.FINE);
    }

    public static void setTrace() {
        setLevel(Level.FINER);
    }

    static final CUSTOM_FORMAT formatter = new CUSTOM_FORMAT();

    public static OutputStream fileStream;

    public static void addFileHandler(Path path) throws IOException {
        fileStream = Files.newOutputStream(path);
        final StreamHandler handler = new StreamHandler(fileStream, formatter);
        handler.setLevel(currentLevel);
        Logger.getLogger("").addHandler(handler);
    }

    public static void log(Level level, String message, Object... params) {
        final int stackPositionOfCaller = 2;
        StackTraceElement caller = new Throwable().getStackTrace()[stackPositionOfCaller];
        String className = caller.getClassName();

        if (logger.isLoggable(level)) {
            String formattedMessage;
            Throwable thrown = null;
            if (params.length == 0) {
                formattedMessage = message;
            } else {
                Object last = params[params.length - 1];
                if (last instanceof Throwable) {
                    Object[] subParams = new Object[params.length - 1];
                    System.arraycopy(params, 0, subParams, 0, subParams.length);
                    formattedMessage = String.format(message, subParams);
                    thrown = (Throwable) last;
                } else {
                    formattedMessage = String.format(message, params);
                }
            }
            LogRecord record = new LogRecord(level, formattedMessage);
            record.setLoggerName(logger.getName());
            record.setSourceClassName(className);
            record.setSourceMethodName(caller.getMethodName());
            record.setThrown(thrown);
            record.setParameters(params);
            logger.log(record);
        }
    }

    static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    static Map<Level, String> levelShortStringMap = new HashMap<>();

    static {
        levelShortStringMap.put(Level.FINEST, "-");
        levelShortStringMap.put(Level.FINER, "T");
        levelShortStringMap.put(Level.FINE, "D");
        levelShortStringMap.put(Level.CONFIG, "-");
        levelShortStringMap.put(Level.OFF, "-");
        levelShortStringMap.put(Level.ALL, "-");
        levelShortStringMap.put(Level.WARNING, "W");
        levelShortStringMap.put(Level.INFO, "I");
        levelShortStringMap.put(Level.SEVERE, "E");
    }

    private static String shortenName(String name, int length) {
        if (name.length() < length) {
            return Strings.rightPad(name, length);
        } else {
            return name.substring(0, length - 1) + "~";
        }
    }

    private static String padIfNecessary(String name, int length) {
        if (name.length() < length) {
            return Strings.rightPad(name, length);
        }
        return name;
    }

    private static class CUSTOM_FORMAT extends Formatter {

        @Override
        public String format(LogRecord record) {
            synchronized (this) {
                StringBuilder sb = new StringBuilder(levelShortStringMap.get(record.getLevel()));
                sb.append("|").append(format.format(new Date())).append("|");
                Object parameters[] = record.getParameters();

                boolean multiLine = false;
                if (record.getMessage().indexOf('\n') > 0)
                    multiLine = true;

                if (!multiLine) {
                    if (parameters == null || parameters.length == 0) {
                        sb.append(padIfNecessary(record.getMessage(), 100));
                    } else {
                        try {
                            sb.append(padIfNecessary(String.format(record.getMessage(), parameters), 100));
                        } catch (IllegalFormatException e) {
                            sb.append("Log Format Error: ")
                                    .append(record.getMessage())
                                    .append(" With Parameters: ")
                                    .append(Joiner.on(",").join(parameters));
                        }
                    }
                    sb.append("| ")
                            .append(Strings.subStringAfterLast(record.getSourceClassName(), "."))
                            .append("#");
                    sb.append(record.getSourceMethodName());

                    sb.append("\n");
                } else {
                    sb.append(" \u2193 |")
                            .append(Strings.subStringAfterLast(record.getSourceClassName(), "."))
                            .append("#");
                    sb.append(record.getSourceMethodName());
                    sb.append("\n");
                    for (String s : Splitter.on("\n").split(record.getMessage())) {
                        sb.append("    ").append(s).append("\n");
                    }
                }
                return sb.toString();
            }
        }
    }
}