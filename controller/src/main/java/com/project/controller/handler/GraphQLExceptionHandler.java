package com.project.controller.handler;

import com.project.controller.exception.CycleDetectedException;
import com.project.controller.exception.MultiComponentDetectedException;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

/**
 * Overriding default exception handler in order to insert custom message in GraphQL errors
 */
@Component
@Slf4j
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
//        todo: two errors returning
        log.debug("Handle error: {}", ex.toString());
        return switch (ex) {
            case CycleDetectedException cycleEx -> buildCustomMessage(ex, ErrorType.ValidationError);
            case MultiComponentDetectedException componentEx -> buildCustomMessage(ex, ErrorType.ValidationError);
            default -> super.resolveToSingleError(ex, env);
        };
    }

    private GraphQLError buildCustomMessage(Throwable ex, ErrorType errorType) {
        return GraphQLError.newError().message(ex.getMessage()).errorType(errorType).build();
    }


}
