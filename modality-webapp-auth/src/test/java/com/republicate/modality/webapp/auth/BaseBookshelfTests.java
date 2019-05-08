package com.republicate.modality.webapp.auth;

import junit.framework.TestCase;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

public class BaseBookshelfTests// extends TestCase
{
    protected static DataSource initDataSource() throws Exception
    {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:hsqldb:.;hsqldb.sqllog=3");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    private static boolean dataSourcePopulated = false;

    protected static synchronized void populateDataSource() throws Exception
    {
        if (dataSourcePopulated)
        {
            return;
        }
        DataSource ds = initDataSource();
        Connection connection = ds.getConnection();
        Statement statement = connection.createStatement();
        String sql = IOUtils.toString(getResourceReader("bookshelf.sql"));
        for (String command : sql.split(";"))
        {
            if (command.trim().length() == 0) continue;
            //System.err.println("Running ["+command+"]");
            statement.executeUpdate(command);
            //System.err.println("Done.");
        }
        statement.close();
        connection.close();
        dataSourcePopulated = true;
    }

    protected static URL getResource(String name)
    {
        return BaseBookshelfTests.class.getClassLoader().getResource(name);
    }

    protected static Reader getResourceReader(String name)
    {
        return new InputStreamReader(BaseBookshelfTests.class.getClassLoader().getResourceAsStream(name), StandardCharsets.UTF_8);
    }

    public static class TestJNDIContext extends InitialContext
    {
        public TestJNDIContext() throws NamingException
        {}

        @Override
        public Object lookup(String name) throws NamingException
        {
            try
            {
                // System.err.println("@@@ looking for " + name);
                switch (name)
                {
                    case "java:comp/env": return this;
                    case "jdbc/test-data-source": return initDataSource();
                    default: return null;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*
     * A dummy jndi mechanism
     */

    static TestJNDIContext jndiContext;
    static
    {
        try
        {
            // define system property after singleton creation to avoid infinite loop
            jndiContext = new TestJNDIContext();
            System.setProperty("java.naming.factory.initial", JNDIContextFactory.class.getName());
        }
        catch (NamingException ne)
        {
            ne.printStackTrace();
        }
    }

    public static class JNDIContextFactory implements InitialContextFactory
    {
        @Override
        public javax.naming.Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
        {
            return jndiContext;
        }
    }


}
