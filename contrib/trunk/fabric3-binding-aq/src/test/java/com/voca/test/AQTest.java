package com.voca.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TransactionInProgressException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.enhydra.jdbc.standard.StandardXADataSource;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.xa.client.OracleXADataSource;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;
import oracle.jms.AdtMessage;
import oracle.xdb.XMLType;

/**
 * This TODO class/interface ...
 */
public class AQTest extends TestCase {

    /**
     * Tests
     */
    public void testCreateCOnnection() throws Exception {
        final ConnectionFactory connectionFactory = AQjmsFactory.getConnectionFactory(getDataSource());
        connectionFactory.createConnection();
    }

    /**
     * Tests via Queue payload type of SYS.XMLTYPE
     * 
     * @throws Exception
     */
    public void testSendBySysXmlType() throws Exception {
        /* s.getQueue("babur", "TESTER_Q"); */

        final DataSource ds = getDataSource();       
        final XMLType xml = createXMLType(ds);

        ConnectionFactory cf = AQjmsFactory.getConnectionFactory(ds);
        Connection c =  cf.createConnection();
        AQjmsSession s = (AQjmsSession)c.createSession(true, Session.SESSION_TRANSACTED);
        s.getQueue("babur", "SAFESTORE_INPUTRNR_Q"); 

        Queue queue = s.createQueue("babur.SAFESTORE_INPUTRNR_Q");

        String name = queue.getQueueName();
        System.err.println("NAME " + name);

        MessageProducer producer = s.createProducer(queue);
        AdtMessage adtMessage = s.createORAMessage(xml);

        producer.send(adtMessage);
        s.commit();
    }

    /**
     * Tests via QUEUE Payload type set up as SYS.AQ$_JMS_MESSAGE
     */
    public void testSendByJMSQueue() throws Exception {
        final DataSource ds = getDataSource();

        QueueConnectionFactory cf = AQjmsFactory.getQueueConnectionFactory(ds);
        QueueConnection c =  cf.createQueueConnection();
        Session s =  c.createQueueSession(true, Session.SESSION_TRANSACTED);

        Queue queue = s.createQueue("babur.TESTER_2");
        String name = queue.getQueueName();
        System.err.println("NAME " + name);
        MessageProducer producer = s.createProducer(queue);
        TextMessage msg = s.createTextMessage();
        msg.setText("<hello>test</hello>");
        producer.send(msg);
        s.commit();

    }
    
    /**
     * Tests via QUEUE Payload type set up as SYS.AQ$_JMS_MESSAGE
     */
    public void testSendByJMSQueueXAFailure() throws Exception {        
        org.enhydra.jdbc.oracle.OracleXADataSource ds = new org.enhydra.jdbc.oracle.OracleXADataSource();
        ds.setUser("babur");
        ds.setPassword("babur");      
        ds.setUrl("jdbc:oracle:thin:@10.64.10.151:1521:edpdev");
        ds.setDriverName("oracle.jdbc.driver.OracleDriver");
        ConnectionFactory non_xa= AQjmsFactory.getConnectionFactory(ds);
        Connection cr = non_xa.createConnection();
        Session ss =  cr.createSession(true, Session.SESSION_TRANSACTED);
        Queue queueq = ss.createQueue("babur.TESTER_2");        
        MessageProducer producerp = ss.createProducer(queueq);
        TextMessage msgt = ss.createTextMessage();
        msgt.setText("<hello>test</hello>");
        producerp.send(msgt);
        ss.commit();
             
        /* NOTE THSI WILL NOT COMMIT WITH dsx as there is no TX enlisted but there is a problem with 
         * enhydra
         */
        oracle.jdbc.xa.client.OracleXADataSource dsx = new  oracle.jdbc.xa.client.OracleXADataSource();
        dsx.setUser("babur");
        dsx.setPassword("babur");      
        dsx.setURL("jdbc:oracle:thin:@10.64.10.151:1521:edpdev");       
        
        XAConnectionFactory cf = AQjmsFactory.getXAConnectionFactory(dsx);
        XAConnection c = cf.createXAConnection();
        Session s =  c.createXASession();//(true, Session.SESSION_TRANSACTED);

        Queue queue = s.createQueue("babur.TESTER_2");
        String name = queue.getQueueName();
        System.err.println("NAME " + name);
        MessageProducer producer = s.createProducer(queue);
        TextMessage msg = s.createTextMessage();
        msg.setText("<hello>test</hello>");
        producer.send(msg);       
       // s.commit();                    
    }

    /**
     * TODO DOCUMENT
     * 
     * @return
     * @throws SQLException
     */
    public DataSource getDataSource() throws SQLException {
        OracleDataSource ds = new OracleDataSource();
        ds.setUser("babur");
        ds.setPassword("babur");
        ds.setDatabaseName("EDPDEV");
        ds.setServerName("10.64.10.151");
        ds.setPortNumber(1521);
        ds.setDriverType("thin");
        return ds;
    }

    /**
     * Create XML Type
     * 
     * @param ds
     * @return
     */
    private XMLType createXMLType(final DataSource ds) {
        final InputStream data = new ByteArrayInputStream("<test>hello</test>".getBytes());
        try {
            final XMLType xml = new XMLType(ds.getConnection(), data);
            return xml;
        } catch (SQLException se) {            
            throw new RuntimeException("SQL EXCeption " + se);
        }
    }

}
package com.voca.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TransactionInProgressException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.enhydra.jdbc.standard.StandardXADataSource;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.xa.client.OracleXADataSource;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;
import oracle.jms.AdtMessage;
import oracle.xdb.XMLType;

