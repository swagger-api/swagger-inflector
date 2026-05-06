package io.swagger.oas.inflector.config;

import io.swagger.v3.parser.core.models.ParseOptions;

/**
 * Controls how the resolved OpenAPI spec is exposed at the {@code /openapi.json} / {@code /openapi.yaml} endpoint.
 *
 * <ul>
 *   <li>{@link #hideInflectorExtensions} — strips {@code x-swagger-router-*} extensions from the published spec</li>
 *   <li>{@link #useOriginalNotParsed} — serves the raw spec bytes instead of the re-serialized parsed model</li>
 *   <li>{@link #mergeRootPath} — prepends the servlet context root path to all spec paths</li>
 * </ul>
 */
public class ExposedSpecOptions {
    private ParseOptions parseOptions;
    private boolean hideInflectorExtensions = true;
    private boolean useOriginalNotParsed = false;
    private boolean mergeRootPath = true;

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

    public boolean isUseOriginalNotParsed() {
        return useOriginalNotParsed;
    }

    public void setUseOriginalNotParsed(boolean useOriginalNotParsed) {
        this.useOriginalNotParsed = useOriginalNotParsed;
    }

    public boolean isMergeRootPath() {
        return mergeRootPath;
    }

    public void setMergeRootPath(boolean mergeRootPath) {
        this.mergeRootPath = mergeRootPath;
    }
}
