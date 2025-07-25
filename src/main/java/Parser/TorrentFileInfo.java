package Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;

import coder.Decoder;

public class TorrentFileInfo {

    public static void getInfo(String fileName) {

        // Read the file as raw bytes to avoid any character encoding issues.
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            System.err.println("ERROR READING FILE : " + fileName);
            return;
        }

        // For parsing, it's often easier to work with a string that preserves byte values.
        String fileContent = new String(fileBytes, StandardCharsets.ISO_8859_1);

        @SuppressWarnings("unchecked")
        HashMap<String, Object> fileInfo = (HashMap<String, Object>) Decoder.decodeBencode(fileContent);

        // System.out.println(fileContent);

        @SuppressWarnings("unchecked")
        HashMap<String, Object> info = (HashMap<String, Object>) fileInfo.get("info");

        System.out.println("Tracker URL: " + fileInfo.get("announce"));
        System.out.println("Length: " + info.get("length"));

        // --- HASHING THE ORIGINAL DATA ---

        String infoKey = "4:info";
        int infoStartIndex = fileContent.indexOf(infoKey) + infoKey.length();

        // 2. Extract the raw bencoded dictionary.
        String bencodedInfoString = fileContent.substring(infoStartIndex, fileContent.length() - 1);
        byte[] bencodedInfoBytes = bencodedInfoString.getBytes(StandardCharsets.ISO_8859_1);

        // 4. Calculate the hash of the original bytes.
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("ERROR DIGESTING THE FILE: " + e.getMessage());
            return;
        }
        byte[] hashBytes = digest.digest(bencodedInfoBytes);

        // 5. Convert the byte array to a hexadecimal string (your existing code for this is correct)
        StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String infoHash = hexString.toString();

        System.out.println("Info Hash: " + infoHash);
        System.out.println("Piece Length: " + info.get("piece length"));

        String piecesString = (String) info.get("pieces");
        byte[] pieceHashesBytes = piecesString.getBytes(StandardCharsets.ISO_8859_1);
        String hexHashes = HexFormat.of().formatHex(pieceHashesBytes);

        System.out.println("Piece Hashes: " + hexHashes);

    }

}
