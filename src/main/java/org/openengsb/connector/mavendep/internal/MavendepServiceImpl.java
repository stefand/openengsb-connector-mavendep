/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.mavendep.internal;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.dependency.DependencyDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavendepServiceImpl extends AbstractOpenEngSBConnectorService implements DependencyDomain {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavendepServiceImpl.class);

    private String pomfile;
    private String attribute;

    public MavendepServiceImpl(String instanceId) {
        super(instanceId);
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void merge(OpenEngSBFileModel arg0) {
        // TODO Auto-generated method stub
    }

    public void setPomFile(String pomfile) {
        this.pomfile = pomfile;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
