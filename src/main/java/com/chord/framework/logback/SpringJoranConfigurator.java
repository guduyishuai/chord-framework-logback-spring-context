package com.chord.framework.logback;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import org.springframework.core.env.Environment;

/**
 *
 * 增加对SpringProfile的解析
 *
 * Created on 2020/11/12
 *
 * @author: wulinfeng
 */
public class SpringJoranConfigurator extends JoranConfigurator {

    private final Environment environment;

    public SpringJoranConfigurator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addInstanceRules(RuleStore rs) {
        super.addInstanceRules(rs);
        rs.addRule(new ElementSelector("*/springProfile"),
                new SpringProfileAction(this.environment));
        rs.addRule(new ElementSelector("*/springProfile/*"), new NOPAction());
    }

}
