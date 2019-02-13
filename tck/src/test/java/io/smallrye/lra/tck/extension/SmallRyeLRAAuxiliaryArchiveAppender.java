package io.smallrye.lra.tck.extension;

import io.smallrye.lra.SmallRyeLRAClient;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.File;

public class SmallRyeLRAAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {
    
    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive lraJar = ShrinkWrap.create(JavaArchive.class)
                .addPackage("org.eclipse.microprofile.lra.annotation")
                .addPackage("org.eclipse.microprofile.lra.client")
                .addPackage("org.eclipse.microprofile.lra.participant")
                .addPackages(true, SmallRyeLRAClient.class.getPackage())
                .addAsManifestResource(new StringAsset("io.smallrye.lra.filter.LRAClientRequestFilter"), "services/javax.ws.rs.ext.Providers");

        File configFile = new File(ClassLoader.getSystemResource("META-INF/microprofile-config.properties").getFile());
        lraJar.addAsManifestResource(configFile, "microprofile-config.properties");
        
        return lraJar;
//        throw new RuntimeException(lraJar.toString(true));
    }
}
