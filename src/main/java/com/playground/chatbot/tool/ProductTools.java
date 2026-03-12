package com.playground.chatbot.tool;

import com.playground.chatbot.service.ProductService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductTools {

    @Autowired
    ProductService productService;

    @Tool(description = "Find for product by price")
    public List<String> findProduct(String name, int cost) {
        return productService.findProduct(name, cost);
    }

    @Tool(description = "Find the specifications of a product name. The specifications include cost, CPU and RAM")
    public Map<String, String> findProductSpecifications(String name) {
        return productService.findProductSpecification(name);
    }

}
