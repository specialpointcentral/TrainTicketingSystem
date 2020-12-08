package ticketingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TraceVerifyTest")
public class TraceVerifyTest {
    private static File workDir = new File("src/main/java/ticketingsystem/");
    private static File verifyDir = new File("src/test/java/verify/");
    private static boolean isWindows;

    @BeforeAll
    static void prepareFile() throws IOException, InterruptedException {
        String oldfile = verifyDir.getAbsolutePath() + "/Trace.java.copy";
        String newfile = workDir.getAbsolutePath() + "/Trace.java";
        copyFile(oldfile, newfile);
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        // compiler
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "javac -encoding UTF-8 -cp . ticketingsystem/Trace.java");
        } else {
            builder.command("sh", "-c", "javac -encoding UTF-8 -cp . ticketingsystem/Trace.java");
        }
        builder.directory(workDir.getParentFile());
        Process process = builder.start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
    }

    @BeforeEach
    void generateTraceFile() throws IOException, InterruptedException {
        File f = new File(verifyDir.getPath() + "/trace");
        if (f.exists()) {
            f.delete();
        }
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "java -cp . ticketingsystem/Trace > " + f.getAbsolutePath());
        } else {
            builder.command("sh", "-c", "java -cp . ticketingsystem/Trace > " + f.getAbsolutePath());
        }
        builder.directory(workDir.getParentFile());
        Process process = builder.start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
    }

    @Test
    void verifyTrace() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "java -jar verify.jar trace");
        } else {
            builder.command("sh", "-c", "java -jar verify.jar trace");
        }
        builder.directory(verifyDir);
        Process process = builder.start();
        InputStream in = process.getInputStream();
        BufferedReader read = new BufferedReader(new InputStreamReader(in));
        assertEquals("Verification Finished", read.readLine());
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
    }

    private static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("copy error!");
            e.printStackTrace();
        }
    }
}