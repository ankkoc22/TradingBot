package com.company;

public class Main {

    public static void main(String[] args) {
        // write your code here
        BackgroundBot bg = new BackgroundBot();
        try {
            while(true) {
                bg.getAnswer();
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
