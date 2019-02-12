package io.smallrye.lra.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class SmallRyeLRAExtension implements LoadableExtension {
    
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(AuxiliaryArchiveAppender.class, SmallRyeLRAAuxiliaryArchiveAppender.class);
    }
}
