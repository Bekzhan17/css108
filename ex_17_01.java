package com.example.bekzhan;

import java.io.*;
import java.util.Scanner;

public class Exercise_17_01 {
  public static void main(String[] args) {

    try (
      PrintWriter pw = new PrintWriter(new FileOutputStream(new File("Exercise17-01.txt"), true));
    ) {
      for (int i = 0; i < 100; i++) {
        pw.print((int)(Math.random() * 100) + " ");
      }
    } 
    catch (FileNotFoundException fnfe) {
      System.out.println("Cannot create the file.");
      fnfe.printStackTrace();
    }
  }
}
