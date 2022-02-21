package io.github.dunwu.bigdata.kafka.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Zhang Peng
 * @since 2018-11-29
 */
@SpringBootTest(classes = MsgKafkaApplication.class)
public class KafkaProducerTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void test() {
        kafkaProducer.sendTransactionMsg("test", "上联：天王盖地虎");
        kafkaProducer.sendTransactionMsg("test", "下联：宝塔镇河妖");
    }

}
