package com.landregistry.workflow;

import com.landregistry.model.Parcel;
import com.landregistry.model.User;
import com.landregistry.repository.ParcelRepository;
import com.landregistry.repository.UserRepository;
import com.landregistry.service.MutationWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EndToEndMutationWorkflowTest {

    @Autowired
    private MutationWorkflowService workflowService;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.landregistry.blockchain.Web3jBlockchainService blockchainService;

    @Test
    @Transactional
    public void testEndToEndTransferWorkflow() {
        blockchainService.setMode("SIMULATION");

        // 1. Verify Seeded Data
        User alice = userRepository.findByLogin("alice").orElseThrow();
        User bob = userRepository.findByLogin("bob").orElseThrow();
        User clara = userRepository.findByLogin("clara").orElseThrow(); // Registrar

        Parcel parcel = parcelRepository.findByParcelCode("P-101").orElseThrow();
        assertEquals("u-alice", parcel.getOwner().getId());
        assertEquals(1, parcel.getVersion());

        // 2. Submit Transfer Request
        var mutation = workflowService.submitMutation(
                "TRANSFER",
                List.of(parcel.getId()),
                Map.of(parcel.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );

        assertNotNull(mutation.getId());
        assertEquals("SUBMITTED", mutation.getStatus());

        // 3. Evaluate Mutation
        Map<String, Object> evalResult = workflowService.evaluateMutation(mutation.getId());
        assertNotNull(evalResult.get("certificateHash"));
        assertEquals("READY_FOR_COMMIT", evalResult.get("status"));
        assertEquals("AUTHORIZE", evalResult.get("decision"));

        // 4. Commit to Blockchain
        Map<String, Object> commitResult = workflowService.commitMutationToBlockchain(mutation.getId(), clara);
        assertEquals("COMMITTED", commitResult.get("status"));
        assertNotNull(commitResult.get("txHash"));

        // 5. Verify Database State Projection
        Parcel updatedParcel = parcelRepository.findById(parcel.getId()).orElseThrow();
        assertEquals(bob.getId(), updatedParcel.getOwner().getId(), "Owner must be updated to Bob");
        assertEquals(2, updatedParcel.getVersion(), "Parcel version must be incremented to 2");
        assertNotNull(updatedParcel.getBlockchainTxHash());
    }
}
