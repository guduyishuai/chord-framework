package com.chord.framework.mybatis.runtime.statement.injector.template.insert;

import com.chord.framework.mybatis.runtime.Constants;
import com.chord.framework.mybatis.runtime.KeyResolver;
import com.chord.framework.mybatis.runtime.MappedStatementInfo;
import com.chord.framework.mybatis.runtime.StatementGenerator;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created on 2020/6/12
 *
 * @author: wulinfeng
 */
public class InsertTemplateMappedStatementGenerator implements StatementGenerator<MappedStatementInfo> {

    private static final Logger logger = LoggerFactory.getLogger(InsertTemplateMappedStatementGenerator.class);

    private VelocityEngine velocityEngine;

    private String templatePath;

    private String namespace;

    private String id;

    private Map<String, Object> model;

    private KeyResolver keyResolver;

    private String databaseId;

    private Configuration configuration;

    private LanguageDriver languageDriver;

    public InsertTemplateMappedStatementGenerator(String templatePath,
                                                  String namespace,
                                                  String id,
                                                  Map<String, Object> model,
                                                  KeyResolver keyResolver,
                                                  String databaseId,
                                                  Configuration configuration,
                                                  LanguageDriver languageDriver,
                                                  VelocityEngine velocityEngine) {
        this.id = id;
        this.namespace = namespace;
        this.templatePath = templatePath;
        this.model = model;
        this.keyResolver = keyResolver;
        this.databaseId = databaseId;
        this.configuration = configuration;
        this.languageDriver = languageDriver;
        this.velocityEngine = velocityEngine;
    }

    @Override
    public MappedStatementInfo generate(Consumer<MappedStatementInfo> callback) {
        Template template = velocityEngine.getTemplate(templatePath, Constants.UTF8);
        StringWriter stringWriter = new StringWriter();
        template.merge(new VelocityContext(model), stringWriter);
        String sql = stringWriter.toString();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);
        logger.info("create mappedStatement {}: \n {}", namespace, sql);

        MappedStatementInfo mappedStatementInfo = new MappedStatementInfo(namespace, id, sqlSource, keyResolver.getKeyGenerator(), keyResolver.getKeyProperty(model), keyResolver.getKeyColumn(model), databaseId);

        callback.accept(mappedStatementInfo);

        return mappedStatementInfo;
    }

}
