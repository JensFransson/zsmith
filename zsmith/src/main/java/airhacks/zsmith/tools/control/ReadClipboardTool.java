package airhacks.zsmith.tools.control;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import org.json.JSONObject;

public class ReadClipboardTool implements Tool {

    @Override
    public String toolName() {
        return "read_clipboard";
    }

    @Override
    public String description() {
        return "Reads the current text content from the system clipboard";
    }

    @Override
    public String inputSchema() {
        return """
                {
                    "type": "object",
                    "properties": {}
                }
                """;
    }

    @Override
    public String execute(JSONObject input) {
        try {
            var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            var contents = clipboard.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
            return "Clipboard is empty or does not contain text";
        } catch (Exception e) {
            return "Error reading clipboard: " + e.getMessage();
        }
    }
}
