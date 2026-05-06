package io.swagger.oas.inflector.config;

import io.swagger.v3.core.filter.OpenAPISpecFilter;

/**
 * Process-wide holder for an {@link OpenAPISpecFilter}.
 *
 * <p>Set a filter before the application starts to transform the resolved OpenAPI spec
 * before it is returned by the spec endpoint or used for validation. At most one filter
 * is active at a time; passing {@code null} disables filtering.
 */
public class FilterFactory {
    protected static OpenAPISpecFilter FILTER = null;

    /**
     * Returns the currently registered filter, or {@code null} if none has been set.
     *
     * @return the active {@link OpenAPISpecFilter}
     */
    public static OpenAPISpecFilter getFilter() {
        return FILTER;
    }

    /**
     * Registers a filter to be applied to the resolved spec.
     *
     * @param filter the filter to use, or {@code null} to disable filtering
     */
    public static void setFilter(OpenAPISpecFilter filter) {
        FILTER = filter;
    }
}