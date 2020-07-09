package com.chord.framework.security;

import lombok.Data;

import java.util.*;

/**
 * Created on 2020/7/1
 *
 * @author: wulinfeng
 */
@Data
public class ClientProperties {

    private boolean inMemory = true;

    private List<ClientInfo> infos;

    public ClientProperties() {
        this.infos = new ArrayList<>();
        infos .add(new ClientInfo());
    }

    @Data
    public static class ClientInfo {

        private String clientId = "chord";

        private String clientSecret = "chord";

        private int accessTokenValiditySeconds = 7200;

        private int refreshTokenValidtySecnods = 2592000;

        private String[] authorizedGrantTypes = new String[]{};

        private String[] authorities = new String[]{};

        private String[] scopes = new String[]{};

        private Map<String, ?> additionalInformation = new HashMap<>(2);

        private boolean autoApprove = false;

        private String[] approveScopes = new String[]{};

    }

}
