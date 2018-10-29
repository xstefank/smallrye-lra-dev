package io.smallrye;

import org.junit.Test;

import java.io.File;

public class TCKRunnerITCase {

    private static final long DESTROY_TIMEOUT = 5000;

    @Test
    public void runTCK() throws Exception {
        ProcessBuilder coordinatorPb = new ProcessBuilder("java", "-jar", "lra-coordinator-swarm.jar", "-Dswarm.http.port=8082");
        coordinatorPb.inheritIO();
        coordinatorPb.directory(new File("."));
        coordinatorPb.redirectOutput(new File("target/coodinator-output.txt"));
        System.out.println("Starting LRA coordinator...");
        Process coordinatorProcess = coordinatorPb.start();
        Thread.sleep(10000);

        ProcessBuilder TCKClientPb = new ProcessBuilder("java", "-jar", 
                "smallrye-lra-tck-1.0-SNAPSHOT-thorntail.jar", "-Dswarm.port.offset=100",
                "-Dio.smallrye.lra.LRACoordinatorRESTClient/mp-rest/url=http://localhost:8082");
        TCKClientPb.inheritIO();
        TCKClientPb.redirectOutput(new File("target/tck-client-output.txt"));
        TCKClientPb.directory(new File("target"));
        System.out.println("Starting LRA TCK client...");
        Process TCKClientProcess = TCKClientPb.start();
        Thread.sleep(10000);

        ProcessBuilder tckPb = new ProcessBuilder("curl", "-XPUT", "http://localhost:8180/tck/all");
        tckPb.inheritIO();
        tckPb.redirectOutput(new File("target/tck-execution-output.txt"));
        coordinatorPb.directory(new File("."));
        System.out.println("Executing TCK run...");
        Process tckProcess = tckPb.start();

        tckProcess.waitFor();
        
        destroyProcess(coordinatorProcess);
        destroyProcess(TCKClientProcess);
    }

    private void destroyProcess(Process process) throws InterruptedException {
        process.destroy();
        Thread.sleep(DESTROY_TIMEOUT);
        if (process.isAlive()) {
            process.destroyForcibly();
        }
    }
}