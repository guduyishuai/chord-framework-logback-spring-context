# 场景
该项目用于在传统spring中使用logback的时候，可以使用SpringProfile标签

# 引入依赖
``` xml
  <dependency>
     <groupId>com.chord.framework</groupId>
     <artifactId>chord-framework-logback-spring-context</artifactId>
  </dependency>  
```

# 配置
注入相应的bean
``` xml
  <bean class="com.chord.framework.logback.LogbackInitialization"/>
```

在web.xml中配置profile
``` xml
<context-param>
	<param-name>spring.profiles.active</param-name>
	<param-value>dev</param-value>
</context-param>
```

# 使用
在logback的配置文件中使用SpringProfile标签   
需要注意的是，确保logback的配置文件名为logback-spring.xml
