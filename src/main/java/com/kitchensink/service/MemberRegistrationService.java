package com.kitchensink.service;

import com.kitchensink.data.MemberRepository;
import com.kitchensink.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberRegistrationService {
    private static final Logger log = LoggerFactory.getLogger(MemberRegistrationService.class);
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MongoTemplate mongoTemplate; // Using MongoTemplate for MongoDB operations

    @Autowired
    private ApplicationEventPublisher eventPublisher; // For publishing events

    public Optional<Member> findById(String id) {
        return memberRepository.findById(id);
    }

     public Optional<Member> getEmail(String email) {
        return memberRepository.findByEmail(email);
    }
    @Transactional
    public void register(Member member) throws Exception {
        log.info("Registering " + member.getName());
        member.setId(UUID.randomUUID().toString().split("-")[0]);
        memberRepository.save(member);  // Save the member to MongoDB
         }

    public List<Member> getAllOrderedByName() {
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
