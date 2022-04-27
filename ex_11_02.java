package com.example.bekzhan;

import java.util.Date;

public class Code_02 {
    
    
    public static void main(String[] args) {
    
    
        Person person = new Person("John","appartment","17858990800","1144196409@qq.com");
        System.out.println(person.toString());
        Student student = new Student("Max","appartment","11111","22222",1);
        System.out.println(student.toString());
        Employee employee = new Employee("Anna","appartment","11111","22222","sss",1000,new Date());
        System.out.println(employee.toString());
        Faculty faculty = new Faculty("Jomart","appartment","11111","22222","sss",1000,new Date(),new Date(),1);
        System.out.println(faculty.toString());
        Staff staff = new Staff("Bakyt","appartment","11111","22222","sss",1000,new Date(),"manager");
        System.out.println(staff.toString());
    }
}
class Person{
    
    
    private String name;
    private String address;
    private String phoneNumber;
    private String email_address;
    Person(String name,String address,String phoneNumber,String email_address){
    
    
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email_address = email_address;
    }
    public String getThisName(){
    
    
        return this.name;
    }
    public String toString(){
    
    
        return ("Person " + this.name);
    }
}
class Student extends Person{
    
    
    final static int freshman = 1;
    final static int senior = 2;
    final static int junior = 3;
    final static int graduate = 4;
    private int studengStatus = 0;
    Student(String name,String address,String phoneNumber,String email_address,int studengStatus){
    
    
        super(name,address,phoneNumber,email_address);
        this.studengStatus = studengStatus;
    }
    @Override
    public String toString(){
    
    
        return ("Student " + this.getThisName());
    }
}
class Employee extends Person{
    
    
    private String office;
    private double salary;
    private Date acceptDate;

    Employee(String name,String address,String phoneNumber,String email_address,String office,double salary,Date acceptDate){
    
    
        super(name,address,phoneNumber,email_address);
        this.office = office;
        this.salary = salary;
        this.acceptDate = acceptDate;
    }
    @Override
    public String toString(){
    
    
        return ("Employee " + this.getThisName());
    }
}
class Faculty extends Employee{
    
    
    private Date workDate;
    private int level;
    Faculty(String name,String address,String phoneNumber,String email_address,String office,double salary,Date acceptDate,Date workDate,int level){
    
    
        super(name,address,phoneNumber,email_address,office,salary,acceptDate);
        this.level = level;
        this.workDate = workDate;
    }
    @Override
    public String toString(){
    
    
        return ("Faculty " + this.getThisName());
    }
}
class Staff extends Employee{
    
    
    private String title;
    Staff(String name,String address,String phoneNumber,String email_address,String office,double salary,Date acceptDate,String title){
    
    
        super(name,address,phoneNumber,email_address,office,salary,acceptDate);
        this.title = title;
    }
    @Override
    public String toString(){
    
    
        return ("Staff " + this.getThisName());
    }
}
