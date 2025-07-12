package Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TorrentFile {

    public static String getFileInfo(String fileName) {

        File file = new File(fileName);
        StringBuilder fileContent = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file)) {
            int byteValue;

            while ((byteValue = fis.read()) != -1) {
                byte b = (byte) byteValue;
                fileContent.append((char) b);
            }

        } catch (IOException e) {
            System.err.println("ERROR READING FILE : " + fileName);
        }

        return fileContent.toString();

    }

}
