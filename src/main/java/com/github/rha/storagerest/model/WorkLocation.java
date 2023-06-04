package com.github.rha.storagerest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkLocation {
    private int zone;
    private int region;
    private int area;
    private int district;

    private int store;
}
