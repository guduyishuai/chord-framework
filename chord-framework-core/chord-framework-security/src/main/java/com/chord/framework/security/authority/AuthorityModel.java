package com.chord.framework.security.authority;

import lombok.Data;

import java.util.List;

/**
 * Created on 2020/7/7
 *
 * @author: wulinfeng
 */
@Data
public class AuthorityModel {

    private String partten;

    private String httpMethod;

    private List<String> roles;

}
