package airhacks.zsmith.tools.control;

import org.json.JSONObject;

public class CalculatorTool implements Tool {

    @Override
    public String toolName() {
        return "calculator";
    }

    @Override
    public String description() {
        return "Performs basic arithmetic operations: add, subtract, multiply, divide";
    }

    @Override
    public String inputSchema() {
        return Tool.schema(
                Prop.stringEnum("operation", "The arithmetic operation to perform",
                        "add", "subtract", "multiply", "divide"),
                Prop.number("a", "First operand"),
                Prop.number("b", "Second operand")
        );
    }

    @Override
    public String execute(JSONObject input) {
        var operation = input.getString("operation");
        var a = input.getDouble("a");
        var b = input.getDouble("b");

        var result = switch (operation) {
            case "add" -> a + b;
            case "subtract" -> a - b;
            case "multiply" -> a * b;
            case "divide" -> a / b;
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };

        return String.valueOf(result);
    }
}
