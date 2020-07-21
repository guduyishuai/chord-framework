package com.chord.framework.boot.autoconfigure.kafka.selector;

import com.chord.framework.boot.autoconfigure.kafka.EosKafkaAutoConfiguration;
import com.chord.framework.boot.autoconfigure.kafka.NormalKafkaAutoConfiguration;
import com.chord.framework.boot.autoconfigure.kafka.annotation.EnableKafka;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2020/7/17
 *
 * @author: wulinfeng
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class EnableKafkaImportSelector extends SpringFactoryImportSelector<EnableKafka> {

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        String[] imports = super.selectImports(metadata);

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(getAnnotationClass().getName(), true));

         EnableKafka.Mode mode = attributes.getEnum("mode");

         switch(mode) {
             case EOS : {
                 List<String> importsList = new ArrayList<>(Arrays.asList(imports));
                 importsList.add(EosKafkaAutoConfiguration.class.getName());
                 return importsList.toArray(new String[0]);
             }
             case NORMAL: {
                 List<String> importsList = new ArrayList<>(Arrays.asList(imports));
                 importsList.add(NormalKafkaAutoConfiguration.class.getName());
                 return importsList.toArray(new String[0]);
             }
             default: return imports;
         }
    }

    @Override
    protected boolean hasDefaultFactory() {
        return false;
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

}
