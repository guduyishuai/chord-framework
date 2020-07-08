package com.chord.framework.mybatis.runtime.statement.injector.program;

/**
 * Created on 2020/6/4
 *
 * @author: wulinfeng
 */
public enum ExtraSqlMethod {

    /**
     * 修改
     */
    UPDATE_BY_MAP("updateByMap", "根据 map 条件，更新记录", "<script>\nUPDATE %s %s %s %s\n</script>"),

    /**
     * 联表查询
     */
    SELECT_JOIN_BY_MAP("selectJoinByMap", "根据 map 条件，联表查询", "<script>\nSELECT %s FROM %s %s %s\n</script>");

    private final String method;
    private final String desc;
    private final String sql;

    ExtraSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public String getSql() {
        return sql;
    }

}
