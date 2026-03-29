package airhacks.zsmith.tools.boundary;

import java.util.List;

import airhacks.zsmith.tools.control.Tool;

public interface ToolProfiles {
    List<Tool> USER_IO = List.of(Tools.USER_MESSAGE, Tools.USER_QUESTION, Tools.USER_CONFIRMATION);
    List<Tool> CLIPBOARD = List.of(Tools.READ_CLIPBOARD, Tools.WRITE_CLIPBOARD);
    List<Tool> ALL = List.of(Tools.values());
}
