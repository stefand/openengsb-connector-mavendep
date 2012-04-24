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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.common.util.ModelUtils;

public class MavenServiceTest {

    private MavendepServiceImpl service;
    private File testDir;
    private OpenEngSBFileModel fileModel;

    @Before
    public void setUp() throws Exception {
        service = new MavendepServiceImpl("42");

        testDir = File.createTempFile("repository", "");
        testDir.delete();
        testDir.mkdir();
        FileUtils.copyDirectory(new File("test-unit-success"), testDir);
        fileModel = createFileModel(testDir);
    }

    @After
    public void deleteLogFile() throws IOException {
        FileUtils.deleteDirectory(testDir);
    }

    // Invalid pomfile
    
    // Invalid property
    
    // Working replace
    public void testWithCorrectParameters() {
        service.setPomFile("pom.xml");
        service.setAttribute("junit.version");
        service.merge(fileModel, "1.2.3");
    }
    
    private OpenEngSBFileModel createFileModel(File f) {
        OpenEngSBFileModel model = ModelUtils.createEmptyModelObject(OpenEngSBFileModel.class);
        model.setFile(f);
        return model;
    }
}
