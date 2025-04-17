package one.terenin.rpc.spec;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class EndpointSpec {

    private final String httpMethod;
    private final String uri;
    private final Function<?, ?> processingFunction;
    private final boolean extendedLogging;

    private EndpointSpec(String httpMethod,
                         String uri,
                         Function<?, ?> processingFunction,
                         boolean extendedLogging
    ) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.processingFunction = processingFunction;
        this.extendedLogging = extendedLogging;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUri() {
        return uri;
    }

    public Function<?, ?> getProcessingFunction() {
        return processingFunction;
    }

    public boolean isExtendedLogging() {
        return extendedLogging;
    }

    private static class Builder {
        private String httpMethod;
        private String uri;
        @Nullable
        private Function<Object[], Object[]> processingFunctionMM;
        @Nullable
        private Function<Object, Object> processingFunctionOO;
        @Nullable
        private Function<Object, Object[]> processingFunctionOM;
        @Nullable
        private Function<Object[], Object> processingFunctionMO;
        private boolean extendedLogging;

        public Builder get() {
            this.httpMethod = "GET";
            return this;
        }

        public Builder post() {
            this.httpMethod = "POST";
            return this;
        }

        public Builder put() {
            this.httpMethod = "PUT";
            return this;
        }

        public Builder patch() {
            this.httpMethod = "PATCH";
            return this;
        }

        public Builder delete() {
            this.httpMethod = "DELETE";
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public <T extends Function<?, ?>> Builder withProcessing(T procFunc, final boolean singleReq, final boolean singleResp) {

            if (singleReq && singleResp) {
                this.processingFunctionOO = (Function<Object, Object>) procFunc;
            } else if (!singleReq && !singleResp) {
                this.processingFunctionMM = (Function<Object[], Object[]>) procFunc;
            } else if (!singleReq) {
                this.processingFunctionMO = (Function<Object[], Object>) procFunc;
            } else {
                this.processingFunctionOM = (Function<Object, Object[]>) procFunc;
            }
            return this;
        }

        public Builder extendedLogging(boolean extendedLogging) {
            this.extendedLogging = extendedLogging;
            return this;
        }

        public EndpointSpec build() {

            return new EndpointSpec(httpMethod, uri, processingFunctionOO != null ? processingFunctionOO : processingFunctionMM != null ? processingFunctionMM : processingFunctionOM != null ? processingFunctionOM : processingFunctionMO, extendedLogging);
        }
    }

}
