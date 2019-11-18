package com.jd.netty;

import java.util.Arrays;

public class ArraysTest {

    public static void main(String[] args) {

        int arr[] = {8, 6, 7, 1, 2, 6, 8};
        // Integer[] array = Arrays.asList(6, 7, 1, 2, 4, 5).toArray(arr);

        //quickSort(arr, 0, arr.length - 1);

        quickSort2(arr, 0, arr.length - 1);


        System.out.println(Arrays.toString(arr));
    }

    private static void quickSort2(int[] arr, int start, int end) {
        if (start >= end) {
            return;
        }

        int index = partation2(arr, start, end);

        quickSort2(arr, start, index - 1);

        quickSort2(arr, index + 1, end);
    }

    // 挖坑填数法
    private static int partation(int[] arr, int start, int end) {
        //分治得到基准数的位置
        int pivot = arr[start];

        //基准数位置  坑位
        int index = start;

        int left = start;
        int right = end;

        while (left <= right) {
            while (left <= right) {
                if (arr[right] < pivot) {
                    arr[left] = arr[right];
                    index = right; //新坑位
                    left++;
                    break;
                }
                right--;
            }
            while (left <= right) {
                if (arr[left] > pivot) {
                    arr[right] = arr[left];
                    index = left; //新坑位
                    right--;
                    break;
                }
                left++;
            }
        }

        arr[index] = pivot;
        return index;
    }

    // 指针交换法
    private static int partation2(int[] arr, int start, int end) {
        //分治得到基准数的位置
        int pivot = arr[start];

        //基准数位置  坑位
        int index = start;

        int left = start;
        int right = end;

        while (left != right) {
            while (left < right && arr[right] > pivot) {
                right--;
            }
            while (left < right && arr[left] <= pivot) {// 注意左边小于等于右边也需要交换
                left++;
            }

            // 交换left和right指向的元素
            if (left < right) {
                int p = arr[left];
                arr[left] = arr[right];
                arr[right] = p;
            }
        }

        // 最后一步将基准数覆盖到指针重合处
        int p = arr[left];
        arr[left] = arr[start];
        arr[start] = p;

        return left;
    }


    public static void quickSort(int[] arr, int low, int high) {
        if (low > high) {
            return;
        }
        int i, j, t, temp;
        temp = arr[low]; //temp中存的就是基准数
        i = low;
        j = high;
        while (i < j) { //顺序很重要，要先从右边开始找
            while (arr[j] >= temp && i < j)
                j--;
            while (arr[i] <= temp && i < j)//再找右边的
                i++;
            if (i < j)//交换两个数在数组中的位置
            {
                t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
            }
        }
        //最终将基准数归位
        arr[low] = arr[i];
        arr[i] = temp;

        quickSort(arr, 0, low - 1);
        quickSort(arr, low + 1, high);
        System.out.println("final =" + Arrays.toString(arr));
    }

}
