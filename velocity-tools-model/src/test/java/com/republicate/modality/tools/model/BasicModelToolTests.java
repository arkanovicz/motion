package com.republicate.modality.tools.model;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.republicate.modality.ModelRepository;
import com.republicate.modality.webapp.ModalityView;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.Toolbox;
import org.apache.velocity.tools.ToolboxFactory;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.XmlFactoryConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * <p>Basic model tests</p>
 *
 * @author Claude Brisson
 * @since VelocityTools 3.1
 * @version $Id$
 */
public class BasicModelToolTests extends BaseBookshelfTests
{
    public @Test void instanciateToolbox() throws Exception
    {
        XmlFactoryConfiguration config = new XmlFactoryConfiguration();
        config.read(ModalityView.MODALITY_TOOLS_DEFAULTS_PATH, true);
        config.setProperty("datasource", initDataSource());
        ToolboxFactory factory = config.createFactory();
        Toolbox toolbox = factory.createToolbox("application");
        Object obj = toolbox.get("model");
        assertNotNull(obj);
        assertTrue(obj instanceof ModelTool);
    }

    public @Test void loadToolbox() throws Exception
    {
        ToolManager manager = new ToolManager();
        manager.setVelocityEngine(createVelocityEngine((String)null));
        FactoryConfiguration config = ConfigurationUtils.find(ModelTool.MODEL_TOOLS_DEFAULTS_PATH);
        FactoryConfiguration modelConfig = ConfigurationUtils.find("blank_model_tools.xml");
        config.addConfiguration(modelConfig);
        manager.configure(config);
        Context context = manager.createContext();
        Object obj = context.get("model");
        assertNotNull(obj);
        assertTrue(obj instanceof ModelTool);
    }

    public @Test void testUberspector() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties velProps = new Properties();
        velProps.put("introspector.uberspect.class", "com.republicate.modality.tools.model.ModelUberspector, org.apache.velocity.util.introspection.UberspectImpl");
        VelocityEngine engine = createVelocityEngine(velProps);

        ModelTool model = new ModelTool();
        Map<String, Object> props = new HashMap<>();
        props.put("datasource", dataSource);
        props.put("reverse", "full");
        props.put("definition", "blank_model.xml");
        props.put("identifiers.inflector", "org.atteo.evo.inflector.English");
        props.put("identifiers.mapping.*", "lowercase");
        props.put("identifiers.mapping.*.*", "lowercase");
        model.configure(props);
        Context context = new VelocityContext();
        context.put("model", model);
        StringWriter out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "$model.book.fetch(1).publisher.name"));
        assertEquals("Green Penguin Books", out.toString());
        out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "#foreach( $book_author in $model.book.fetch(1).book_authors )$book_author.author.author_id#end"));
        assertEquals("12", out.toString());
    }

    public @Test void testExtended() throws Exception
    {
        DataSource dataSource = initDataSource();
        Properties velProps = new Properties();
        velProps.put("introspector.uberspect.class", "com.republicate.modality.tools.model.ModelUberspector, org.apache.velocity.util.introspection.UberspectImpl");
        VelocityEngine engine = createVelocityEngine(velProps);

        ModelTool model = new ModelTool();
        Map<String, Object> props = new HashMap<>();
        props.put("datasource", dataSource);
        props.put("reverse", "full");
        props.put("definition", "blank_model.xml");
        props.put("identifiers.inflector", "org.atteo.evo.inflector.English");
        props.put("identifiers.mapping.*", "lowercase");
        props.put("identifiers.mapping.*.*", "lowercase");
        model.configure(props);
        Context context = new VelocityContext();
        context.put("model", model);
        StringWriter out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "$model.book.fetch(1).publisher.name"));
        assertEquals("Green Penguin Books", out.toString());
        out = new StringWriter();
        assertTrue(engine.evaluate(context, out, "test", "#foreach( $book_author in $model.book.fetch(1).book_authors )$book_author.author.author_id#end"));
        assertEquals("12", out.toString());
    }

    @Before
    public void clearRepository()
    {
        ModelRepository.clear();
    }

    @BeforeClass
    public static void populateDataSource() throws Exception
    {
        BaseBookshelfTests.populateDataSource();
    }

}
