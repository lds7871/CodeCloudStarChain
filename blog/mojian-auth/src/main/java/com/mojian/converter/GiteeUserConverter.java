package com.mojian.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojian.dto.OAuth2ThirdAccount;

public class GiteeUserConverter {

    public static OAuth2ThirdAccount convert(String userInfoResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(userInfoResponse);
            OAuth2ThirdAccount account = new OAuth2ThirdAccount();
            account.setUniqueId(jsonNode.get("id").asText());
            account.setUsername(jsonNode.get("login").asText());
            account.setEmail(jsonNode.get("email").asText());
            // 设置其他字段
            return account;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
