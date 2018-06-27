package com.gwh;

/** 
 * 二分查找 
 * @author gwh 
 * 
 */  
public class BinarySearch {  
    private int rCount=0;  //递归的次数 
    private int lCount=0;  //循环的次数 
      
    /** 
     * 获取递归的次数 
     * @return 
     */  
    public int getrCount() {  
        return rCount;  
    }  
  
    /** 
     * 获取循环的次数 
     * @return 
     */  
    public int getlCount() {  
        return lCount;  
    }  
  
    /** 
     * 执行递归二分查找，返回第一次出现该值的位置 
     * @param sortedData    已排序的数组 
     * @param start         开始位置 
     * @param end           结束位置 
     * @param findValue     需要找的值 
     * @return              值在数组中的位置，从0开始。找不到返回-1 
     */  
    public int searchRecursive(int[] sortedData,int start,int end,int findValue)  
    {  
        rCount++;  
        if(start<=end)  
        {  
            //中间位置  
            int middle=(start+end)>>1;    //相当于(start+end)/2  
            //中值  
            int middleValue=sortedData[middle];  
              
            if(findValue==middleValue)  
            {  
                //等于中值直接返回  
                return middle;  
            }  
            else if(findValue<middleValue)  
            {  
                //小于中值时在中值前面找  
                return searchRecursive(sortedData,start,middle-1,findValue);  
            }  
            else  
            {  
                //大于中值在中值后面找  
                return searchRecursive(sortedData,middle+1,end,findValue);  
            }  
        }  
        else  
        {  
            //找不到  
            return -1;  
        }  
    }  
      
    /** 
     * 循环二分查找，返回第一次出现该值的位置 
     * @param sortedData    已排序的数组 
     * @param findValue     需要找的值 
     * @return              值在数组中的位置，从0开始。找不到返回-1 
     */  
    public int searchLoop(int[] sortedData,int findValue)  
    {  
        int start=0;  
        int end=sortedData.length-1;  
          
        while(start<=end)  
        {  
            lCount++;  
            //中间位置  
            int middle=(start+end)>>1;    //相当于(start+end)/2  
            //中值  
            int middleValue=sortedData[middle];  
              
            if(findValue==middleValue)  
            {  
                //等于中值直接返回  
                return middle;  
            }  
            else if(findValue<middleValue)  
            {  
                //小于中值时在中值前面找  
                end=middle-1;  
            }  
            else  
            {  
                //大于中值在中值后面找  
                start=middle+1;  
            }  
        }  
        //找不到  
        return -1;  
    }  
}  