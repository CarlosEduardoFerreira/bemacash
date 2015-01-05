package com.kaching123.tcr.print.builder;

/**
 * Created by vkompaniets on 11.03.14.
 */
public class DigitalReportItemWideBuilder extends DigitalReportItemBuilder {
    protected static final String BODY_STYLE_WIDE = "font-size:0.9em;width:700px;padding:20px;";

    protected static final String HEADER_WIDE =
            "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "</head>\n" +
                    _styled("body", BODY_STYLE_WIDE);

    @Override
    protected StringBuilder createBuilder() {
        return new StringBuilder(HEADER_WIDE);
    }
}
