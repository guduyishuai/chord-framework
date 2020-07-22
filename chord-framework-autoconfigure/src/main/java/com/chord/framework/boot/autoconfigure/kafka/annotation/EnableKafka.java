package com.chord.framework.boot.autoconfigure.kafka.annotation;

import com.chord.framework.boot.autoconfigure.kafka.selector.EnableKafkaImportSelector;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListenerConfigurationSelector;

import java.lang.annotation.*;

import static com.chord.framework.boot.autoconfigure.kafka.annotation.EnableKafka.Mode.EOS;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({EnableKafkaImportSelector.class, KafkaListenerConfigurationSelector.class})
public @interface EnableKafka {

    Mode mode() default EOS;

    enum Mode {

        EOS, ATO, AMO, NORMAL;

    }

}
