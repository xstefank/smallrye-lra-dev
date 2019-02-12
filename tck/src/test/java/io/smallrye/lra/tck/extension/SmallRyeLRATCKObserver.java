package io.smallrye.lra.tck.extension;

import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeSetup;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import java.util.Date;

public class SmallRyeLRATCKObserver {

    public void beforeStart(@Observes BeforeStart event) {
        System.out.println("Before start " + event + ", " + new Date());
    }

    public void beforeSetup(@Observes BeforeSetup event) {
        System.out.println("Before setup " + event + ", " + new Date());
    }

    public void beforeDeploy(@Observes BeforeDeploy event) {
        System.out.println("Before deploy " + event + ", " + new Date());
    }

    public void beforeSuite(@Observes BeforeSuite event) {
        System.out.println("Before suite " + event + ", " + new Date());
    }

    public void beforeSuite(@Observes BeforeClass event) {
        System.out.println("Before class " + event + ", " + new Date());
    }

    public void afterDeploy(@Observes AfterDeploy event) {
        System.out.println("After deploy " + event + ", " + new Date());
    }

    public void afterStart(@Observes AfterStart event) {
        System.out.println("After start " + event + ", " + new Date());
    }

    public void afterSetup(@Observes AfterSetup event) {
        System.out.println("After setup " + event + ", " + new Date());
    }

    public void afterClass(@Observes AfterClass event) {
        System.out.println("After class " + event + ", " + new Date());
    }
}
