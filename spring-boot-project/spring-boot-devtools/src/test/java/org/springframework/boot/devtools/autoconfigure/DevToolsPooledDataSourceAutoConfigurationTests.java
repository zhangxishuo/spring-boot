/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.devtools.autoconfigure;

import java.io.File;
import java.io.IOException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DevToolsDataSourceAutoConfiguration} with a pooled data source.
 *
 * @author Andy Wilkinson
 */
class DevToolsPooledDataSourceAutoConfigurationTests extends AbstractDevToolsDataSourceAutoConfigurationTests {

	@BeforeEach
	public void before(@TempDir File tempDir) throws IOException {
		System.setProperty("derby.stream.error.file", new File(tempDir, "derby.log").getAbsolutePath());
	}

	@AfterEach
	public void after() {
		System.clearProperty("derby.stream.error.file");
	}

	@Test
	void autoConfiguredInMemoryDataSourceIsShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(
				() -> createContext(DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement).execute("SHUTDOWN");
	}

	@Test
	void autoConfiguredExternalDataSourceIsNotShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(() -> createContext("org.postgresql.Driver",
				DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement, never()).execute("SHUTDOWN");
	}

	@Test
	void h2ServerIsNotShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(() -> createContext("org.h2.Driver",
				"jdbc:h2:hsql://localhost", DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement, never()).execute("SHUTDOWN");
	}

	@Test
	void inMemoryH2IsShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(() -> createContext("org.h2.Driver", "jdbc:h2:mem:test",
				DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement, times(1)).execute("SHUTDOWN");
	}

	@Test
	void hsqlServerIsNotShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(() -> createContext("org.hsqldb.jdbcDriver",
				"jdbc:hsqldb:hsql://localhost", DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement, never()).execute("SHUTDOWN");
	}

	@Test
	void inMemoryHsqlIsShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(() -> createContext("org.hsqldb.jdbcDriver",
				"jdbc:hsqldb:mem:test", DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement, times(1)).execute("SHUTDOWN");
	}

	@Test
	void derbyClientIsNotShutdown() throws Exception {
		ConfigurableApplicationContext context = getContext(() -> createContext("org.apache.derby.jdbc.ClientDriver",
				"jdbc:derby://localhost", DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(context.getBean(DataSource.class));
		context.close();
		verify(statement, never()).execute("SHUTDOWN");
	}

	@Test
	void inMemoryDerbyIsShutdown() throws Exception {
		ConfigurableApplicationContext configurableApplicationContext = getContext(
				() -> createContext("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:memory:test",
						DataSourceAutoConfiguration.class, DataSourceSpyConfiguration.class));
		Statement statement = configureDataSourceBehavior(configurableApplicationContext.getBean(DataSource.class));
		configurableApplicationContext.close();
		verify(statement, times(1)).execute("SHUTDOWN");
	}

}
