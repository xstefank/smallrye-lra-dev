package io.smallrye;

import org.junit.Test;

import java.io.File;

public class TCKTestCase {

    private static final long DESTROY_TIMEOUT = 5000;

    @Test
    public void runTCK() throws Exception {
        ProcessBuilder coordinatorPb = new ProcessBuilder("java", "-jar", "lra-coordinator-swarm.jar");
        coordinatorPb.inheritIO();
        coordinatorPb.directory(new File("."));
        coordinatorPb.redirectOutput(new File("target/coodinator-output.txt"));
        System.out.println("Starting LRA coordinator...");
        Process coordinatorProcess = coordinatorPb.start();
        Thread.sleep(10000);

        ProcessBuilder tckPb = new ProcessBuilder("curl", "-XPUT", "http://localhost:8080/tck/all\\?verbose\\=false", "-i");
        tckPb.inheritIO();
        tckPb.redirectOutput(new File("target/tck-output.txt"));
        System.out.println("Executing TCK run...");
        Process tckProcess = tckPb.start();

        tckProcess.waitFor();
        
        destroyProcess(coordinatorProcess);
    }

    private void destroyProcess(Process process) throws InterruptedException {
        process.destroy();
        Thread.sleep(DESTROY_TIMEOUT);
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }
}
