package br.tec.ebz.polygon.connector.jumpserver;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.test.common.TestHelpers;

public abstract class BaseTestConnection {

    public ConnectorFacade getTestConnection(JumpServerConfiguration configuration) {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();

        APIConfiguration impl = TestHelpers.createTestConfiguration(JumpServerConnector.class, configuration);

        impl.getResultsHandlerConfiguration().setEnableAttributesToGetSearchResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableCaseInsensitiveFilter(false);
        impl.getResultsHandlerConfiguration().setEnableFilteredResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableNormalizingResultsHandler(false);
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(false);

        return factory.newInstance(impl);
    }

    public ConnectorFacade getTestConnection() {
        JumpServerConfiguration configuration = new JumpServerConfiguration();
        configuration.setBaseUrl("https://jumpserver.lab");
        configuration.setUsername("admin");
        configuration.setPassword(new GuardedString("eBZteste123...".toCharArray()));
        configuration.setIsTrustAllCertificates(true);

        return getTestConnection(configuration);
    }
}