/**
 * This TODO class/interface ...
 */
public class AQTest extends TestCase {

    /**
     * Tests
     */
    public void testCreateCOnnection() throws Exception {
        final ConnectionFactory connectionFactory = AQjmsFactory.getConnectionFactory(getDataSource());
        connectionFactory.createConnection();
    }

    /**
     * Tests via Queue payload type of SYS.XMLTYPE
     * 
     * @throws Exception
     */
    public void testSendBySysXmlType() throws Exception {
        /* s.getQueue("babur", "TESTER_Q"); */

        final DataSource ds = getDataSource();       
        final XMLType xml = createXMLType(ds);

        ConnectionFactory cf = AQjmsFactory.getConnectionFactory(ds);
        Connection c =  cf.createConnection();
        AQjmsSession s = (AQjmsSession)c.createSession(true, Session.SESSION_TRANSACTED);
        s.getQueue("babur", "SAFESTORE_INPUTRNR_Q"); 

        Queue queue = s.createQueue("babur.SAFESTORE_INPUTRNR_Q");

        String name = queue.getQueueName();
        System.err.println("NAME " + name);

        MessageProducer producer = s.createProducer(queue);
        AdtMessage adtMessage = s.createORAMessage(xml);

        producer.send(adtMessage);
        s.commit();
    }

    /**
     * Tests via QUEUE Payload type set up as SYS.AQ$_JMS_MESSAGE
     */
    public void testSendByJMSQueue() throws Exception {
        final DataSource ds = getDataSource();

        QueueConnectionFactory cf = AQjmsFactory.getQueueConnectionFactory(ds);
        QueueConnection c =  cf.createQueueConnection();
        Session s =  c.createQueueSession(true, Session.SESSION_TRANSACTED);

        Queue queue = s.createQueue("babur.TESTER_2");
        String name = queue.getQueueName();
        System.err.println("NAME " + name);
        MessageProducer producer = s.createProducer(queue);
        TextMessage msg = s.createTextMessage();
        msg.setText("<hello>test</hello>");
        producer.send(msg);
        s.commit();

    }
    
    /**
     * Tests via QUEUE Payload type set up as SYS.AQ$_JMS_MESSAGE
     */
    public void testSendByJMSQueueXAFailure() throws Exception {        
        org.enhydra.jdbc.oracle.OracleXADataSource ds = new org.enhydra.jdbc.oracle.OracleXADataSource();
        ds.setUser("babur");
        ds.setPassword("babur");      
        ds.setUrl("jdbc:oracle:thin:@10.64.10.151:1521:edpdev");
        ds.setDriverName("oracle.jdbc.driver.OracleDriver");
        ConnectionFactory non_xa= AQjmsFactory.getConnectionFactory(ds);
        Connection cr = non_xa.createConnection();
        Session ss =  cr.createSession(true, Session.SESSION_TRANSACTED);
        Queue queueq = ss.createQueue("babur.TESTER_2");        
        MessageProducer producerp = ss.createProducer(queueq);
        TextMessage msgt = ss.createTextMessage();
        msgt.setText("<hello>test</hello>");
        producerp.send(msgt);
        ss.commit();
             
        /* NOTE THSI WILL NOT COMMIT WITH dsx as there is no TX enlisted but there is a problem with 
         * enhydra
         */
        oracle.jdbc.xa.client.OracleXADataSource dsx = new  oracle.jdbc.xa.client.OracleXADataSource();
        dsx.setUser("babur");
        dsx.setPassword("babur");      
        dsx.setURL("jdbc:oracle:thin:@10.64.10.151:1521:edpdev");       
        
        XAConnectionFactory cf = AQjmsFactory.getXAConnectionFactory(dsx);
        XAConnection c = cf.createXAConnection();
        Session s =  c.createXASession();//(true, Session.SESSION_TRANSACTED);

        Queue queue = s.createQueue("babur.TESTER_2");
        String name = queue.getQueueName();
        System.err.println("NAME " + name);
        MessageProducer producer = s.createProducer(queue);
        TextMessage msg = s.createTextMessage();
        msg.setText("<hello>test</hello>");
        producer.send(msg);       
       // s.commit();                    
    }

    /**
     * TODO DOCUMENT
     * 
     * @return
     * @throws SQLException
     */
    public DataSource getDataSource() throws SQLException {
        OracleDataSource ds = new OracleDataSource();
        ds.setUser("babur");
        ds.setPassword("babur");
        ds.setDatabaseName("EDPDEV");
        ds.setServerName("10.64.10.151");
        ds.setPortNumber(1521);
        ds.setDriverType("thin");
        return ds;
    }

    /**
     * Create XML Type
     * 
     * @param ds
     * @return
     */
    private XMLType createXMLType(final DataSource ds) {
        final InputStream data = new ByteArrayInputStream("<test>hello</test>".getBytes());
        try {
            final XMLType xml = new XMLType(ds.getConnection(), data);
            return xml;
        } catch (SQLException se) {            
            throw new RuntimeException("SQL EXCeption " + se);
        }
    }

}
