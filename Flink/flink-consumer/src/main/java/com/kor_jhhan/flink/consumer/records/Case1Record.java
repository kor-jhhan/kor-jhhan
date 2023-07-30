package com.kor_jhhan.flink.consumer.records;
public class Case1Record {
    private String col1;
    private String col2;
    private String col3;
    private String col4;
    private String col5;

    public String getcol1(){ return this.col1; }
    public String getcol2(){
        return this.col2;
    }
    public String getcol3(){ return this.col3; }
    public String getcol4(){ return this.col4; }
    public String getcol5(){ return this.col5; }

    public void setcol1(String val){ this.col1 = val; }
    public void setcol2(String val){ this.col2 = val; }
    public void setcol3(String val){ if(val != null) { this.col3=val; }else{ this.col3 = ""; }}
    public void setcol4(String val){ this.col4 = val; }
    public void setcol5(String val){ this.col5 = val; }
}
