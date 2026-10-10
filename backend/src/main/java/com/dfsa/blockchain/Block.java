package com.dfsa.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class Block {
    private int index;
    private long timestamp;
    private String screenshotHash;
    private String caseId;
    private String previousHash;
    private String hash;
    private int nonce;

    public Block(int index, String caseId, String screenshotHash, String previousHash) {
        this.index = index;
        this.caseId = caseId;
        this.screenshotHash = screenshotHash;
        this.previousHash = previousHash;
        this.timestamp = Instant.now().toEpochMilli();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        try {
            String dataToHash = index + Long.toString(timestamp) + previousHash + screenshotHash + caseId + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getScreenshotHash() {
        return screenshotHash;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public int getNonce() {
        return nonce;
    }
}