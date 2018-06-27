package com.gaowh.gwh;

//import org.junit.Test;
  
  
public class BinarySearchTest {  
//    @Test
    public void testSearch()  
    {  
        BinarySearch bs=new BinarySearch();
          
        int[] sortedData={1,2,3,4,5,6,6,7,8,8,9,10};  
        int findValue=6;  
        int length=sortedData.length;  
          
        int pos=bs.searchRecursive(sortedData, 0, length-1, findValue);  
        System.out.println("递归二分查找-要查找的值: "+findValue+" 在数组中的位置： "+pos+" ;递归次数: "+bs.getrCount());  
        int pos2=bs.searchLoop(sortedData, findValue);  
          
        System.out.println("循环二分查找-要查找的值: "+findValue+" 在数组中的位置： "+pos2+" ;循环次数: "+bs.getlCount());  
    }  
}  