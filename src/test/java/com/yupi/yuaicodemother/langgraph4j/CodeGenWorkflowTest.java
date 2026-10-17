package com.yupi.yuaicodemother.langgraph4j;

import org.bsc.langgraph4j.GraphRepresentation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CodeGenWorkflowTest {

    @Test
    void productionGraphUsesFixedVueTypeAndBoundedRetryPath() {
        String mermaid = new CodeGenWorkflow()
                .createWorkflow()
                .getGraph(GraphRepresentation.Type.MERMAID)
                .content();

        Assertions.assertFalse(mermaid.contains("router"));
        Assertions.assertTrue(mermaid.contains("retry_preparation"));
        Assertions.assertTrue(mermaid.contains("workflow_failure"));
        Assertions.assertTrue(mermaid.contains("project_builder"));
    }
}
