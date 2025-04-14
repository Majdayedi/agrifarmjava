package org.example.java1.entity;

import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    public static List<Role> fromJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> rolesAsString = gson.fromJson(json, listType);
        return rolesAsString.stream().map(Role::valueOf).collect(Collectors.toList());
    }
}

