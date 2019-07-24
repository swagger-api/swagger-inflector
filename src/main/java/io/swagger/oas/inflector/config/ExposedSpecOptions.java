package io.swagger.oas.inflector.config;

import io.swagger.v3.parser.core.models.ParseOptions;

public class ExposedSpecOptions {
    private ParseOptions parseOptions;
    private boolean hideInflectorExtensions = true;

    public ExposedSpecOptions() {
        this.parseOptions = new ParseOptions();
    }

    public ParseOptions getParseOptions() {
        return parseOptions;
    }

    public void setParseOptions(ParseOptions parseOptions) {
        this.parseOptions = parseOptions;
    }

    public boolean isHideInflectorExtensions() {
        return hideInflectorExtensions;
    }

    public void setHideInflectorExtensions(boolean hideInflectorExtensions) {
        this.hideInflectorExtensions = hideInflectorExtensions;
    }
}
