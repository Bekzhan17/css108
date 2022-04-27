package com.example.bekzhan;

import java.util.*;

public class ex_21_01 {
	public static void main(String[] args) {
    
		Set<String> set1 = new LinkedHashSet<>(Arrays.asList("George", "Jim", "John", "Blake", "Kevin", "Michael"));
		Set<String> set2 = new LinkedHashSet<>(Arrays.asList("George", "Katie", "Kevin", "Michelle", "Ryan"));

		Set<String> union = new LinkedHashSet<>(set1);
		union.addAll(set2);
		System.out.println("Union of the two sets: " + union);

		Set<String> difference = new LinkedHashSet<>(set1);
		difference.removeAll(set2);
		System.out.println("Difference of the two sets: " + difference);

		Set<String> intersection = new LinkedHashSet<>();
		for (String e: set2) {
			if (set1.contains(e))
				intersection.add(e);
		}
		System.out.println("Intersection of the two sets: " + intersection);
	}
}
