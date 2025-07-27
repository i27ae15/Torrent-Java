package Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;

import Parser.Exceptions.FileReadingException;
import coder.Decoder;

public class TorrentFile {

    private String fileStringContent = null;
    private String fileName = null;

    private HashMap<String, Object> fileInfo;
    private HashMap<String, Object> info;
    private String infoHash;
    private byte[] infoHashBytes;

    // Constructors

    public TorrentFile(String fileName) {
        this.fileName = fileName;

        this.setFileInfo();
    }

    // Getters

    public String getStringFileContent() { return this.fileStringContent; }
    public String getInfoHash() { return this.infoHash; }
    public byte[] getInfoHashBytes() { return this.infoHashBytes; }

    public HashMap<String, Object> getFileInfo() { return this.fileInfo; }
    public HashMap<String, Object> getFileInsideInfo() { return this.info; }

    @SuppressWarnings("unchecked")
    private void setFileInfo() {

        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            throw new FileReadingException(fileName);
        }

        this.fileStringContent = new String(fileBytes, StandardCharsets.ISO_8859_1);

        this.fileInfo = (HashMap<String, Object>) Decoder.decodeBencode(this.fileStringContent);
        this.info = (HashMap<String, Object>) fileInfo.get("info");

        this.setHashing();

    }

    private void setHashing() {

        String infoKey = "4:info";
        int infoStartIndex = this.fileStringContent.indexOf(infoKey) + infoKey.length();

        // 2. Extract the raw bencoded dictionary.
        String bencodedInfoString = this.fileStringContent.substring(
            infoStartIndex, this.fileStringContent.length() - 1
        );
        byte[] bencodedInfoBytes = bencodedInfoString.getBytes(StandardCharsets.ISO_8859_1);

        // 4. Calculate the hash of the original bytes.
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("ERROR DIGESTING THE FILE: " + e.getMessage());
            return;
        }

        this.infoHashBytes = digest.digest(bencodedInfoBytes);
        this.infoHash = HexFormat.of().formatHex(infoHashBytes);

    }

    public void printFileInfo() {

        // Read the file as raw bytes to avoid any character encoding issues.
        System.out.println("Tracker URL: " + this.fileInfo.get("announce"));
        System.out.println("Length: " + this.info.get("length"));
        System.out.println("Info Hash: " + this.infoHash);
        System.out.println("Piece Length: " + this.info.get("piece length"));

        String piecesString = (String) info.get("pieces");
        byte[] pieceHashesBytes = piecesString.getBytes(StandardCharsets.ISO_8859_1);
        String hexHashes = HexFormat.of().formatHex(pieceHashesBytes);

        System.out.println("Piece Hashes: " + hexHashes);

    }

}
