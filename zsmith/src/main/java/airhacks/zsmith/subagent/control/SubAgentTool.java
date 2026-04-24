package airhacks.zsmith.subagent.control;

import org.json.JSONObject;

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tools.control.Tool;

public class SubAgentTool implements Tool {

    static final int DEFAULT_MAX_DEPTH = 3;
    static final ScopedValue<Integer> DEPTH = ScopedValue.newInstance();

    private final Agent subAgent;
    private final String name;
    private final String toolDescription;
    private final int maxDepth;
    private final boolean runParallel;

    public SubAgentTool(Agent subAgent, String name, String description, int maxDepth, boolean parallel) {
        this.subAgent = subAgent;
        this.name = name;
        this.toolDescription = description;
        this.maxDepth = maxDepth;
        this.runParallel = parallel;
    }

    public SubAgentTool(Agent subAgent, String name, String description, int maxDepth) {
        this(subAgent, name, description, maxDepth, true);
    }

    public SubAgentTool(Agent subAgent, String name, String description) {
        this(subAgent, name, description, DEFAULT_MAX_DEPTH);
    }

    public SubAgentTool(Agent subAgent, boolean parallel) {
        this(subAgent,
                "delegate_to_" + subAgent.name(),
                "Delegates a task to the '%s' sub-agent. Send a clear, complete task description and the sub-agent will work on it independently and return the result."
                        .formatted(subAgent.name()),
                DEFAULT_MAX_DEPTH,
                parallel);
    }

    public SubAgentTool(Agent subAgent) {
        this(subAgent, true);
    }

    @Override
    public String toolName() {
        return this.name;
    }

    @Override
    public String description() {
        return this.toolDescription;
    }

    enum Field {
        task
    }

    @Override
    public String inputSchema() {
        return Tool.schema(
                Prop.string(Field.task, "The task to delegate to the sub-agent. Be specific and complete."));
    }

    @Override
    public boolean parallel() {
        return this.runParallel;
    }

    @Override
    public String execute(JSONObject input) {
        var currentDepth = DEPTH.orElse(0);
        if (currentDepth >= this.maxDepth) {
            return "Error: Maximum sub-agent depth (%d) exceeded".formatted(this.maxDepth);
        }
        var task = input.getString(Field.task.name());
        Log.subagent("delegating to sub-agent '%s': %s".formatted(this.subAgent.name(), task));
        try {
            var result = ScopedValue.where(DEPTH, currentDepth + 1)
                    .call(() -> this.subAgent.chat(task));
            Log.subagent("sub-agent '%s' completed".formatted(this.subAgent.name()));
            return result;
        } catch (Exception e) {
            return "Error: Sub-agent '%s' failed: %s".formatted(this.subAgent.name(), e.getMessage());
        }
    }
}
