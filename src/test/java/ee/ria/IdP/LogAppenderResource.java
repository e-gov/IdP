package ee.ria.IdP;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.rules.ExternalResource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogAppenderResource extends ExternalResource {
    private org.apache.logging.log4j.core.Logger logger;

    public LogAppenderResource(String loggerName) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        logger = loggerContext.getLogger(loggerName);
        loggerContext.getConfiguration().addLoggerAppender(logger, SimpleTestAppender.createAppender("testAppender", null, null, null));
    }

    @Override
    protected void before() {
    }

    @Override
    protected void after() {
        SimpleTestAppender.events.clear();
    }

    public List<LogEvent> getOutput() {
        return SimpleTestAppender.events;
    }

    @Plugin(name = "SimpleTestAppender", category = "Core")
    static class SimpleTestAppender extends AbstractAppender {
        public static List<LogEvent> events = new ArrayList<>();

        protected SimpleTestAppender(String name, org.apache.logging.log4j.core.Filter filter, org.apache.logging.log4j.core.Layout<? extends Serializable> layout, boolean ignoreExceptions) {
            super(name, filter, layout, ignoreExceptions);
        }

        @Override
        public void append(LogEvent e) {
            events.add(e);
        }

        @PluginFactory
        public static SimpleTestAppender createAppender(
                @PluginAttribute("name") String name,
                @PluginElement("Layout") org.apache.logging.log4j.core.Layout<? extends Serializable> layout,
                @PluginElement("Filter") final org.apache.logging.log4j.core.Filter filter,
                @PluginAttribute("otherAttribute") String otherAttribute) {
            if (name == null) {
                LOGGER.error("No name provided for SimpleTestAppender");
                return null;
            }
            if (layout == null) {
                layout = PatternLayout.createDefaultLayout();
            }
            return new SimpleTestAppender(name, filter, layout, true);
        }
    }
}

