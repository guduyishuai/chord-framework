package com.chord.framework.mybatis.runtime.statement.injector.program;

import com.baomidou.mybatisplus.core.toolkit.Constants;

/**
 * Created on 2020/6/4
 *
 * @author: wulinfeng
 */
public interface SqlInjectConstants extends Constants {

    String SET_CONDITION = "st";
    String SET_CONDITION_DOT = SET_CONDITION + DOT;
    String JOIN = "join";
    String JOIN_LEFT = "left" + SPACE + JOIN;
    String JOIN_RIGHT = "right" + SPACE + JOIN;

}
