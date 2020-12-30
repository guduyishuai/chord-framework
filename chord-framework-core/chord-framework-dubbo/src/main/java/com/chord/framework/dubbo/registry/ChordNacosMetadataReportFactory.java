package com.chord.framework.dubbo.registry;

import com.chord.framework.dubbo.commons.ExtPropertyKeyConst;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.store.MetadataReport;
import org.apache.dubbo.metadata.store.nacos.NacosMetadataReport;
import org.apache.dubbo.metadata.store.nacos.NacosMetadataReportFactory;

/**
 * Created on 2020/8/21
 *
 * @author: wulinfeng
 */
public class ChordNacosMetadataReportFactory extends NacosMetadataReportFactory {

    @Override
    protected MetadataReport createMetadataReport(URL url) {
        return new NacosMetadataReport(url.setProtocol(ExtPropertyKeyConst.PROTOCOL_NACOS));
    }

}
