/**
 * Copyright (c) 2012 sndyuk <sanada@sndyuk.com>
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.qos.logback.more.appenders;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.more.appenders.marker.StructuredMarker;
import ch.qos.logback.more.appenders.marker.StructuredMarkerUtil;

import org.komamitsu.fluency.EventTime;
import org.komamitsu.fluency.Fluency;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FluencyLogbackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final int MSG_SIZE_LIMIT = 65535;

    private Fluency fluency;

    @Override
    public void start() {
        super.start();

        try {
            this.fluency = Fluency.defaultFluency(configureServers(), configureFluency());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void append(ILoggingEvent rawData) {
        String msg;
        if (layout != null) {
            msg = layout.doLayout(rawData);
        } else {
            msg = rawData.toString();
        }
        if (msg != null && msg.length() > MSG_SIZE_LIMIT) {
            msg = msg.substring(0, MSG_SIZE_LIMIT);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("msg", msg);
        data.put("message", rawData.getFormattedMessage());
        data.put("logger", rawData.getLoggerName());
        data.put("thread", rawData.getThreadName());
        data.put("level", rawData.getLevel().levelStr);
        if (rawData.getMarker() != null) {
            Marker marker = rawData.getMarker();
            if(marker instanceof StructuredMarker) {
                StructuredMarkerUtil.mapInto(data, (StructuredMarker<Object>)marker);                
            } else {
                data.put("marker", rawData.getMarker().toString());
            }
        }
        if (rawData.hasCallerData()) {
            data.put("caller", new CallerDataConverter().convert(rawData));
        }
        if (rawData.getThrowableProxy() != null) {
            data.put("throwable", ThrowableProxyUtil.asString(rawData.getThrowableProxy()));
        }
        if (additionalFields != null) {
            data.putAll(additionalFields);
        }
        for (Map.Entry<String, String> entry : rawData.getMDCPropertyMap().entrySet()) {
            data.put(entry.getKey(), entry.getValue());
        }
        try {
            if (this.isUseEventTime()){
                EventTime eventTime = EventTime.fromEpochMilli(System.currentTimeMillis());
                fluency.emit(tag == null ? "" : tag, eventTime, data);
            }else {
                fluency.emit(tag == null ? "" : tag, data);
            }
        } catch (IOException e) {
            // pass
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            if (fluency != null) {
                try {
                    fluency.close();
                } catch (IOException e) {
                    // pass
                }
            }
        }
    }

    private String tag;
    private String remoteHost;
    private int port;
    private Map<String, String> additionalFields;
    private RemoteServers remoteServers;
    private Layout<ILoggingEvent> layout;
    private boolean ackResponseMode;
    private String fileBackupDir;
    private Integer bufferChunkInitialSize;
    private Integer bufferChunkRetentionSize;
    private Long maxBufferSize;
    private Integer waitUntilBufferFlushed;
    private Integer waitUntilFlusherTerminated;
    private Integer flushIntervalMillis;
    private Integer senderMaxRetryCount;
    private boolean useEventTime; // Flag to enable/disable usage of eventtime

    public RemoteServers getRemoteServers() {
        return remoteServers;
    }

    public void setRemoteServers(RemoteServers remoteServers) {
        this.remoteServers = remoteServers;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addAdditionalField(Field field) {
        if (additionalFields == null) {
            additionalFields = new HashMap<String, String>();
        }
        additionalFields.put(field.getKey(), field.getValue());
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public boolean isAckResponseMode() {
        return ackResponseMode;
    }

    public void setAckResponseMode(boolean ackResponseMode) {
        this.ackResponseMode = ackResponseMode;
    }

    public String getFileBackupDir() {
        return fileBackupDir;
    }

    public void setFileBackupDir(String fileBackupDir) {
        this.fileBackupDir = fileBackupDir;
    }

    public Integer getBufferChunkInitialSize() {
        return bufferChunkInitialSize;
    }

    public void setBufferChunkInitialSize(Integer bufferChunkInitialSize) {
        this.bufferChunkInitialSize = bufferChunkInitialSize;
    }

    public Integer getBufferChunkRetentionSize() {
        return bufferChunkRetentionSize;
    }

    public void setBufferChunkRetentionSize(Integer bufferChunkRetentionSize) {
        this.bufferChunkRetentionSize = bufferChunkRetentionSize;
    }

    public Long getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(Long maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public Integer getWaitUntilBufferFlushed() {
        return waitUntilBufferFlushed;
    }

    public void setWaitUntilBufferFlushed(Integer waitUntilBufferFlushed) {
        this.waitUntilBufferFlushed = waitUntilBufferFlushed;
    }

    public Integer getWaitUntilFlusherTerminated() {
        return waitUntilFlusherTerminated;
    }

    public void setWaitUntilFlusherTerminated(Integer waitUntilFlusherTerminated) {
        this.waitUntilFlusherTerminated = waitUntilFlusherTerminated;
    }

    public Integer getFlushIntervalMillis() {
        return flushIntervalMillis;
    }

    public void setFlushIntervalMillis(Integer flushIntervalMillis) {
        this.flushIntervalMillis = flushIntervalMillis;
    }

    public Integer getSenderMaxRetryCount() {
        return senderMaxRetryCount;
    }

    public void setSenderMaxRetryCount(Integer senderMaxRetryCount) {
        this.senderMaxRetryCount = senderMaxRetryCount;
    }

    /**
     * get the value for EventTime usage
     * @return true if EventTime is used, false otherwise
     */
    public boolean isUseEventTime(){ return this.useEventTime; }

    /**
     * Set the value for EventTime usage
     * @param useEventTime the new value
     */
    public void setUseEventTime(boolean useEventTime){ this.useEventTime = useEventTime; }

    protected Fluency.Config configureFluency() {
        Fluency.Config config = new Fluency.Config().setAckResponseMode(ackResponseMode);
        if (fileBackupDir != null) config.setFileBackupDir(fileBackupDir);
        if (bufferChunkInitialSize != null) config.setBufferChunkInitialSize(bufferChunkInitialSize);
        if (bufferChunkRetentionSize != null) config.setBufferChunkRetentionSize(bufferChunkRetentionSize);
        if (maxBufferSize != null) config.setMaxBufferSize(maxBufferSize);
        if (waitUntilBufferFlushed != null) config.setWaitUntilBufferFlushed(waitUntilBufferFlushed);
        if (waitUntilFlusherTerminated != null) config.setWaitUntilFlusherTerminated(waitUntilFlusherTerminated);
        if (flushIntervalMillis != null) config.setFlushIntervalMillis(flushIntervalMillis);
        if (senderMaxRetryCount != null) config.setSenderMaxRetryCount(senderMaxRetryCount);
        return config;
    }

    protected List<InetSocketAddress> configureServers() throws URISyntaxException {
        List<InetSocketAddress> dest = new ArrayList<InetSocketAddress>();
        if (remoteHost != null && port > 0) {
            dest.add(new InetSocketAddress(remoteHost, port));
        }
        if (remoteServers != null) {
            for (RemoteServer server : remoteServers.getRemoteServers()) {
                dest.add(new InetSocketAddress(server.getHost(), server.getPort()));
            }
        }
        return dest;
    }

    public static class RemoteServers {

        private List<RemoteServer> remoteServers;

        public RemoteServers() {
            remoteServers = new ArrayList<RemoteServer>();
        }

        public List<RemoteServer> getRemoteServers() {
            return remoteServers;
        }

        public void addRemoteServer(RemoteServer remoteServer) {
            remoteServers.add(remoteServer);
        }
    }

    public static class RemoteServer {

        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Field {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
