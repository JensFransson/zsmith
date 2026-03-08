# zsmith

![zsmith](zsmith.png)

AI agent framework for Claude with tool execution support.

## Usage

```java
var agent = new Agent("calculator", "You are a helpful assistant.")
        .withTool(new CalculatorTool())
        .withTool(new CurrentTimeTool());

var response = agent.chat("What is 42 * 17?");
```

## Configuration

### Properties

Loaded in order (each layer overrides the previous):

1. `~/.zsmith/app.properties` — global defaults
2. `./app.properties` — local project defaults
3. `~/.zsmith/[agentName]/app.properties` — global agent-specific
4. `./[agentName]/app.properties` — local agent-specific
5. System properties — highest priority

Only keys present in later files override earlier values; other keys are preserved.

### System Prompt

Loaded from `system.prompt` files in order (each layer overrides the previous):

1. `~/.zsmith/[agentName]/system.prompt` — global agent-specific
2. `./[agentName]/system.prompt` — local agent-specific
3. `./system.prompt` — highest priority
4. Constructor parameter — fallback if no file exists

## Running the Example

```bash
zb.sh
java -cp zbo/app.jar src/test/java/airhacks/zsmith/MeetingPlannerExample.java
```

## Custom Tools

Implement the `Tool` interface:

```java
public class MyTool implements Tool {

    public String name() {
        return "my_tool";
    }

    public String description() {
        return "Does something useful";
    }

    public String inputSchema() {
        return """
            {
                "type": "object",
                "properties": {
                    "param": { "type": "string", "description": "Parameter description" }
                },
                "required": ["param"]
            }
            """;
    }

    public String execute(JSONObject input) {
        return "Result: " + input.getString("param");
    }
}
```
