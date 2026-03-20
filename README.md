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

## Running the Examples

```bash
zb.sh
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/MeetingPlannerExample.java
```

The `UserConfirmationExample` demonstrates interactive yes/no confirmation prompts before the agent proceeds with actions:

```bash
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/UserConfirmationExample.java
```

The `EpisodicMemoryExample` demonstrates persistent long-term memory across conversations:

```bash
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/EpisodicMemoryExample.java
```

The `SkillsExample` demonstrates loading and using skills — reusable prompt instructions the agent can activate on demand:

```bash
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/SkillsExample.java
```

## Skills

Agents can load skills — reusable prompt snippets stored as `SKILL.md` files in a directory structure:

```
skills/
  explain/
    SKILL.md
  code-reviewer/
    SKILL.md
  java-modernizer/
    SKILL.md
```

Default skill resolution (each layer overrides the previous):

1. `~/.zsmith/skills/` — global skills
2. `~/.zsmith/[agentName]/skills/` — global agent-specific
3. `./skills/` — local project skills
4. `./[agentName]/skills/` — local agent-specific

```java
var agent = new Agent()
        .withSkills();
```

Custom skill directory:

```java
var agent = new Agent()
        .withSkills("path/to/skills");
```

Each `SKILL.md` uses frontmatter for metadata:

```markdown
---
name: explain
description: Explains a concept using an analogy and a short example
---
When explaining a concept:

1. Start with a one-sentence definition
2. Give an everyday analogy
3. Show a minimal code example (if applicable)
4. End with one common misconception

Keep it under 10 sentences total.
```

## Episodic Memory

Agents can store and recall information across conversations using `EpisodicMemoryStore`.

Agent-specific memory (stored at `~/.zsmith/[agentName]/memory/episodic-memory.json`):

```java
var agent = new Agent("planner")
        .withEpisodicMemory();
```

Shared memory across all agents (stored at `~/.zsmith/memory/episodic-memory.json`):

```java
var agent = new Agent("planner")
        .withSharedEpisodicMemory();
```

Custom storage location:

```java
var agent = new Agent()
        .withEpisodicMemory(new EpisodicMemoryStore(Path.of("custom-memory.json")));
```

Memories are persisted to a JSON file and classified by type:

- `user` — role, preferences, and knowledge about the user
- `feedback` — guidance or corrections from the user
- `project` — ongoing work, goals, decisions, or incidents
- `reference` — pointers to external resources and systems

## Built-in Tools

| Tool | Name | Description |
|------|------|-------------|
| `CalculatorTool` | `calculator` | Performs basic arithmetic operations: add, subtract, multiply, divide |
| `CurrentTimeTool` | `current_time` | Returns the current date and time |
| `ReadClipboardTool` | `read_clipboard` | Reads the current text content from the system clipboard |
| `ReadFileTool` | `read_file` | Reads the contents of a file within the sandbox directory |
| `WriteFileTool` | `write_file` | Writes content to a file within the sandbox directory |
| `LinkCheckerTool` | `check_link` | Checks a URL and returns response information including status code, content type, and body preview |
| `UserConfirmationTool` | `user_confirmation` | Asks the user a yes/no question and returns the answer |
| `StoreMemoryTool` | `store_memory` | Stores an episode in long-term memory for future recall |
| `RecallMemoryTool` | `recall_memory` | Recalls past memories, optionally filtered by type or limited to recent entries |

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
