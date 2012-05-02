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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.dependency.DependencyDomain;
import org.openengsb.domain.dependency.DependencyDomainEvents;
import org.openengsb.domain.dependency.MergeFailEvent;
import org.openengsb.domain.dependency.MergeSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavendepServiceImpl extends AbstractOpenEngSBConnectorService implements DependencyDomain {
    private static final Logger log = LoggerFactory.getLogger(MavendepServiceImpl.class);

    private String pomfile;
    private String attribute;
    private DependencyDomainEvents events;
    private Executor executor = Executors.newSingleThreadExecutor();

    public MavendepServiceImpl(String instanceId) {
        super(instanceId);
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void merge(final OpenEngSBFileModel directory, final String dependencyLocation, final long processId) {
        final String contextId = ContextHolder.get().getCurrentContextId();
        Runnable runMerge = new Runnable() {

            @Override
            public void run() {
                String oldCtx = ContextHolder.get().getCurrentContextId();
                ContextHolder.get().setCurrentContextId(contextId);
                File projectPath = directory.getFile();

                String error = doMerge(projectPath, dependencyLocation);
                
                if (error == null) {
                    OpenEngSBFileModel outPath = ModelUtils.createEmptyModelObject(OpenEngSBFileModel.class);
                    outPath.setFile(projectPath);
                    events.raiseEvent(new MergeSuccessEvent(processId, outPath, ""));
                } else {
                    System.out.println("Error: " + error);
                    events.raiseEvent(new MergeFailEvent(processId, error));
                }
                ContextHolder.get().setCurrentContextId(oldCtx);
            }
        };
        executor.execute(runMerge);
    }

    private String doMerge(File projectPath, String dependencyLocation) {
        log.debug("Opening file" + pomfile);
        File pomFile = new File(projectPath, pomfile);
        if (pomFile == null) return "File " + pomfile + " not found";
        log.debug("File " + pomfile + " opened successfully");

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = builderFactory.newDocumentBuilder();
            doc = builder.parse(pomFile);
        } catch (Exception e) {
            log.info("XML parse exception", e);
            return "Failed to parse " + pomfile + ": " + e.getMessage();
        }

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr;
        try {
            expr = xpath.compile("/project/properties/" + attribute);
        } catch (XPathExpressionException e) {
            log.error("XPath compile error:", e);
            return "Internal error";
        }

        Object result;
        try {
            result = expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return "huhuh?";
        }
        
        NodeList nodes = (NodeList) result;
        if (nodes.getLength() != 1) {
            return "Unexpected number of matching nodes: " + nodes.getLength();
        }

        Node n = nodes.item(0);
        NodeList nlList = n.getChildNodes();
        Node nValue = (Node) nlList.item(0);
        nValue.setNodeValue(dependencyLocation);

        Source source = new DOMSource(doc);

        // Prepare the output file
        Result writeResult = new StreamResult(pomFile);

        // Write the DOM document to the file
        Transformer xformer;
        try {
            xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, writeResult);
        } catch (Exception e) {
            log.error("XML write error:", e);
            return "XML write error : " + e.getMessage();
        }
        return null;
    }

    public void setPomFile(String pomfile) {
        this.pomfile = pomfile;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setDependencyEvents(DependencyDomainEvents events) {
        this.events = events;
    }

    public DependencyDomainEvents getDependencyEvents() {
        return events;
    }
}
