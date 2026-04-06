# zsmith

Zero-dependency AI agent framework for Claude with tool execution, SKILL.md and agentic loop support. No external libraries — only the Java standard library and a bundled JSON parser (`org.json`).

![zsmith](zsmith.png)

## Requirements

- **Java 25+** — uses implicit classes, text blocks, records, and source-file mode
- **Anthropic API key** — set `anthropic.api.key` in `~/.zsmith/app.properties` or as a system property
- **Build with zb** — run `./zb.sh` to produce `zbo/zsmith.jar` (no Maven/Gradle needed)

## Usage

```java
var agent = new Agent("calculator", "You are a helpful assistant.")
        .withTools(new CalculatorTool(), new CurrentTimeTool());

var response = agent.chat("What is 42 * 17?");
```

Autonomous execution without user input:

```java
var agent = new Agent("reporter", "Summarize today's tasks.")
        .withTools(new ReadFileTool(), new CurrentTimeTool());

var response = agent.act();
```

## Tool Profiles

Predefined tool groupings for common use cases:

```java
var agent = new Agent("assistant")
        .withUserIOTools()   // user_message, user_question, user_confirmation
        .withFileIOTools()   // read_file, write_file, list_files, read_any_file
        .withAllTools();     // all available tools
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

```bash
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/UserConfirmationExample.java
```

```bash
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/EpisodicMemoryExample.java
```

```bash
java -cp zbo/zsmith.jar src/test/java/airhacks/zsmith/SkillsExample.java
```

## Java Script Usage

zsmith agents can run as standalone Java scripts using source-file mode — no build tool, no compilation step:

```bash
./src/test/java/airhacks/zsmith/userConfirmationExample
```

The script uses a shebang to reference `zbo/zsmith.jar` directly, so build first with `./zb.sh`. Example script:

```java
#!/usr/bin/java --class-path=../../../../../zbo/zsmith.jar --source 25

// Requires zbo/zsmith.jar — build first with: ./zb.sh

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.logging.control.Log;
import airhacks.zsmith.tools.boundary.Tools;

void main() {

        var agent = new Agent()
                .withSystemPrompt("""
                        You are a helpful assistant with access to tools.
                        Use the user_confirmation tool to ask the user yes/no questions before proceeding with actions.
                        Be concise in your responses.
                        """)
                .withTool(Tools.USER_CONFIRMATION);

        Log.agent("Agent initialized with user_confirmation tool");

        var question = "I want to create a HelloWorld.java example. Can you help?";
        Log.prompt("User: " + question);

        var response = agent.chat(question);
        Log.answer("Agent: " + response);

}
```

No package declaration, no class wrapper — Java 25 implicit classes keep the script minimal. Install system-wide by copying the jar and script to a PATH directory, adjusting the `--class-path` accordingly.

## Skills

Skills are reusable prompt snippets stored as `SKILL.md` files. Each skill uses frontmatter for metadata:

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

## Episodic Memory

Agents store and recall information across conversations using `EpisodicMemoryStore`. Memories are persisted to a JSON file and classified by type: `user`, `feedback`, `project`, `reference`.

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

## Built-in Tools

| Tool | Name | Description |
|------|------|-------------|
| `CalculatorTool` | `calculator` | Performs basic arithmetic operations: add, subtract, multiply, divide |
| `CurrentTimeTool` | `current_time` | Returns the current date and time |
| `ReadClipboardTool` | `read_clipboard` | Reads text content from the system clipboard |
| `WriteClipboardTool` | `write_clipboard` | Writes text content to the system clipboard |
| `ReadFileTool` | `read_file` | Reads the contents of a file within the sandbox directory |
| `WriteFileTool` | `write_file` | Writes content to a file within the sandbox directory |
| `ListFilesTool` | `list_files` | Lists all files within the sandbox directory |
| `ReadAnyFileTool` | `read_any_file` | Reads a file from any location on the filesystem after user confirmation |
| `LinkCheckerTool` | `check_link` | Checks a URL and returns response information including status code, content type, and body preview |
| `UserConfirmationTool` | `user_confirmation` | Asks the user a yes/no question and returns the answer |
| `UserQuestionTool` | `user_question` | Asks the user a question and returns the typed answer |
| `UserMessageTool` | `user_message` | Presents a message to the user |
| `StoreMemoryTool` | `store_memory` | Stores an episode in long-term memory for future recall |
| `RecallMemoryTool` | `recall_memory` | Recalls past memories, optionally filtered by type or limited to recent entries |
| `LoadSkillTool` | `load_skill` | Loads a skill by name (added automatically with `withSkills()`) |

## Custom Tools

Implement the `Tool` interface. Use `Tool.Prop` with an enum for type-safe field names and `Tool.schema()` to define the input schema:

```java
public class MyTool implements Tool {

    enum Field { param, count }

    public String toolName() {
        return "my_tool";
    }

    public String description() {
        return "Does something useful";
    }

    public String inputSchema() {
        return Tool.schema(
            Prop.string(Field.param, "Parameter description"),
            Prop.integer(Field.count, "How many times").optional()
        );
    }

    public String execute(JSONObject input) {
        return "Result: " + input.getString(Field.param.name());
    }
}
```

Available `Prop` types: `string`, `stringEnum` (with allowed values), `number`, `integer`. Any prop can be marked `.optional()`.

---

Powered by [airhacks.live](https://airhacks.live)
