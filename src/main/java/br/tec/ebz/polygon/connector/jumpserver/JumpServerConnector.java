/*
 * Copyright (c) 2010-2014 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.tec.ebz.polygon.connector.jumpserver;

import jakarta.ws.rs.ProcessingException;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.TestOp;

import java.nio.channels.UnresolvedAddressException;


@ConnectorClass(displayNameKey = "jumpserver.connector.display", configurationClass = JumpServerConfiguration.class)
public class JumpServerConnector implements Connector, TestOp {

    private static final Log LOG = Log.getLog(JumpServerConnector.class);

    private JumpServerConfiguration configuration;
    private JumpServerConnection connection;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        this.configuration = (JumpServerConfiguration)configuration;
        try {
            configuration.validate();

            connection = new JumpServerConnection(this.configuration);
            connection.init();
            connection.setupServices();

        } catch (UnresolvedAddressException e) {
            LOG.error("Could not connect to the Jump Server, reason: \"{0}\"", e.getMessage());
            throw new ConnectionFailedException(e.getMessage());
        }
        catch (ProcessingException e) {
            LOG.error("Could not connect to the Jump Server, reason: \"{0}\"", e.getMessage());
            throw new ConnectionFailedException("URL is not valid");
        } catch (ConnectionFailedException e) {
            throw e;
        }  catch (Exception e) {
            throw new ConnectorException("Could not connect to services, reason: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        configuration = null;
        if (connection != null) {
            connection.dispose();
            connection = null;
        }
    }

    @Override
    public void test() {
        if (!connection.isValidToken()) {
            throw new ConnectionFailedException("Token is not valid");
        }
        LOG.ok("Test connection executed successfully.");
    }
}
