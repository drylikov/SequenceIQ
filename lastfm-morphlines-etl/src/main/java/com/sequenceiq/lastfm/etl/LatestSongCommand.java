package com.sequenceiq.lastfm.etl;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.AbstractCommand;
import org.kitesdk.morphline.base.Fields;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

public class LatestSongCommand implements CommandBuilder {

    public static final String HIGHER = ">";
    public static final String LOWER = "<";
    public static final String EQUALS = "=";
    public static final ObjectMapper OBJECTMAPPER = new ObjectMapper();

    @Override
    public Collection<String> getNames() {
        return Collections.singletonList("latestSongs");
    }

    @Override
    public Command build(Config config, Command command, Command command2, MorphlineContext morphlineContext) {
        return new DateCheck(this, config, command, command2, morphlineContext);
    }

    private static final class DateCheck extends AbstractCommand {

        private final String fieldName;
        private final String operator;
        private final String pattern;

        public DateCheck(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder, config, parent, child, context);
            this.fieldName = getConfigs().getString(config, "field");
            this.operator = getConfigs().getString(config, "operator");
            this.pattern = getConfigs().getString(config, "pattern");
            LOG.debug("fieldName: {}", fieldName);
            validateArguments();
        }

        @Override
        protected boolean doProcess(Record record) {
            Map<String, String> attachmentBody = (Map<String, String>) record.get(Fields.ATTACHMENT_BODY).get(0);
            String fieldValue = attachmentBody.get(fieldName).toString();

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date fieldDate = sdf.parse(fieldValue);
                Date patternDate = sdf.parse(pattern + " 00:00:00");

                if (operator.equals(HIGHER)) {
                    if (!fieldDate.after(patternDate)) {
                        return true;
                    }
                } else if (operator.equals(LOWER)) {
                    if (!fieldDate.before(patternDate)) {
                        return true;
                    }
                } else if (operator.equals(EQUALS)) {
                    if (fieldDate.getYear() != patternDate.getYear()
                            || fieldDate.getMonth() != patternDate.getMonth()
                            || fieldDate.getDay() != patternDate.getDay()) {
                        return true;
                    }
                } else {
                    LOG.info("bad operator syntax");
                }
            } catch (Exception e) {
                LOG.info("parse exception: " + e.getMessage());
                return false;
            }
            record.removeAll(Fields.ATTACHMENT_BODY);
            try {
                record.put(Fields.MESSAGE, OBJECTMAPPER.writeValueAsString(attachmentBody));
            } catch (JsonProcessingException e) {
                LOG.info("parse exception: " + e.getMessage());
                return false;
            }
            return super.doProcess(record);
        }

        @Override
        protected void doNotify(Record notification) {
            LOG.debug("myNotification: {}", notification);
            super.doNotify(notification);
        }

    }
}
