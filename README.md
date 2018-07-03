# Notes

### Links

A link to my planner, to see what I am current doing with this application, and ideas I am proposing:
https://trello.com/b/3Y4c8e7j

If you have any questions about this application, or for support, please email:
nicholasjonescode@gmail.com

### Overview

A multi-feature, user-based project manager centered around musical compositions. 
This is a web application designed to help composers manage their projects in a centralized location, 
both with collaborations and solo projects, and also to receive feedback from other composers.
The goal is to provide the composer with everything necessary to keep track of their projects with the different features: 

### Features

* Create, delete, and update profiles with info for your works
* A task/idea manager for each project
* Secure and user-based, with sessions and BCrypt encryption
* Ability to publish your work as public on the integrated blog for review and comments
* Cloud sharing imported files with the Dropbox API
* Local file attachment, to open project files straight from the manager

### Technologies Used
* Java
* JavaScript (proposed)
* Springboot Framework
* Thymeleaf template engine
* HTML/CSS
* JPA (Spring JPA)
* MySQL
* Hibernate
* REST
* IntelliJ IDEA (IDE)
* Dropbox API (Java SDK)
* Cloud Convert API (Http, using Spring's Rest Template)
* Apache IO utils
* Pegdown Markdown->HTML conversion Java library

### What I learned
* REST services / Using REST APIs, Spring's Rest Template (CloudConcert API)
* JavaScript (vanilla ES6, mostly)
* File and local file parsing topics, Java NIO, Apache I/O, Desktop API, File uploads, File downloads, input/output streams, etc.
* Spring Security / Basic Encryption
* Spring Sessions / HTTP sessions
* Extensive learning of CSS elements, usage, and techniques
* Extended learning of the technologies learned in LaunchCode's LC101: Java, Spring, Hibernate, Thymeleaf, CSS...
* Creating custom validation annotations like the ones in javax.validation.constraints
* Using an API SDK and basic OAuth2/Authentication flows (Dropbox's, specifically)