package com.makila.domain.model;

import java.util.List;

public class AgentToolResult {
    public enum Status {
        SUCCESS,
        AMBIGUOUS,
        BLOCKED_GUARDRAIL,
        ERROR
    }

    private Status status;
    private String toolName;
    private String message;
    private Object data;
    private List<String> ambiguousOptions;

    public AgentToolResult() {}

    public AgentToolResult(Status status, String toolName, String message, Object data) {
        this.status = status;
        this.toolName = toolName;
        this.message = message;
        this.data = data;
    }

    public static AgentToolResult success(String toolName, String message, Object data) {
        return new AgentToolResult(Status.SUCCESS, toolName, message, data);
    }

    public static AgentToolResult blocked(String toolName, String reason) {
        return new AgentToolResult(Status.BLOCKED_GUARDRAIL, toolName, reason, null);
    }

    public static AgentToolResult ambiguous(String toolName, String message, List<String> options) {
        AgentToolResult result = new AgentToolResult(Status.AMBIGUOUS, toolName, message, null);
        result.setAmbiguousOptions(options);
        return result;
    }

    public static AgentToolResult error(String toolName, String message) {
        return new AgentToolResult(Status.ERROR, toolName, message, null);
    }

    // Getters y Setters
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public List<String> getAmbiguousOptions() { return ambiguousOptions; }
    public void setAmbiguousOptions(List<String> ambiguousOptions) { this.ambiguousOptions = ambiguousOptions; }
}

