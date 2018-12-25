package com.mmall;
/*
给定n个整数，请统计出每个整数出现的次数，按出现次数从多到少的顺序输出。

输入格式

　　输入的第一行包含一个整数n，表示给定数字的个数。
 　　第二行包含n个整数，相邻的整数之间用一个空格分隔，表示所给定的整数。

输出格式

　　输出多行，每行包含两个整数，分别表示一个给定的整数和它出现的次数。按出现次数递减的顺序输出。如果两个整数出现的次数一样多，则先输出值较小的，然后输出值较大的。

样例输入

12
 5 2 3 3 1 3 4 2 5 2 3 5

样例输出

3 4
 2 3
 5 3
 1 1
 4 1
 */

import java.util.Arrays;
import java.util.Scanner;

public class 数字排序 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n=sc.nextInt();
        int []a = new int[n];
        int []num = new int[n];
        int temp = 1;
        for (int i = 0; i < n; i++) {
           a[i] = sc.nextInt();
        }
        Arrays.sort(a);
        for (int i = 0; i < a.length-1; i++) {
            if (a[i] == a[i+1])
                num[i] = a[i];
            System.out.println(num);
        }
        for (int i = 0; i < num.length; i++) {
            System.out.println(num);
        }
        sc.close();

    }
}
