package com.dfsa.blockchain;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BlockchainIntegrityService {

    private final List<Block> chain = Collections.synchronizedList(new ArrayList<>());

    public BlockchainIntegrityService() {
        // Create Genesis Block (First block in chain of custody)
        Block genesisBlock = new Block(0, "GENESIS_CASE", "0000000000000000000000000000000000000000000000000000000000000000", "0");
        chain.add(genesisBlock);
    }

    /**
     * Computes raw SHA-256 hash of an uploaded file stream
     */
    public String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream();
                 DigestInputStream dis = new DigestInputStream(is, digest)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {}
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate SHA-256 file hash", e);
        }
    }

    /**
     * Appends a new verified evidence record to the ledger
     */
    public synchronized Block sealEvidence(String caseId, String fileHash) {
        Block previousBlock = chain.get(chain.size() - 1);
        Block newBlock = new Block(chain.size(), caseId, fileHash, previousBlock.getHash());
        chain.add(newBlock);
        return newBlock;
    }

    /**
     * Verifies that the chain of custody has not been tampered with
     */
    public synchronized boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Verify current block hash authenticity
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            // Verify unbroken linkage with previous block
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }
        return true;
    }

    public List<Block> getAllBlocks() {
        return new ArrayList<>(chain);
    }
}