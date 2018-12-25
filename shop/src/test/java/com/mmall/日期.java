package com.mmall;

import java.util.Scanner;

public class 日期 {
    public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        int month = 1;
        int year = sc.nextInt();
        int day =  sc.nextInt();
        int []arr = {31,28,31,30,31,30,31,31,30,31,30,31};
        if(year % 4 ==0 && year %100 !=0 || year % 400 ==0){
            arr[1] = 29;
        }

        for (int i = 0; day - arr[i] > 0; ++i) {
            day = day - arr[i];
            ++month;
        }
        System.out.println(month);
        System.out.println(day);

    }
}
