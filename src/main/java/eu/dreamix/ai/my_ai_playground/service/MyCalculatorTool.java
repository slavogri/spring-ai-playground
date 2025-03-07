package eu.dreamix.ai.my_ai_playground.service;

import org.springframework.ai.tool.annotation.Tool;

import java.math.BigDecimal;

public class MyCalculatorTool {
    @Tool(description = "This method calculates the factorial of a given number.")
    public String calculateFactorial(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
        }
        if (n>64){
            throw new IllegalArgumentException("The current implementation does not support factorial of number higher than 64.");
        }
        BigDecimal result = BigDecimal.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(new BigDecimal(i));
        }
        return result.toString();
    }
}
