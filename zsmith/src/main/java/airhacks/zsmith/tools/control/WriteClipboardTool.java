package airhacks.zsmith.tools.control;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import org.json.JSONObject;

public class WriteClipboardTool implements Tool {

    @Override
    public String toolName() {
        return "write_clipboard";
    }

    @Override
    public String description() {
        return "Writes text content to the system clipboard";
    }

    enum Field { text }

    @Override
    public JSONObject inputSchema() {
        return Tool.schema(Prop.string(Field.text, "The text to write to the clipboard"));
    }

    @Override
    public String execute(JSONObject input) {
        try {
            var text = input.getString(Field.text.name());
            var selection = new StringSelection(text);
            var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            return "Text written to clipboard";
        } catch (Exception e) {
            return "Error writing to clipboard: " + e.getMessage();
        }
    }
}
