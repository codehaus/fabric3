package org.fabric3.tutorials.hessian;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import junit.framework.TestCase;

/**
 * Test the provisioned hessian service from a non-SCA client.
 *
 */
public class HessianTest extends TestCase {
    
    public void test() throws Throwable {
        
        WeatherRequest request = new WeatherRequest();
        request.setCity("LONDON");
        request.setDate(new Date());
        
        URL url = new URL("http://localhost:8900/hessian-webapp/weatherService");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "x-application/hessian");

        OutputStream os = conn.getOutputStream();
        Hessian2Output output = new Hessian2Output(os);
        output.setSerializerFactory(new SerializerFactory());
        output.startCall();
        output.writeHeader("callFrames");
        output.writeObject(new LinkedList());
        output.writeMethod("getWeather");
        output.writeObject(request);
        output.completeCall();
        output.flush();
        
        InputStream is = conn.getInputStream();
        Hessian2Input input = new Hessian2Input(is);
        WeatherResponse response = (WeatherResponse) input.readReply(WeatherResponse.class);
        
        assertNotNull(response);
        assertEquals("SUNNY", response.getForecast()); // I wish
        assertEquals(23.0, response.getTemp());
        
        os.close();
        is.close();
        conn.disconnect();
        
        System.err.println("*********************");
        
    }

}
