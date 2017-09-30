package com.github.taodong.maven.plugins.cloud.utils;

import com.google.common.base.Joiner;
import org.apache.commons.exec.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public class ShellExecutor {
    // default shell time out at 10 minutes
    private static long defaultTimeout = 10;

    /**
     * Execute a single command and get output
     * @param command - command to run
     * @param workingDirectory - working directory
     * @param timeout - timeout in seconds
     * @return command output as string list
     */
    public List<String> executeSingleCommandGetOutput(final Log logger, final String command, final File workingDirectory, long timeout) throws Exception {
        Executor executor = new DefaultExecutor();

        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        if (timeout < 1) {
            timeout = defaultTimeout;
        }
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout * 1000);
        executor.setWatchdog(watchdog);
        logger.debug(Joiner.on(" ").skipNulls().join("Set command time out for", timeout,"seconds"));

        if (workingDirectory != null) {
            executor.setWorkingDirectory(workingDirectory);
            logger.debug(Joiner.on(" ").skipNulls().join("Set working directory as", workingDirectory.getAbsolutePath()));
        }

        try (StringOutputStream outputStream = new StringOutputStream(logger) ){
            logger.debug(Joiner.on(" ").skipNulls().join("Executing", command));
            CommandLine cl = CommandLine.parse(command);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            executor.setStreamHandler(streamHandler);
            executor.execute(cl);
            List<String> rs = outputStream.getOutputs();
            return rs;
        }
    }

    /**
     * Execute shell commands
     * @param logger - logger for shell outputs
     * @param commands - command lines to be executed
     * @param workingDirectory - working directory
     * @param timeout - time out in seconds, if less than 1, the default time out will be 5 minutes
     * @return
     *  1 when all commands ran successfully otherwise -1
     */
    public int executeCommands(final Log logger, final List<String> commands, final File workingDirectory, long timeout) {
        OutputStream stdout = null;
        OutputStream stderr = null;

        Executor executor = new DefaultExecutor();

        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        if (timeout < 1) {
            timeout = defaultTimeout;
        }
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout * 1000);
        executor.setWatchdog(watchdog);
        logger.debug(Joiner.on(" ").skipNulls().join("Set command time out for", timeout,"seconds"));

        if (workingDirectory != null) {
            executor.setWorkingDirectory(workingDirectory);
            logger.debug(Joiner.on(" ").skipNulls().join("Set working directory as", workingDirectory.getAbsolutePath()));
        }

        try {
            stdout = new LoggerOutputStream(logger, 0);
            stderr = new LoggerOutputStream(logger, 1);

            ExecuteStreamHandler streamHandler = new PumpStreamHandler(stdout, stderr);
            executor.setStreamHandler(streamHandler);

            for (String command : commands) {
                logger.debug(Joiner.on(" ").skipNulls().join("Executing", command));
                CommandLine cl = CommandLine.parse(command);
                int rs = executor.execute(cl);
                if (executor.isFailure(rs)) {
                    logger.error(Joiner.on(" ").skipNulls().join("Process stopped due to failed to execute command", command));
                    return -1;
                }
            }
        } catch (ExecuteException e) {
            if (executor.getWatchdog() != null && executor.getWatchdog().killedProcess()) {
                logger.error(Joiner.on(" ").skipNulls().join("Process killed after", timeout, "seconds timeout"));
            } else {
                logger.error(Joiner.on(" ").skipNulls().join("Process failed.", e.getMessage()), e);
                return -1;
            }
        } catch (IOException e) {
            logger.error(Joiner.on(" ").skipNulls().join("Process failed.", e.getMessage()), e);
            return -1;
        } finally {
            if (stdout != null) {
                try {
                    stdout.flush();
                    stdout.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
            if (stderr != null) {
                try {
                    stderr.flush();
                    stderr.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }

        return 1;
    }

    private static class LoggerOutputStream extends LogOutputStream {
        private final Log logger;

        LoggerOutputStream(Log logger, int logLevel) {
            super(logLevel);
            this.logger = logger;
        }

        @Override
        public final void flush() {
            // buffer processing on close() only
        }

        @Override
        protected void processLine(final String line, final int logLevel) {
            if (logLevel == 0) {
                logger.info(line);
            } else {
                logger.error(line);
            }
        }
    }

    private static class StringOutputStream extends  LogOutputStream {
        private final List<String> outputs = new ArrayList<>();
        private final Log logger;

        public StringOutputStream(Log logger) {
            super(0);
            this.logger = logger;
        }

        @Override
        protected void processLine(String line, int logLevel) {
            if (logLevel == 0) {
                outputs.add(line);
            } else {
                logger.error(line);
            }
        }

        public List<String> getOutputs() {
            return outputs;
        }
    }
}
