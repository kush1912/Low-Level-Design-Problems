package lld.DesignProblems.phonePePractice.hackathon.model;

import lld.DesignProblems.phonePePractice.hackathon.utils.IdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Getter
@Setter
@ToString
public class User {
    public User(String name, String department) {
        this.userId = IdGenerator.id();
        this.name = name;
        Department = department;
        System.out.println("User Created! "+ name);
    }
    private String userId;
    private String name;
    private String Department;


}
