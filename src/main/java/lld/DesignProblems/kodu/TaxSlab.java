package lld.DesignProblems.kodu;

public class TaxSlab {
    private Integer lowerLimit;
    private Integer upperLimit;
    private Integer rate;
    private Integer baseTax;

    public TaxSlab(Integer lowerLimit, Integer upperLimit, Integer rate, Integer baseTax) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.rate = rate;
        this.baseTax = baseTax;
    }

    public Integer getBaseTax(){
        return baseTax;
    }
    public Integer getLowerLimit() {
        return lowerLimit;
    }

    public Integer getUpperLimit() {
        return upperLimit;
    }

    public Integer getRate() {
        return rate;
    }



}
