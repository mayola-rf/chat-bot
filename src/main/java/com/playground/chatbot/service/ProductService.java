package com.playground.chatbot.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    Map<String, List<String>> products = Map.of("laptop", List.of("Dell", "Lenovo", "Apple", "ThinkPad"));

    public List<String> findProduct(String name, int cost) {
        return products.get(name)
                .stream().filter(s -> Integer.parseInt(findProductSpecification(s).get("cost")) <= cost)
                .toList();
    }

    public Map<String, String> findProductSpecification(String name) {
        switch (name) {
            case "Dell":
                return Map.of("cost", "100", "RAM", "16", "currency", "$", "RAM Unit", "GB");
            case "Lenovo":
                return Map.of("cost", "90", "RAM", "32", "currency", "$", "RAM Unit", "GB");
            case "Apple":
                return Map.of("cost", "105", "RAM", "32", "currency", "$", "RAM Unit", "GB");
            case "ThinkPad":
                return Map.of("cost", "57", "RAM", "16", "currency", "$", "RAM Unit", "GB");
            default:
                return Map.of();
        }
    }
}
