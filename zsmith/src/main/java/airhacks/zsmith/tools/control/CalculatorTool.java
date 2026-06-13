package airhacks.zsmith.tools.control;

import org.json.JSONObject;

public interface CalculatorTool {

    enum Field { operation, a, b }

    static Tool create() {
        return Tool.of(
                "calculator",
                "Performs basic arithmetic operations: add, subtract, multiply, divide",
                Tool.schema(
                        Tool.Prop.stringEnum(Field.operation, "The arithmetic operation to perform",
                                "add", "subtract", "multiply", "divide"),
                        Tool.Prop.number(Field.a, "First operand"),
                        Tool.Prop.number(Field.b, "Second operand")),
                CalculatorTool::run);
    }

    private static String run(JSONObject input) {
        var operation = input.getString(Field.operation.name());
        var a = input.getDouble(Field.a.name());
        var b = input.getDouble(Field.b.name());

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
