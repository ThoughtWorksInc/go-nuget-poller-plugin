package com.tw.go.plugin.nuget.exe;

import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class ProcessRunner {
    private static Logger LOGGER = Logger.getLoggerFor(ProcessRunner.class);

    public NuGetCmdOutput execute(String[] command, boolean http) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = null;
        try {
            process = processBuilder.start();
            LOGGER.info("Nuget process for " + command[2] + " started at " + new Date());
            final List<String> errorStream = new ArrayList<String>();
            launchThread(errorStream, process.getErrorStream());
            List<String> outputStream = new ArrayList<String>();
            launchThread(outputStream, process.getInputStream());
            int returnCode = getReturnCode(process);
            LOGGER.info("Nuget process for " + command[2] + " returning with exit code " + returnCode + " at " + new Date());
            if(http) return new NuGetCmdOutput(returnCode, outputStream, errorStream);
            return new NuGetCmdOutputNonHttp(returnCode, outputStream, errorStream);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
                process.destroy();
            }
        }
    }

    private void launchThread(final List<String> writeToStream, final InputStream readFromStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("enter read");
                BufferedReader br = new BufferedReader(new InputStreamReader(readFromStream));
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        writeToStream.add(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private int getReturnCode(Process process) {
        Worker worker = new Worker(process);
        worker.start();
        try {
            worker.join(30l * 1000);
            if (worker.exit != null)
                return worker.exit;
            else
                throw new RuntimeException("Nuget exec timed out (30s)");
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } finally {
            process.destroy();
        }
    }


    private static class Worker extends Thread {
        private final Process process;
        private Integer exit;

        private Worker(Process process) {
            this.process = process;
        }

        public void run() {
            LOGGER.debug("in worker run");
            try {
                exit = process.waitFor();
            } catch (InterruptedException ignore) {
                LOGGER.info("Worker Interrupted...");
            }
        }
    }

}
