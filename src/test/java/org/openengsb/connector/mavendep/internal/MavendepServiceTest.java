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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.edb.EDBBatchEvent;
import org.openengsb.core.api.edb.EDBDeleteEvent;
import org.openengsb.core.api.edb.EDBInsertEvent;
import org.openengsb.core.api.edb.EDBUpdateEvent;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.dependency.DependencyDomainEvents;
import org.openengsb.domain.dependency.MergeFailEvent;
import org.openengsb.domain.dependency.MergeSuccessEvent;

public class MavendepServiceTest {

    private MavendepServiceImpl service;
    private File testDir;
    private OpenEngSBFileModel fileModel;
    private myDependencyDomainEvents events = new myDependencyDomainEvents();
    private Event lastEvent;

    class myDependencyDomainEvents implements DependencyDomainEvents {

        @Override
        synchronized public void raiseEvent(MergeSuccessEvent arg0) {
            lastEvent = arg0;
            this.notifyAll();
        }

        @Override
        synchronized public void raiseEvent(MergeFailEvent arg0) {
            lastEvent = arg0;
            this.notifyAll();
        }

        @Override
        public void raiseEvent(EDBInsertEvent arg0) {
        }

        @Override
        public void raiseEvent(EDBDeleteEvent arg0) {
        }

        @Override
        public void raiseEvent(EDBUpdateEvent arg0) {
        }

        @Override
        public void raiseEvent(EDBBatchEvent arg0) {
        }
    }

    @Before
    public void setUp() throws Exception {
        service = new MavendepServiceImpl("42");
        service.setDependencyEvents(events);

        testDir = File.createTempFile("repository", "");
        testDir.delete();
        testDir.mkdir();
        FileUtils.copyDirectory(new File(getPath("test-unit-success")), testDir);
        fileModel = createFileModel(testDir);
    }

    @After
    public void deleteLogFile() throws IOException {
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    public void testWithNonexistentPomFile() throws InterruptedException, IOException {
        service.setPomFile("blubba.xml");
        service.setAttribute("junit.version");

        synchronized(events) {
            service.merge(fileModel, "123");
            events.wait();
        }
        assertThat(lastEvent instanceof MergeFailEvent, is(true));
    }

    @Test
    public void testWithInvalidPomFile() throws InterruptedException, IOException {
        final String[] pomFiles = {"invalid1.xml", "invalid2.xml", "invalid3.xml"};
        
        for (String f : pomFiles) {
            service.setPomFile(f);
            service.setAttribute("junit.version");
    
            synchronized(events) {
                service.merge(fileModel, "123");
                events.wait();
            }
            assertThat(lastEvent instanceof MergeFailEvent, is(true));
        }
    }

    @Test
    public void testWithInvalidProperty() throws InterruptedException, IOException {
        service.setPomFile("pom.xml");
        service.setAttribute("lalalalalalalalala");

        String origContent = FileUtils.readFileToString(new File(testDir, "pom.xml"));

        synchronized(events) {
            service.merge(fileModel, "123");
            events.wait();
        }
        assertThat(lastEvent instanceof MergeFailEvent, is(true));

        String modifiedContent = FileUtils.readFileToString(new File(testDir, "pom.xml"));
        assertThat(origContent.equals(modifiedContent), is(true));
    }
    
    @Test
    public void testWithCorrectParameters() throws InterruptedException, IOException {
        /* A random uuid without any deeper meaning */
        final String testVersion = "64c80721-17fc-4b08-acff-79e33be4fec4";

        service.setPomFile("pom.xml");
        service.setAttribute("junit.version");

        String content = FileUtils.readFileToString(new File(testDir, "pom.xml"));
        assertThat(content.contains("<junit.version>" + testVersion + "</junit.version>"), is(false));

        synchronized(events) {
            service.merge(fileModel, testVersion);
            events.wait();
        }
        assertThat(lastEvent instanceof MergeSuccessEvent, is(true));
        content = FileUtils.readFileToString(new File(testDir, "pom.xml"));
        assertThat(content.contains("<junit.version>" + testVersion + "</junit.version>"), is(true));
    }

    private OpenEngSBFileModel createFileModel(File f) {
        OpenEngSBFileModel model = ModelUtils.createEmptyModelObject(OpenEngSBFileModel.class);
        model.setFile(f);
        return model;
    }

    private String getPath(String folder) {
        return ClassLoader.getSystemResource(folder).getFile();
    }
}
