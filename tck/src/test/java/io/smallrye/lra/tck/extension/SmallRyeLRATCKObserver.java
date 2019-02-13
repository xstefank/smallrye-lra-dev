package io.smallrye.lra.tck.extension;

import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

public class SmallRyeLRATCKObserver {

    private static final long DESTROY_TIMEOUT = 5000;

    private Process coordinatorProcess;

    public void beforeSuite(@Observes BeforeSuite event) throws IOException {
        ProcessBuilder coordinatorPb = new ProcessBuilder("java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9001", "-jar", "lra-coordinator-swarm.jar");
        coordinatorPb.inheritIO();
        coordinatorPb.directory(new File("."));
        coordinatorPb.redirectOutput(new File("target/coodinator-output.txt"));
        System.out.println("Starting LRA coordinator...");
        coordinatorProcess = coordinatorPb.start();
    }

    
    public void afterDeploy(@Observes AfterDeploy event) throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget coordinatorTarget = client.target("http://localhost:8080/lra-coordinator");
        
        boolean coordinatorUp = false;
        Response response = null;

        while (!coordinatorUp) {
            try {
                response = coordinatorTarget.request().get();
                if (response.getStatus() == 200) coordinatorUp = true;
            } catch (ProcessingException e) {
                // ok
            }
            
            Thread.sleep(200);
        }

        System.out.println("LRA coordinator is running...");
        response.close();
    }

    public void afterClass(@Observes AfterClass event) throws InterruptedException {
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
