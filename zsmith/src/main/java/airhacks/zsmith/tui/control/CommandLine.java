package airhacks.zsmith.tui.control;

import java.util.Arrays;

@SuppressWarnings("unused")
public interface CommandLine {

    static int port(String[] args, int fallback) {
        return Arrays.stream(args)
                     .findFirst()
                     .map(Integer::parseInt)
                     .orElse(fallback);
    }
}
