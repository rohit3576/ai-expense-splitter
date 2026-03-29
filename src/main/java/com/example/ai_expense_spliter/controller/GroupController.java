package com.example.ai_expense_spliter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_expense_spliter.model.Group;
import com.example.ai_expense_spliter.service.GroupService;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public Group create(@RequestBody Group group) {
        return groupService.save(group);
    }

    @GetMapping
    public List<Group> getAll() {
        return groupService.getAll();
    }
}