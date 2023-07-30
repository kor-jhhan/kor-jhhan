package com.kor_jhhan.flink.consumer.records;
public class Case3Record {
    private String col1;
    private int col2;
    private String col3;
    private String col4;
    private int col5;
    private String col6;

    public String getcol1(){
        return this.col1;
    }
    public int getcol2(){ return this.col2; }
    public String getcol3(){ return this.col3; }
    public String getcol4(){ return this.col4; }
    public int getcol5(){ return this.col5; }
    public String getcol6(){ return this.col6; }

    public void setcol1(String val){ this.col1 = val; }
    public void setcol2(int val){ this.col2 = val; }
    public void setcol3(String val){ this.col3 = val; }
    public void setcol4(String val){ this.col4 = val; }
    public void setcol5(int val){ this.col5 = val; }
    public void setcol6(String val){ this.col6 = val; }
}
