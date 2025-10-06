package lld.DesignProblems.kodu;

import java.util.List;

public class Calculator {
    private List<TaxSlab> taxSlabs;

    public Calculator(List<TaxSlab> taxSlabs){
        this.taxSlabs = taxSlabs;
    }

    public Double calculateNetPay(Employee emp){
        Integer sal= emp.getSalary();
        Double totalTax = 0.0;
        TaxSlab chosen;
        for(TaxSlab slab: taxSlabs){
            if(sal>=slab.getLowerLimit() && sal<=slab.getUpperLimit()){
                double taxableSal = sal - slab.getLowerLimit();
                totalTax = taxableSal* slab.getRate()/100;
                totalTax+=slab.getBaseTax();
//                double tax = Math.min(sal, slab.getUpperLimit())-slab.getLowerLimit();
//                totalTax+= sal*slab.getRate()/100;
//                chosen =slab;
//                break;
            }
        }
//        totalTax+=chosen.getBaseTax();

        System.out.println(totalTax);
        return sal-totalTax;
    }

    public static void main(String[] args) {
        TaxConfiguration configuration= new TaxConfiguration();
        configuration.addTaxSlab(5, 10, 10,1);
        configuration.addTaxSlab(10,20, 20, 2);
        configuration.addTaxSlab(20, 100, 30, 3);

        Calculator calculator =configuration.createCalculator();
        Employee e1 = new Employee(25);
        Double netPay = calculator.calculateNetPay(e1);
        System.out.println(netPay);
    }
}
