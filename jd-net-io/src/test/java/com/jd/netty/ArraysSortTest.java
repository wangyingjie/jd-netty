package com.jd.netty;

import java.util.Arrays;

public class ArraysSortTest {


    public static void main(String[] args) {
        int arr[] = {8, 6, 7, 1, 2, 6, 8};

        selectionSort(arr);

        printArr(arr);
    }

    private static void printArr(int[] arr) {
        System.out.println(Arrays.toString(arr));
    }

    //选择排序算法
    private static void selectionSort(int[] arr) {
        // 循环数组
        for (int i = 0; i < arr.length; i++) {

            // 假设第i个为最小值
            int minIndex = i;

            for (int j = i + 1; j < arr.length; j++) {
                //  寻找最小值
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;// 记录最小值索引位置
                }
            }

            int temp = arr[i];
            arr[i] = arr[minIndex];
            arr[minIndex] = temp;

            printArr(arr);
        }
    }

    //插入排序
    private static void insertionSort(int[] arr) {

        for (int i = 0; i < arr.length; i++) {

            int current = arr[i + 1];
            int preIndex = i;

            while (preIndex >= 0 && current < arr[preIndex]) {
                arr[preIndex + 1] = arr[preIndex];
                preIndex--;
            }
            arr[preIndex + 1] = current;
        }
    }


}
