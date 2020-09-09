package com.chord.framework.nacos.config.encryption;

import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created on 2020/8/13
 *
 * @author: wulinfeng
 */
public class EnvironmentPrefixHelper {

    private static final String PROFILES = "profiles";

    private static final String NAME = "name";

    private static final String ESCAPE = "{plain}";

    /**
     *
     * 解析暗文中的{key:keyName}和{profile:profile}形式的前缀
     *
     * @param name
     * @param profiles
     * @param text
     * @return
     */
    public Map<String, String> getEncryptorKeys(String name, String profiles,
                                                String text) {

        Map<String, String> keys = new LinkedHashMap<String, String>();

        // 去除前缀{key:keyName}和{profile:profile}
        text = removeEnvironmentPrefix(text);
        keys.put(NAME, name);
        keys.put(PROFILES, profiles);

        // 去除{plain}
        if (text.contains(ESCAPE)) {
            text = text.substring(0, text.indexOf(ESCAPE));
        }

        String[] tokens = StringUtils.split(text, "}");
        while (tokens != null) {
            // 将{name:value}视为{key:value}进行处理
            String token = tokens[0].trim();
            if (token.startsWith("{")) {
                String key = "";
                String value = "";
                if (token.contains(":") && !token.endsWith(":")) {
                    key = token.substring(1, token.indexOf(":"));
                    value = token.substring(token.indexOf(":") + 1);
                }
                else {
                    // 没有value的情况
                    key = token.substring(1);
                }
                keys.put(key, value);
            }
            text = tokens[1];
            tokens = StringUtils.split(text, "}");
        }

        return keys;

    }

    public String addPrefix(Map<String, String> keys, String input) {
        keys.remove(NAME);
        keys.remove(PROFILES);
        StringBuilder builder = new StringBuilder();
        for (String key : keys.keySet()) {
            builder.append("{").append(key).append(":").append(keys.get(key)).append("}");
        }
        builder.append(input);
        return builder.toString();
    }

    public String stripPrefix(String value) {
        // 不包含"}"则表示不包含前缀
        if (!value.contains("}")) {
            return value;
        }
        // {plain}的类容直接获取
        if (value.contains(ESCAPE)) {
            return value.substring(value.indexOf(ESCAPE) + ESCAPE.length());
        }
        // 去除"{*}"前缀
        return value.replaceFirst("^(\\{.*?:.*?\\})+", "");
    }

    private String removeEnvironmentPrefix(String input) {
        return input.replaceFirst("\\{name:.*\\}", "").replaceFirst("\\{profiles:.*\\}",
                "");
    }

}
