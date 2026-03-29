package airhacks.zsmith.tools.boundary;

import java.util.List;

import airhacks.zsmith.tools.control.ListFilesTool;
import airhacks.zsmith.tools.control.ReadFileTool;
import airhacks.zsmith.tools.control.Tool;
import airhacks.zsmith.tools.control.WriteFileTool;

/**
 * Predefined groupings of {@link Tool}s for common agent capabilities.
 *
 * <p>Separates the concern of <em>which tools exist</em> ({@link Tools}) from
 * <em>which tools belong together for a given use case</em>. Agents compose
 * capabilities by selecting a profile rather than cherry-picking individual
 * tools, keeping the wiring in {@link airhacks.zsmith.agent.boundary.Agent}
 * intention-revealing (e.g. {@code agent.withTools(ToolProfiles.USER_IO)}).
 *
 * <p>Implemented as an interface with constant fields so it acts as a
 * pure namespace — no instantiation, no state, just curated lists.
 */
public interface ToolProfiles {
    List<Tool> USER_IO = List.of(Tools.USER_MESSAGE, Tools.USER_QUESTION, Tools.USER_CONFIRMATION);
    List<Tool> CLIPBOARD = List.of(Tools.READ_CLIPBOARD, Tools.WRITE_CLIPBOARD);
    List<Tool> FILE_IO = List.of(new ReadFileTool(), new WriteFileTool(), new ListFilesTool());
    List<Tool> ALL = List.of(Tools.values());
}
