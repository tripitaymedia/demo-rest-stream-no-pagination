package com.github.rha.storagerest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;

    private WorkLocation workLocation;

    private Address homeAddress;
    private Address workAddress;

    private String phone;
    private String workPhone;

    private String ssn;
    private String salary;
}
