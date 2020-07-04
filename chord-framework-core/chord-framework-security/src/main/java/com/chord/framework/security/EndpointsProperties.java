package com.chord.framework.security;

import lombok.Data;
import org.springframework.http.HttpMethod;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Data
public class EndpointsProperties {

    private HttpMethod[] httpMethod = new HttpMethod[]{HttpMethod.POST};

    private boolean approvalStoreDisabled = true;

}
