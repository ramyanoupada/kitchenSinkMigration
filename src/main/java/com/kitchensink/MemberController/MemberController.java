package com.kitchensink.MemberController;

import com.kitchensink.model.Member;
import com.kitchensink.service.MemberRegistrationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/members")
public class MemberController {
     @Autowired
    private Validator validator;

    @Autowired
    private MemberRegistrationService service;

    @Autowired
    private MemberRegistrationService registration;
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    @GetMapping
    public List<Member> listAllMembers() {
        return service.getAllOrderedByName();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> lookupMemberById(@PathVariable("id") String id) {
        return service.findById(id)
                .map(member -> ResponseEntity.ok().body(member))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity createMember(@RequestBody Member member) {
        try {
            validateMember(member);
            registration.register(member);
            return ResponseEntity.status(HttpStatus.CREATED).body(member); // Return created member or some other response
        } catch (ConstraintViolationException ce) {
            return createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "Email taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseObj);
        } catch (Exception e) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObj);
        }
    }

    private void validateMember(Member member) throws ValidationException {
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        if (emailAlreadyExists(member.getEmail())) {
            throw new ValidationException("Unique Email Violation");
        }
    }

    private ResponseEntity<Map<String, String>> createViolationResponse(Set<ConstraintViolation<?>> violations) {
        log.info("Validation completed. Violations found: " + violations.size());
        Map<String, String> responseObj = new HashMap<>();
        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObj);
    }

    public boolean emailAlreadyExists(String email) {
        return service.getEmail(email).isPresent();
    }
}