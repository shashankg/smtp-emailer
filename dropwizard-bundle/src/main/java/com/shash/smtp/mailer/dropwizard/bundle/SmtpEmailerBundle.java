package com.shash.smtp.mailer.dropwizard.bundle;

import com.shash.smtp.mailer.emailer.Emailer;
import com.shash.smtp.mailer.emailer.config.SmtpConfig;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;

/**
 * @author by shashank.g
 */
public abstract class SmtpEmailerBundle<T extends Configuration> implements ConfiguredBundle<T> {

    @Getter
    private Emailer emailer;

    @Override
    public void run(final T config, final Environment environment) throws Exception {
        final SmtpConfig smtpConfig = getSmtpConfig(config);
        emailer = new Emailer(smtpConfig);
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        // nothing to do here
    }

    protected abstract SmtpConfig getSmtpConfig(final T config);
}