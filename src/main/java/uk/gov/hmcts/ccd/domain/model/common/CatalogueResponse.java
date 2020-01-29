package uk.gov.hmcts.ccd.domain.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = CatalogueResponse.Builder.class)
public class CatalogueResponse<T> {

    private final String code;
    private final String message;
    private final T details;

    public CatalogueResponse() {
        this(null,null,null);
    }

    private CatalogueResponse(final String code, final String message, final T details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    private CatalogueResponse(final String code, final String message) {
        this(code,message,null);
    }

    public CatalogueResponse(final CatalogueResponseElement catalogueResponseElement, final T details) {
        this(catalogueResponseElement.getCode(), catalogueResponseElement.getMessage(), details);
    }

    public CatalogueResponse(final CatalogueResponseElement catalogueResponseElement) {
        this(catalogueResponseElement, null);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public T getDetails() {
        return details;
    }

    @JsonPOJOBuilder
    static class Builder<T> {
        String code;
        String message;
        T details;

        Builder<T> withCode(final String code) {
            this.code = code;
            return this;
        }

        Builder<T> withMessage(final String message) {
            this.message = message;
            return this;
        }

        Builder<T> withDetails(final T details) {
            this.details = details;
            return this;
        }

        public CatalogueResponse<T> build() {
            return new CatalogueResponse<>(code, message, details);
        }
    }
}
