hbnpojogen
==========

Hibernate Pojo Generator

What does it do?
================

Given an accessible database schema, the Hibernate POJO Generator produces all the Java code necessary to access each field in each table via the Hibernate persistence framework. Additionally, the generator also creates all the necessary helper classes and test units for each component.

Features
========

Given a source database, Hibernate POJO Generator (hbnPojoGen) generates:

- Java objects representing each table using annotations for use with Hibernate.
- A JUnit test case per table that uses the objects generated to create, populate, save, retrieve and compare results
- DAO per class
- The appropriate enumeration files
- Spring and hibernate configuration
- DAO layers
- A data factory class per schema to return a pre-populated object with random data (for boundary checking, database population, etc)

Also supports
- Join tables including those with additional fields in link tables
- Polymorphism/inheritance support
- Composite Keys
- One-To-One, many-to-one, many-to-many, etc
- Multiple schema support (4 modes)
- Natural Keys
- Enumerations (including those entries which cannot be mapped cleanly onto the java world)
- A whole bunch of more stuff (see [sample.xml](./hbnpojogen-core/sample.xml))

Please see sample.xml for a very documented list of available features.
