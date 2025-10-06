package lld.DesignProblems;

import lld.DesignProblems.kodu.Calculator;
import lld.DesignProblems.kodu.Employee;
import lld.DesignProblems.kodu.TaxConfiguration;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorTest {
    @Test
    public void testNetPayCalculator(){
        TaxConfiguration configuration= new TaxConfiguration();
        configuration.addTaxSlab(5, 10, 10, 1);
        configuration.addTaxSlab(10,20, 20, 1);
        configuration.addTaxSlab(20, 100, 30,1 );

        Calculator calculator =configuration.createCalculator();
        Employee e1 = new Employee(25);
        Double netPay = calculator.calculateNetPay(e1);
        assertEquals(18,netPay);
    }
}
