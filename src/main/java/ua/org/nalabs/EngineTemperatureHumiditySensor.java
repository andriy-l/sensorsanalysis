package ua.org.nalabs;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class EngineTemperatureHumiditySensor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineTemperatureHumiditySensor.class.getName());
    private IMqttClient client;
    private String publisherId;
    private ThreadLocalRandom rnd = ThreadLocalRandom.current();

    public EngineTemperatureHumiditySensor(IMqttClient client, String publisherId) {
        this.client = client;
        this.publisherId = publisherId;
    }

    @Override
    public void run()  {


        if (!client.isConnected()) {
            LOGGER.info("[I31] Client not connected.");
            System.exit(0);
        }

        MqttMessage msg = readEngineTempHumidity();

        InputStream resourcesInputStream = PublisherImpl.class.getClassLoader().getResourceAsStream("application.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourcesInputStream);
        } catch (IOException e) {
            LOGGER.warn("Cannot read property ", e);
        }
        // QoS
        //
        // 0 – “at most once” semantics, also known as “fire-and-forget”.
        // Use this option when message loss is acceptable, as it does not require any kind of acknowledgment or persistence
        //
        // 1 – “at least once” semantics.
        // Use this option when message loss is not acceptable and your subscribers can handle duplicates
        //
        // 2 – “exactly once” semantics.
        // Use this option when message loss is not acceptable and your subscribers cannot handle duplicates

        msg.setQos(0);
        msg.setRetained(true);
        try {
            client.publish(properties.getProperty("topic"), msg);
            LOGGER.info("On topic {} message: {} sent", properties.getProperty("topic"), msg);
        } catch (MqttException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * This method simulates reading the engine temperature
     *
     * @return
     */
    private MqttMessage readEngineTempHumidity() {
        double temp  = 80 + rnd.nextDouble() * 20.0;
        int humidity = 710 + rnd.nextInt(50);
        long timestamp = System.currentTimeMillis();
        byte[] payload = String.format(Locale.US, "%s/%d/%04.2f/%d", publisherId, timestamp, temp, humidity).getBytes();
        MqttMessage msg = new MqttMessage(payload);
        return msg;
    }
}



