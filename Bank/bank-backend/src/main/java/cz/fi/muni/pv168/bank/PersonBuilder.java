package cz.fi.muni.pv168.bank;


import java.time.LocalDate;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Katarina Matusova
 */
public class PersonBuilder {

    private Long id;
    private String name;
    private LocalDate born;

    public PersonBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public PersonBuilder born(LocalDate born) {
        this.born = born;
        return this;
    }

    public PersonBuilder name(String name) {
        this.name = name;
        return this;
    }

    public Person build() {
        Person person = new Person();
        person.setId(id);
        person.setBorn(born);
        person.setName(name);
        return person;
    }
}