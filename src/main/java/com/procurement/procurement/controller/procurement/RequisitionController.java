// Requisition controller
package com.procurement.procurement.controller.procurement;

import com.procurement.procurement.entity.procurement.Requisition;
import com.procurement.procurement.repository.procurement.RequisitionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.procurement.procurement.entity.user.User;
import com.procurement.procurement.repository.user.UserRepository;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/procurement/requisition")
public class RequisitionController {

    private final RequisitionRepository requisitionRepository;
    private final UserRepository userRepository;

    public RequisitionController(RequisitionRepository requisitionRepository, UserRepository userRepository) {
        this.requisitionRepository = requisitionRepository;
        this.userRepository = userRepository;
    }

    // ===================== Create Requisition =====================
    @PostMapping("/create")
    public ResponseEntity<?> createRequisition(@RequestBody Requisition requisition) {
        // Fetch the full User entity
        if (requisition.getRequestedBy() != null && requisition.getRequestedBy().getId() != null) {
            Optional<User> userOpt = userRepository.findById(requisition.getRequestedBy().getId());
            if (userOpt.isPresent()) {
                requisition.setRequestedBy(userOpt.get());
            } else {
                return ResponseEntity.badRequest().body("Requested User not found");
            }
        }

        requisition.setStatus("PENDING"); // Default status

        // Link items to the requisition to satisfy foreign key constraint
        if (requisition.getItems() != null) {
            requisition.getItems().forEach(item -> item.setRequisition(requisition));
        }

        Requisition savedReq = requisitionRepository.save(requisition);
        return ResponseEntity.ok(savedReq);
    }

    // ===================== Get all Requisitions =====================
    @GetMapping("/all")
    public ResponseEntity<List<Requisition>> getAllRequisitions() {
        List<Requisition> requisitions = requisitionRepository.findAll();
        return ResponseEntity.ok(requisitions);
    }

    // ===================== Get Requisition by ID =====================
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequisitionById(@PathVariable Long id) {
        Optional<Requisition> reqOpt = requisitionRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Requisition not found");
        }
        return ResponseEntity.ok(reqOpt.get());
    }

    // ===================== Update Requisition Status =====================
    @PatchMapping("/update-status/{id}")
    public ResponseEntity<String> updateRequisitionStatus(@PathVariable Long id,
                                                          @RequestParam String status) {
        Optional<Requisition> reqOpt = requisitionRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Requisition not found");
        }

        Requisition requisition = reqOpt.get();
        requisition.setStatus(status);
        requisitionRepository.save(requisition);

        return ResponseEntity.ok("Requisition status updated to " + status);
    }
}
