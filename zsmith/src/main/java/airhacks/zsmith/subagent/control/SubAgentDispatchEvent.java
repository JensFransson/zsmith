package airhacks.zsmith.subagent.control;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("airhacks.zsmith.subagent.Dispatch")
@Label("Sub-Agent Dispatch")
@Category({"zsmith", "subagent"})
@Description("Single delegation of a task to a sub-agent")
public class SubAgentDispatchEvent extends Event {

    @Label("Child Agent")
    String childAgent;

    @Label("Mode")
    String mode;

    @Label("Depth")
    int depth;

    @Label("First Run")
    boolean firstRun;

    @Label("Outcome")
    String outcome;

    @Label("Task Size")
    int taskSize;
}
