/**
 * Copyright (c) 2012 sndyuk <sanada@sndyuk.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.qos.logback.more.appenders;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.MyMapMarker;

public class LogbackAppenderTest {

    private static final Logger LOG = LoggerFactory.getLogger(LogbackAppenderTest.class);

    @Test
    public void logSimple() throws InterruptedException {

        LOG.debug("Test the logger 1.");
        LOG.debug("Test the logger 2.");

        Thread.sleep(1000); // Wait a moment because these log is being appended asynchronous...
    }

    @Test
    public void logMdc() throws InterruptedException {
        MDC.put("req.requestURI", "/hello/world.js");
        LOG.debug("Test the logger 1.");
        LOG.debug("Test the logger 2.");

        Thread.sleep(1000); // Wait a moment because these log is being appended asynchronous...
    }

    @Test
    public void logMarker() throws InterruptedException {
        Marker sendEmailMarker = new BasicMarkerFactory().getMarker("SEND_EMAIL");
        LOG.debug(sendEmailMarker, "Test the marker 1.");
        LOG.debug(sendEmailMarker, "Test the marker 2.");

        Thread.sleep(1000); // Wait a moment because these log is being appended asynchronous...
    }

    @Test
    public void logNestedMarker() throws InterruptedException {
        Marker notifyMarker = new BasicMarkerFactory().getMarker("NOTIFY");
        Marker sendEmailMarker = new BasicMarkerFactory().getMarker("SEND_EMAIL");
        sendEmailMarker.add(notifyMarker);
        LOG.debug(sendEmailMarker, "Test the nested marker 1.");
        LOG.debug(sendEmailMarker, "Test the nested marker 2.");

        Thread.sleep(1000); // Wait a moment because these log is being appended asynchronous...
    }

    @Test
    public void logMapMarker() throws InterruptedException {
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("key", "value");
        Marker myMarker = new MyMapMarker<Object>("my", map);
        
        LOG.debug(myMarker, "Test the map marker.");

        Thread.sleep(1000); // Wait a moment because these log is being appended asynchronous...
    }

    
    @Test
    public void logThrowable() throws InterruptedException {
        LOG.info("Without Exception.");
        LOG.error("Test the checked Exception.", new IOException("Connection something"));
        LOG.warn("Test the unchecked Exception.", new IllegalStateException("Oh your state"));

        Thread.sleep(1000); // Wait a moment because these log is being appended asynchronous...
    }
}
