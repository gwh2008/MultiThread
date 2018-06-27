package com.gwh;

public class FactoryTest {
	 public static void main(String[] args) {
	        Car c1=Factory1.getCarInstance("Ford");
	        //Car c2=Factory1.getCarInstance("Benz");
	        if(c1!=null){
	            c1.run();
	            c1.stop();
	        }else{
	            System.out.println("造不了这种汽车。。。");
	        }
	    }
}
