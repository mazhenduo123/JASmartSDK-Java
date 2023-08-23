package com.jingansi.smart.channel;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Liuxc
 * @version 1.0
 * @description
 * @date 2023/6/16 17:40
 **/
@Slf4j
public class MQTTChannel implements MessageChannel, MqttCallbackExtended {
    private String endpoint;
    private String productKey;
    private String deviceId;
    private String username;
    private String password;
    private List<String> topics = new Vector<>();
    private MessageReceiveCallback callback;
    private MqttClient client;
    private Timer timer = new Timer();
    private Lock subscribeSync = new ReentrantLock(true);
    private Executor executor = Executors.newFixedThreadPool(10);
    private boolean isConnected = false;

    public MQTTChannel(String endpoint, String productKey, String deviceId, String username, String password, List<String> topics
            , MessageReceiveCallback callback) {
        this.endpoint = endpoint;
        this.productKey = productKey;
        this.deviceId = deviceId;
        this.username = username;
        this.password = password;
        this.topics.addAll(topics);
        this.callback = callback;
    }

    private void initMQTTConnection() {
        String clientId = "DEVICE:" + deviceId;
        try {
            client = new MqttClient(endpoint, clientId, new MemoryPersistence());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setKeepAliveInterval(20);
            mqttConnectOptions.setConnectionTimeout(10);
            client.setTimeToWait(3000);
            client.setCallback(this);
            client.setManualAcks(false);
            client.connect(mqttConnectOptions);

        } catch (MqttException e) {
            log.error("MQTT CONNECTION EXCEPTION. ERROR={}", e.getMessage());
        }
    }

    private void subscribeTopics() throws MqttException {
        String[] topicFilters = new String[topics.size()];
        topics.toArray(topicFilters);
        int[] qos = new int[topicFilters.length];
        for (int i = 0; i < qos.length; i++) {
            qos[i] = 0;
        }
        client.subscribe(topicFilters, qos);
    }

    @Override
    public void send(String topic, String data) {
        if (log.isDebugEnabled()) {
            log.debug("send message -> topic[{}], message[{}]", topic, data);
        }
        executor.execute(() -> {
            try {
                MqttMessage mqttMessage = new MqttMessage(data.getBytes(StandardCharsets.UTF_8));
                mqttMessage.setQos(0);
                client.publish(topic, mqttMessage);
            } catch (MqttException e) {
                log.error("MQTT PUBLISH MESSAGE EXCEPTION. ERROR={}", e.getMessage());
            }
        });
    }

    @Override
    public void start() {
        initMQTTConnection();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (client == null || !client.isConnected() || !isConnected) {
                    subscribeSync.lock();
                    try {
                        if (client == null || !client.isConnected() || !isConnected) {
                            client.reconnect();
                        }
                    } catch (Exception e) {
                        log.error("MQTT RECONNECT FAILED.", e);
                    } finally {
                        subscribeSync.unlock();
                    }
                }
            }
        }, 1000, 10000);
    }

    @Override
    public void stop() {
        subscribeSync.lock();
        try {
            timer.cancel();
            client.close();
        } catch (MqttException e) {
            log.error("MQTT CLOSE EXCEPTION. ERROR={}", e.getMessage());
        } finally {
            subscribeSync.unlock();
        }
    }

    public void addTopic(String topic) {
        subscribeSync.lock();
        topics.add(topic);
        try {
            subscribeTopics();
        } catch (MqttException e) {
            log.error("MQTT SUBSCRIBE EXCEPTION. ERROR={}", e.getMessage());
        } finally {
            subscribeSync.unlock();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("MQTT CONNECTION LOST. ERROR={}", throwable.getMessage());
        isConnected = false;
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        executor.execute(() -> {
            if (log.isDebugEnabled()) {
                log.debug("received message -> topic[{}], message[{}]", s, new String(mqttMessage.getPayload()));
            }
            callback.receive(s, new String(mqttMessage.getPayload()));
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void connectComplete(boolean b, String s) {
        try {
            isConnected = true;
            subscribeTopics();
        } catch (MqttException e) {
            log.error("MQTT CONNECTION EXCEPTION. ERROR={}", e.getMessage());
        }
    }
}
