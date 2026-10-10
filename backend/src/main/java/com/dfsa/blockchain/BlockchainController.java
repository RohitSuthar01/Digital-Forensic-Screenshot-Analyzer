package com.dfsa.blockchain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forensics/integrity")
@CrossOrigin(origins = "*")
public class BlockchainController {

    private final BlockchainIntegrityService integrityService;

    public BlockchainController(BlockchainIntegrityService integrityService) {
        this.integrityService = integrityService;
    }

    /**
     * Endpoint to seal an uploaded evidence file onto the immutable chain
     */
    @PostMapping("/seal")
    public ResponseEntity<Map<String, Object>> sealEvidence(
            @RequestParam("file") MultipartFile file,
            @RequestParam("caseId") String caseId) {

        String fileHash = integrityService.calculateFileHash(file);
        Block block = integrityService.sealEvidence(caseId, fileHash);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("caseId", caseId);
        response.put("fileHash", fileHash);
        response.put("blockIndex", block.getIndex());
        response.put("blockHash", block.getHash());
        response.put("timestamp", block.getTimestamp());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to verify blockchain ledger integrity
     */
    @GetMapping("/validate-chain")
    public ResponseEntity<Map<String, Object>> validateChain() {
        boolean isValid = integrityService.isChainValid();
        Map<String, Object> response = new HashMap<>();
        response.put("isChainValid", isValid);
        response.put("totalBlocks", integrityService.getAllBlocks().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve complete chain of custody ledger
     */
    @GetMapping("/ledger")
    public ResponseEntity<List<Block>> getLedger() {
        return ResponseEntity.ok(integrityService.getAllBlocks());
    }
}