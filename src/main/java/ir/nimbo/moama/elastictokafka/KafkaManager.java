package ir.nimbo.moama.elastictokafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class KafkaManager {
    private final String topic;

    private Producer<String, String> producer;

    public KafkaManager(String topic, String portsWithIp) {
        this.topic = topic;
        Properties props = new Properties();
        props.put("bootstrap.servers", portsWithIp);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "10000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
    }


    public void pushNewURL(String... links) {
        for (String url : links) {
            try {
                String key = new URL(url).getHost();
                producer.send(new ProducerRecord<>(topic, key, url));
            } catch (MalformedURLException e) {
                System.out.println("Wrong Exception" + url);
            }
        }
        flush();

    }


    @Override
    protected void finalize() {
        flush();
        producer.close();
    }

    private void flush() {
        producer.flush();
    }
}
