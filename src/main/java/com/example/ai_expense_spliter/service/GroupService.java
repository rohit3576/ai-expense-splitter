package com.example.ai_expense_spliter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ai_expense_spliter.model.Group;
import com.example.ai_expense_spliter.model.User;
import com.example.ai_expense_spliter.repository.GroupRepository;
import com.example.ai_expense_spliter.repository.UserRepository;

@Service
public class GroupService {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Group createGroup(Group group) {
        // Handle members if they're passed with IDs
        if (group.getMembers() != null) {
            List<User> members = new ArrayList<>();
            for (User member : group.getMembers()) {
                if (member.getId() != null) {
                    User existingUser = userRepository.findById(member.getId())
                        .orElseThrow(() -> new RuntimeException("User not found: " + member.getId()));
                    members.add(existingUser);
                }
            }
            group.setMembers(members);
        }
        return groupRepository.save(group);
    }
    
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }
    
    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
    }
}