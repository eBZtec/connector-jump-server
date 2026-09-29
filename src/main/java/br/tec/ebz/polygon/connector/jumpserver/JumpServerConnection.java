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

import br.tec.ebz.polygon.connector.jumpserver.api.Connection;
import org.identityconnectors.common.logging.Log;

import java.net.MalformedURLException;

public class JumpServerConnection extends Connection {

    private static final Log LOG = Log.getLog(JumpServerConnection.class);


    public JumpServerConnection(JumpServerConfiguration configuration) throws MalformedURLException {
        super(configuration);
    }

    public void setupServices() {
        LOG.ok("Initializing connector services...");
    }

    public void dispose() {
        LOG.ok("Connector disposed successfully");
    }
}