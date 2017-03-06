package fiftyone.mobile.detection.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * file helpers
 */
public class FileHelper {
    /**
     * If only we could Use Files.toByteArray ...
     * @param filename the path to the file we want as bytes
     */
    protected static byte[] fileAsBytes(String filename) throws IOException {
        File file = new File(filename);
        byte fileContent[] = new byte[(int) file.length()];
        FileInputStream in = new FileInputStream(file);
        try {
            int bytesRead = 0;
            while (bytesRead < file.length()) {
                int b = in.read(fileContent);
                if (b == -1 && bytesRead != file.length()) {
                    throw new IllegalStateException("File not completely read");
                }
            }
        } finally {
            in.close();
        }
        return fileContent;
    }
}
