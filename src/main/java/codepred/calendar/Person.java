package codepred.calendar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class Person {

    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return name + " (" + age + " years old)";
    }

    public static void main(String[] args) {
        List<Person> people = new ArrayList<>();
        people.add(new Person("Alice", 28));
        people.add(new Person("Bob", 22));
        people.add(new Person("Charlie", 35));
        people.add(new Person("David", 19));

        // Create a Comparator to sort by age in ascending order
        people = people.stream().sorted(Comparator.comparing(Person::getAge))
            .collect(Collectors.toList());

        // Print the sorted list
        for (Person person : people) {
            System.out.println(person);
        }
    }

}

