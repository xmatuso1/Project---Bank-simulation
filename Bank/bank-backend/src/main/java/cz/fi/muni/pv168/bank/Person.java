package cz.fi.muni.pv168.bank;


import java.time.LocalDate;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 ** This entity class represents Person. Person have some name and date of born.
 * One person could have zero or more accounts.
 * @author Katarina Matusova
 */
public class Person {
    
    private Long id;
    private String name;
    private LocalDate born;



    public Person() {
        this.id = null;
        this.name = null;
        this.born = null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBorn(LocalDate born) {
        this.born = born;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBorn() {
        return born;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return id != null ? id.equals(person.id) : person.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Person{"
                + "id=" + id
                + ", name=" + name
                + ", born=" + born
                + '}';
    }
    
}
