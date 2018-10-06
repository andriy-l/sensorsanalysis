package ua.org.nalabs;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublisherImpl {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PublisherImpl.class.getName());

    public static void main(String[] args) {
        InputStream resourcesInputStream = PublisherImpl.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourcesInputStream);
        } catch (IOException e) {
            LOGGER.warn("Cannot read property ", e);
        }


        String publisherId = UUID.randomUUID().toString();
        IMqttClient client = null;
        try {
            String mqttBbrokerUrl = properties.getProperty("mqttbroker_url");
            client = new MqttClient(mqttBbrokerUrl, publisherId);
            LOGGER.info("Client for connection to broker: {} created", mqttBbrokerUrl);
        } catch (MqttException e) {
            LOGGER.warn("Client was not created");
        }
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        // The library will automatically try to reconnect to the server in the event of a network failure
        mqttConnectOptions.setAutomaticReconnect(true);
        // It will discard unsent messages from a previous run
        mqttConnectOptions.setCleanSession(true);
        // Connection timeout is set to 10 seconds
        mqttConnectOptions.setConnectionTimeout(10);

        try {
            Objects.requireNonNull(client).connect(mqttConnectOptions);
        } catch (MqttException e) {
            LOGGER.warn("Connection failed {} with code: {}", e.getMessage(), e.getReasonCode());
        }

        EngineTemperatureHumiditySensor mockDataGenerator = new EngineTemperatureHumiditySensor(client, publisherId);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(mockDataGenerator, 5, 10, TimeUnit.SECONDS);


        //es.shutdown();
//        try {
//            Objects.requireNonNull(client).disconnect();
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }


    }
}
