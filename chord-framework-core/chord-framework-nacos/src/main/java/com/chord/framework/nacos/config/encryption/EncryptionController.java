package com.chord.framework.nacos.config.encryption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.rsa.crypto.RsaSecretEncryptor;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 *
 * 提供加密和解密的api
 *
 * Created on 2020/8/13
 *
 * @author: wulinfeng
 */
@RestController
@RequestMapping(path = "${chord.nacos.config.prefix:}")
public class EncryptionController {

    private static Log logger = LogFactory.getLog(EncryptionController.class);

    volatile private TextEncryptor encryptor;

    private EnvironmentPrefixHelper helper = new EnvironmentPrefixHelper();

    private String defaultApplicationName = "application";

    private String defaultProfile = "default";

    public EncryptionController(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public void setDefaultApplicationName(String defaultApplicationName) {
        this.defaultApplicationName = defaultApplicationName;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    @PostMapping("encrypt")
    public String encrypt(@RequestBody String data,
                          @RequestHeader("Content-Type") MediaType type) {
        return encrypt(defaultApplicationName, defaultProfile, data, type);
    }

    @RequestMapping(value = "/encrypt/{name}/{profiles}", method = RequestMethod.POST)
    public String encrypt(@PathVariable String name, @PathVariable String profiles,
                          @RequestBody String data, @RequestHeader("Content-Type") MediaType type) {
        TextEncryptor encryptor = getEncryptor(name, profiles, "");
        validateEncryptionWeakness(encryptor);
        String input = stripFormData(data, type, false);
        Map<String, String> keys = helper.getEncryptorKeys(name, profiles, input);
        String textToEncrypt = helper.stripPrefix(input);
        String encrypted = helper.addPrefix(keys,
                encryptor.encrypt(textToEncrypt));
        logger.info("Encrypted data");
        return encrypted;
    }

    @RequestMapping(value = "decrypt", method = RequestMethod.POST)
    public String decrypt(@RequestBody String data,
                          @RequestHeader("Content-Type") MediaType type) {
        return decrypt(defaultApplicationName, defaultProfile, data, type);
    }

    @RequestMapping(value = "/decrypt/{name}/{profiles}", method = RequestMethod.POST)
    public String decrypt(@PathVariable String name, @PathVariable String profiles,
                          @RequestBody String data, @RequestHeader("Content-Type") MediaType type) {
        TextEncryptor encryptor = getEncryptor(name, profiles, "");
        checkDecryptionPossible(encryptor);
        validateEncryptionWeakness(encryptor);
        try {
            encryptor = getEncryptor(name, profiles, data);
            String input = stripFormData(helper.stripPrefix(data), type, true);
            String decrypted = encryptor.decrypt(input);
            logger.info("Decrypted cipher data");
            return decrypted;
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot decrypt key:" + name + ", value:" + data, e);
            throw new InvalidCipherException();
        }
    }

    private TextEncryptor getEncryptor(String name, String profiles, String data) {
        if (encryptor == null) {
            throw new KeyNotInstalledException();
        }
        return encryptor;
    }

    private void validateEncryptionWeakness(TextEncryptor textEncryptor) {
        if (textEncryptor.encrypt("FOO").equals("FOO")) {
            throw new EncryptionTooWeakException();
        }
    }

    private String stripFormData(String data, MediaType type, boolean cipher) {

        if (data.endsWith("=") && !type.equals(MediaType.TEXT_PLAIN)) {
            try {
                data = URLDecoder.decode(data, "UTF-8");
                if (cipher) {
                    data = data.replace(" ", "+");
                }
            }
            catch (UnsupportedEncodingException e) {
                // Really?
            }
            String candidate = data.substring(0, data.length() - 1);
            if (cipher) {
                if (data.endsWith("=")) {
                    if (data.length() / 2 != (data.length() + 1) / 2) {
                        try {
                            Hex.decode(candidate);
                            return candidate;
                        }
                        catch (IllegalArgumentException e) {
                            try {
                                Base64Utils.decode(candidate.getBytes());
                                return candidate;
                            }
                            catch (IllegalArgumentException ex) {
                            }
                        }
                    }
                }
                return data;
            }
            // User posted data with content type form but meant it to be text/plain
            data = candidate;
        }

        return data;

    }

    private void checkDecryptionPossible(TextEncryptor textEncryptor) {
        if (textEncryptor instanceof RsaSecretEncryptor
                && !((RsaSecretEncryptor) textEncryptor).canDecrypt()) {
            throw new DecryptionNotSupportedException();
        }
    }

}

class KeyNotInstalledException extends RuntimeException {

}

@SuppressWarnings("serial")
class KeyNotAvailableException extends RuntimeException {

}

@SuppressWarnings("serial")
class EncryptionTooWeakException extends RuntimeException {

}

@SuppressWarnings("serial")
class InvalidCipherException extends RuntimeException {

}

@SuppressWarnings("serial")
class DecryptionNotSupportedException extends RuntimeException {

}