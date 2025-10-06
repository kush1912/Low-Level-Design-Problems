package lld.DesignProblems.Oracle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OracleROne {
    public static void main(String[] args) {
        int[] nums ={ 4, 2, 1, 5, 10, 4, 8,3};
        List<List<Integer>> ans = longestDecreasingSub(nums);
        List<List<Integer>> result = new ArrayList<>();
        int s=0;
        for(List<Integer> candidate: ans){
            if(candidate.size()>s){
                s= candidate.size();
            }
        }
        for(List<Integer> candidate: ans){
            if(candidate.size()==s){
                result.add(candidate);
            }
        }
        System.out.println(result);
    }


    public static List<List<Integer>> longestDecreasingAll(int[] nums){
        int n = nums.length;
        List<List<Integer>>[] dp= new ArrayList[n];

        for(int i=0;i<n;i++){
            dp[i] = new ArrayList<>();
            dp[i].add(new ArrayList<>(Arrays.asList(nums[i])));
        }
        List<List<Integer>> longest = new ArrayList<>();
        longest.add(Arrays.asList(nums[0]));
        for(int i=1;i<n;i++){
            for(int j=0;j<i;j++){
                if(nums[j]>nums[i] && dp[i].size()<dp[j].size()+1){
                    dp[i] = new ArrayList<>(dp[j]);
                    for(List<Integer> seq: dp[i])
                        seq.add(nums[i]);
                }
            }
            if(dp[i].size()>longest.get(0).size()){
                longest.clear();
            } else if (dp[i].size()==longest.get(0).size()) {
                longest.addAll(dp[i]);
            }
        }
        return longest;
    }

    public static List<List<Integer>> longestDecreasingSub(int[] nums){
        List<List<Integer>> res = new ArrayList<>();
        List<Integer> curr = new ArrayList<>();
        backtrack(nums, 0, curr, res);
        return res;
    }

    public static void backtrack(int[] nums, int index, List<Integer>curr, List<List<Integer>> res){
        if(index==nums.length){
            if(!curr.isEmpty()){
                res.add(new ArrayList<>(curr));
            }
            return;
        }

        backtrack(nums, index+1, curr, res);
        if(curr.isEmpty() || nums[index]<curr.get(curr.size()-1)){
            curr.add((nums[index]));
            backtrack(nums, index+1, curr, res);
            curr.remove(curr.size()-1);
        }
    }

    public static List<Integer> longestDecreasing(int[] nums){
        int n = nums.length;
        List<Integer>[] dp= new ArrayList[n];

        for(int i=0;i<n;i++){
            dp[i] = new ArrayList<>();
            dp[i].add(nums[i]);
        }
        List<Integer> longest = new ArrayList<>();
        for(int i=1;i<n;i++){
            for(int j=0;j<i;j++){
                if(nums[j]>nums[i] && dp[i].size()<dp[j].size()+1){
                    dp[i] = new ArrayList<>(dp[j]);
                    dp[i].add(nums[i]);
                }
            }
            if(dp[i].size()>longest.size()){
                longest=dp[i];
            }
        }
        return longest;
    }
    public static void permutations(String str, int start, int end){
        if(start==end) {
            System.out.println(str);
        }else{
            for(int i=start;i<=end;i++){
                str = swap(str,start, i);
                permutations(str, start+1,end);
                str= swap(str, start, i);
            }
        }
    }

    public static String swap(String str, int i, int j){
        char temp;
        char[] characterArray =str.toCharArray();
        temp = characterArray[i];
        characterArray[i]=characterArray[j];
        characterArray[j]=temp;
        return  String.valueOf(characterArray);
    }
}
