package org.fabric3.tutorials.burlap;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import junit.framework.TestCase;

import org.fabric3.tutorials.burlap.WeatherRequest;
import org.fabric3.tutorials.burlap.WeatherResponse;

import com.caucho.burlap.io.BurlapInput;
import com.caucho.burlap.io.BurlapOutput;

/**
 * Test the provisioned hessian service from a non-SCA client.
 *
 */
public class BurlapTest extends TestCase {
    
    public void test() throws Throwable {
        
        WeatherRequest request = new WeatherRequest();
        request.setCity("LONDON");
        request.setDate(new Date());
        
        URL url = new URL("http://localhost:8900/burlap-webapp/weatherService");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "x-application/hessian");

        OutputStream os = conn.getOutputStream();
        BurlapOutput output = new BurlapOutput(os);
        // output.setSerializerFactory(new SerializerFactory());
        output.startCall();
        output.writeMethod("getWeather");
        output.writeObject(request);
        output.completeCall();
        
        InputStream is = conn.getInputStream();
        BurlapInput input = new BurlapInput(is);
        WeatherResponse response = (WeatherResponse) input.readReply(WeatherResponse.class);
        
        assertNotNull(response);
        assertEquals("SUNNY", response.getForecast()); // I wish
        assertEquals(23.0, response.getTemp());
        
        os.close();
        is.close();
        conn.disconnect();
        
    }

}
