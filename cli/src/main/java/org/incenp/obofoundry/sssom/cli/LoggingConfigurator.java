/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.cli;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * Configures the logger for the SSSOM-CLI application. We want a simpler format
 * than Logback’s default one, and we only want our own messages.
 */
public class LoggingConfigurator extends ContextAwareBase implements Configurator {

    public LoggingConfigurator() {
    }

    @Override
    public ExecutionStatus configure(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<>();
        ca.setContext(context);
        ca.setName("console");
        ca.setTarget("System.err");

        PatternLayout layout = new PatternLayout();
        layout.setPattern("sssom-cli: %logger: %level: %msg%n");
        layout.setContext(context);
        layout.start();

        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(context);
        encoder.setLayout(layout);

        ca.setEncoder(encoder);
        ca.start();

        Logger sssomLogger = context.getLogger("org.incenp.obofoundry.sssom");
        sssomLogger.addAppender(ca);

        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }
}
