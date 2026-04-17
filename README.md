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

Agentic execution — `act()` sends `"go"` as the user message, letting the system prompt drive the task:

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
        .withFileIOTools()   // read_file, write_file, list_files, read_any_file (sandboxed)
        .withAllTools();     // calculator, current_time, clipboard, read_any_file,
                             // check_link, user_confirmation, user_message, user_question
```

`withAllTools()` includes all tools from the `Tools` enum. Sandboxed file tools (`read_file`, `write_file`, `list_files`) require `withFileIOTools()` because they need a configured `sandbox.path`.

### Launch App Tool

`withLaunchAppTool()` adds a config-driven tool that launches an external application and passes arguments to it:

```java
// Explicit configuration
var agent = new Agent("assistant")
        .withLaunchAppTool("run_tests", "Runs the test suite", "./run-tests.sh");

// From app.properties (launch.tool.name, launch.tool.description, launch.command)
var agent = new Agent("assistant")
        .withLaunchAppTool();
```

Configure in `app.properties`:

```properties
launch.tool.name=run_tests
launch.tool.description=Runs the test suite
launch.command=./run-tests.sh
```

## Configuration

### Required Properties

Add to `~/.zsmith/app.properties` or any properties file in the loading chain:

```properties
anthropic.api.key=sk-ant-...
anthropic.version=2023-06-01
```

The default model is `claude-opus-4-6`. Override via system property:

```bash
java -Dmodel=sonnet -cp zbo/zsmith.jar MyAgent.java
```

Partial matching works — `sonnet` resolves to `claude-sonnet-4-6`, `4-5` to `claude-opus-4-5-20251101`, etc.

### Properties Loading Order

Loaded in order (each layer overrides the previous):

1. `~/.zsmith/app.properties` — global defaults
2. `./app.properties` — local project defaults
3. `~/.zsmith/[agentName]/app.properties` — global agent-specific
4. `./[agentName]/app.properties` — local agent-specific
5. System properties — highest priority

Only keys present in later files override earlier values; other keys are preserved.

### Tool Permissions

Control which tools require user confirmation before execution. Three permission levels: `allow` (execute silently), `deny` (reject), `confirm` (ask user first). Default is `confirm`.

```properties
tools.permissions.default=confirm
tools.permissions.calculator=allow
tools.permissions.current_time=allow
tools.permissions.execute_script=confirm
tools.permissions.read_any_file=confirm
```

Agent-specific permissions in `~/.zsmith/[agentName]/app.properties` override global defaults:

```properties
# A trusted automation agent
tools.permissions.default=allow
tools.permissions.execute_script=confirm
```

### System Prompt

Loaded from `system.prompt` files in order (each layer overrides the previous):

1. `~/.zsmith/[agentName]/system.prompt` — global agent-specific
2. `./[agentName]/system.prompt` — local agent-specific
3. `./system.prompt` — highest priority

If no file is found, the constructor parameter is used as fallback.

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

## Subagents

Agents can delegate tasks to other agents via `withSubAgent()`. The child agent becomes a callable tool (`delegate_to_<name>`).

Podcast transcription example — the coordinator asks for the transcript path, reads the file, delegates link verification, stores guests and links in memory, and copies the result to the clipboard:

```java
var linkChecker = new Agent("link_checker", """
        You verify URLs. For each URL given, use the check_link tool
        to confirm it is reachable. Return a markdown status list.
        """)
        .withTool(Tools.LINK_CHECKER);

var transcriber = new Agent("transcriber", """
        You process podcast transcriptions.
        1. Ask the user for the transcript file path.
        2. Read the transcript.
        3. Extract all guest names and URLs mentioned.
        4. Delegate link verification to the link_checker agent.
        5. Store guests and verified links in memory.
        6. Write a summary with link status annotations to the clipboard.
        """)
        .withTools(Tools.USER_QUESTION, Tools.READ_ANY_FILE, Tools.WRITE_CLIPBOARD)
        .withSubAgent(linkChecker)
        .withEpisodicMemory();

var response = transcriber.act();
```

As a standalone Java script with shebang:

```java
#!/usr/bin/java --class-path=zbo/zsmith.jar --source 25

import airhacks.zsmith.agent.boundary.Agent;
import airhacks.zsmith.tools.boundary.Tools;

void main() {

        var linkChecker = new Agent("link_checker", """
                You verify URLs. For each URL given, use the check_link tool
                to confirm it is reachable. Return a markdown status list.
                """)
                .withTool(Tools.LINK_CHECKER);

        var transcriber = new Agent("transcriber", """
                You process podcast transcriptions.
                1. Ask the user for the transcript file path.
                2. Read the transcript.
                3. Extract all guest names and URLs mentioned.
                4. Delegate link verification to the link_checker agent.
                5. Store guests and verified links in memory.
                6. Write a summary with link status annotations to the clipboard.
                """)
                .withTools(Tools.USER_QUESTION, Tools.READ_ANY_FILE, Tools.WRITE_CLIPBOARD)
                .withSubAgent(linkChecker)
                .withEpisodicMemory();

        transcriber.act();
}
```

Custom tool name, description, and max delegation depth:

```java
var agent = new Agent("coordinator")
        .withTool(new SubAgentTool(linkChecker, "verify_links",
                "Verifies all URLs in the given text", 2));
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
| `ReadAnyFileTool` | `read_any_file` | Reads a file from any location on the filesystem |
| `LinkCheckerTool` | `check_link` | Checks a URL and returns response information including status code, content type, and body preview |
| `UserConfirmationTool` | `user_confirmation` | Asks the user a yes/no question and returns the answer |
| `UserQuestionTool` | `user_question` | Asks the user a question and returns the typed answer |
| `UserMessageTool` | `user_message` | Presents a message to the user |
| `StoreMemoryTool` | `store_memory` | Stores an episode in long-term memory for future recall |
| `RecallMemoryTool` | `recall_memory` | Recalls past memories, optionally filtered by type or limited to recent entries |
| `LoadSkillTool` | `load_skill` | Loads a skill by name (added automatically with `withSkills()`) |
| `ExecuteScriptTool` | `execute_script` | Executes a script and returns its output |
| `LaunchAppTool` | *(config-driven)* | Launches an external application with arguments (name, description, command from config or constructor) |

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

architecture by [bce.design](https://bce.design) | built by [zb](https://github.com/AdamBien/zb) | tested by [zunit](https://github.com/AdamBien/zunit) | skill provided by: [airails](https://airails.dev) | powered by [airhacks.live](https://airhacks.live)
