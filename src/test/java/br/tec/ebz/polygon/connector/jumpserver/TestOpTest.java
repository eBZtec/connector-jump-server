package br.tec.ebz.polygon.connector.jumpserver;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.testng.annotations.Test;

public class TestOpTest extends BaseTestConnection{

    @Test
    public void deveConectarComSucessoNaBaseDoJumpServerTest() {
        ConnectorFacade facade = getTestConnection();

        facade.test();
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void naoDeveConectarNaBaseDevidoAoUsernameEstaIncorretoTest() {
        JumpServerConfiguration configuration = new JumpServerConfiguration();
        configuration.setBaseUrl("https://jumpserver.lab");
        configuration.setUsername("fake");
        configuration.setPassword(new GuardedString("senhaerrada.".toCharArray()));
        configuration.setIsTrustAllCertificates(true);

        ConnectorFacade facade = getTestConnection(configuration);

        facade.test();
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void naoDeveConectarNaBaseDevidoAoHostEstaIncorretoTest() {
        JumpServerConfiguration configuration = new JumpServerConfiguration();
        configuration.setBaseUrl("https://unknown.lab");
        configuration.setUsername("usuario.errado");
        configuration.setPassword(new GuardedString("senha_errada.".toCharArray()));
        configuration.setIsTrustAllCertificates(true);

        ConnectorFacade facade = getTestConnection(configuration);

        facade.test();
    }

    @Test(expectedExceptions = ConfigurationException.class)
    public void shouldReturnConfigurationExceptionWhenIfTheHostIsNotSet() {
        JumpServerConfiguration configuration = new JumpServerConfiguration();
        configuration.setUsername("usuario.errado");
        configuration.setPassword(new GuardedString("senha_errada.".toCharArray()));
        configuration.setIsTrustAllCertificates(true);

        ConnectorFacade facade = getTestConnection(configuration);

        facade.test();
    }
}
