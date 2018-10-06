package ua.org.nalabs;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SubscriberImpl {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SubscriberImpl.class.getName());

    public static void main(String[] args) {
        InputStream resourcesInputStream = PublisherImpl.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourcesInputStream);
        } catch (IOException e) {
            LOGGER.warn("Cannot read property ", e);
        }
        String topicProperty = properties.getProperty("topic");
        String subscriberId = UUID.randomUUID().toString();
        MqttClient subscriber = null;
        try {
            subscriber = new MqttClient(properties.getProperty("mqttbroker_url"),subscriberId);
        } catch (MqttException e) {
            LOGGER.warn(e.getMessage() + " Code: " + e.getReasonCode());
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);

        try {
            Objects.requireNonNull(subscriber).connect(options);
        } catch (MqttException e) {
            LOGGER.warn("Cannot connect to MQTT Broker");
        }

        try {
            subscriber.subscribe(topicProperty, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                LOGGER.debug("[I82] Message received: topic={}, payload={}", topic, new String(payload));
                String[] recievedValuesAsSrings = new String(payload).split("/");
                DBLogging.writeToDb(recievedValuesAsSrings[0],
                        Long.valueOf(recievedValuesAsSrings[1]),
                        Double.valueOf(recievedValuesAsSrings[2]),
                        Integer.valueOf(recievedValuesAsSrings[3]));
            });
        } catch (MqttException e) {
            LOGGER.warn("Cannot subscribe on {}. Code={}.{}", topicProperty, e.getReasonCode(), e.getMessage());
        }

    }
}
