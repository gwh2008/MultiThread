package com.gaowh.gwh;

public class Factory2 {
	public static Car getCarInstance(String type){
        Car c=null;
        if("Benz".equals(type)){
            c=new Benz();
        }
        if("Ford".equals(type)){
            c=new Ford();
        }
        return c;
    }
}
