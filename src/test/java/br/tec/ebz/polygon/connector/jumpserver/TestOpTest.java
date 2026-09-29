package br.tec.ebz.polygon.connector.jumpserver;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.ConnectorFacade;
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
        configuration.setBaseUrl("http://jumpserver.lab");
        configuration.setUsername("usuario_errado");
        configuration.setPassword(new GuardedString("senha_errada.".toCharArray()));

        ConnectorFacade facade = getTestConnection(configuration);

        facade.test();
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void naoDeveConectarNaBaseDevidoAoHostEstaIncorretoTest() {
        JumpServerConfiguration configuration = new JumpServerConfiguration();
        configuration.setBaseUrl("http://host.errado");
        configuration.setUsername("usuario_errado");
        configuration.setPassword(new GuardedString("senha_errada.".toCharArray()));

        ConnectorFacade facade = getTestConnection(configuration);

        facade.test();
    }
}
