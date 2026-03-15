package airhacks.zsmith.tools.boundary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SandboxedFileSystem {

    Path rootDirectory;

    public SandboxedFileSystem(Path rootDirectory) {
        this.rootDirectory = rootDirectory.toAbsolutePath().normalize();
    }

    Path resolve(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (relativePath.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (relativePath.startsWith("/") || (relativePath.length() >= 2 && relativePath.charAt(1) == ':')) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (containsDotDot(relativePath)) {
            throw new IllegalArgumentException("Invalid path");
        }
        var resolved = this.rootDirectory.resolve(relativePath).normalize();
        if (!resolved.startsWith(this.rootDirectory)) {
            throw new IllegalArgumentException("Invalid path");
        }
        if (Files.exists(resolved) && Files.isSymbolicLink(resolved)) {
            try {
                var target = resolved.toRealPath();
                if (!target.startsWith(this.rootDirectory)) {
                    throw new IllegalArgumentException("Invalid path");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid path");
            }
        }
        return resolved;
    }

    public String readFile(String relativePath) {
        Path resolved;
        try {
            resolved = resolve(relativePath);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        try {
            return Files.readString(resolved);
        } catch (java.nio.file.NoSuchFileException e) {
            return "Error: File not found";
        } catch (IOException e) {
            return "Error: Could not read file";
        }
    }

    public void writeFile(String relativePath, String content) {
        Path resolved;
        try {
            resolved = resolve(relativePath);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        try {
            var parent = resolved.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(resolved, content);
        } catch (IOException e) {
            throw new RuntimeException("Error: Could not write file");
        }
    }

    private boolean containsDotDot(String path) {
        for (var segment : path.replace('\\', '/').split("/")) {
            if ("..".equals(segment)) {
                return true;
            }
        }
        return false;
    }
}
