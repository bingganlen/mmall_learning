package com.mmall;

import java.util.Scanner;
/*
旋转是图像处理的基本操作，在这个问题中，你需要将一个图像逆时针旋转90度。
　　计算机中的图像表示可以用一个矩阵来表示，为了旋转一个图像，只需要将对应的矩阵旋转即可。
输入格式
　　输入的第一行包含两个整数n, m，分别表示图像矩阵的行数和列数。
　　接下来n行每行包含m个整数，表示输入的图像。
输出格式
　　输出m行，每行包含n个整数，表示原始矩阵逆时针旋转90度后的矩阵。
样例输入
2 3
1 5 3
3 2 4
样例输出
3 4
5 2
1 3
 */
public class 图像旋转 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n=sc.nextInt();
        int m=sc.nextInt();
        int [][]arr = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                arr[i][j] = sc.nextInt();
            }
            
        }

        int [][]b = new int [m][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                b[m -j-1][i] = arr[i][j];
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(b[i][j]+" ");
            }
            System.out.println("");
        }
        sc.close();

    }
}
/*另一种 输入时就。。
           Scanner in = new Scanner(System.in);
16         int m=in.nextInt();//2
17         int n=in.nextInt();//3
18         int [][]a = new int[n][m];
19         for(int i=0;i<m;i++ ){
20             for(int  j=n-1;j>=0;j--){
21                 a[j][i]=in.nextInt();
22             }
23         }
24         for(int i=0;i<n;i++){
25             for(int j=0;j<m;j++){
26                 System.out.print(a[i][j]+" ");
27             }
28             System.out.println();
29         }

 */