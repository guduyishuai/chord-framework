# 简介
    `chord-framework`开源项目由个人业余时间编写，主要是提供各种框架的扩展，目前功能比较少，后续会逐渐加入模块。</br>
    本人会添加模块可能是比较随机的。</br>
    本框架类似spring-boot的结构，用户只需要引入相应的starter即可。

# 功能特性
## sentinel
- 配置信息通过nacos进行持久化
- 与nacos联动，双方均可修改配置，并进行联动
- 解决事务问题，nacos和sentinel有一方同步失败，则全部失败，不会造成数据不一致的问题
- 整合spring-cloud-gateway，sentinel关于网关的配置也同样可以和nacos联动
    
# 版本说明
目前基于的第三方依赖为：</br> 
- spring-cloud-alibaba 2.1.0.RELEASE
- spring-cloud 2.0.8.RELEASE
    
# POM依赖
`chord-framework`提供了一个bom，使用的时候可以引用该bom，从而防止依赖上的问题
``` xml
    <dependencyManagement>

        <dependencies>

            <dependency>
                <groupId>com.chord.framework</groupId>
                <artifactId>chord-framework-dependencies</artifactId>
                <version>${chord.framework.version}</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>

        </dependencies>

    </dependencyManagement>
```

# 使用说明
详细的使用说明请看项目的wiki页面
