package lld.DesignProblems.kodu;

import java.util.ArrayList;
import java.util.List;

public class TaxConfiguration {
    List<TaxSlab> taxSlabs;

    public TaxConfiguration() {
        this.taxSlabs = new ArrayList<>();
    }

    public void addTaxSlab(Integer lower, Integer upper, Integer rate, Integer baseTax){
        taxSlabs.add(new TaxSlab(lower, upper, rate, baseTax));
    }

    public List<TaxSlab> getTaxSlab(){
        return taxSlabs;
    }
    public Calculator createCalculator(){
        return new Calculator(taxSlabs);
    }
}
