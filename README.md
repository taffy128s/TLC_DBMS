# TLC_DBMS <br />
A mini database management system for CS4710 in National Tsing Hua University. <br />

## Getting Started <br />
Clone the whole repo and add it into a new Java project. <br />

### Prerequisites <br />
Any computer with Java IDE and JDK 1.7 or higher installed. <br />

## Running the tests <br />
Now we ONLY support CREATE, INSERT, SHOW, DESC, QUIT, EXIT commands. <br />
Tests can be done through two ways... <br />
(1) Add testcases like the format in any file under directory "test". Then, runs them using JUnit Test. <br />
(2) Straightforwardly run SQL commands in the console. <br />

### Testing example <br />
In the file "SQLParserTest.java", we have "Create table f();". <br />
Just like any SQL language, we should reject it because an empty table is not allowed. <br />

## Deployment <br />
Export a jar file in any IDE, and run it on any machine that supports Java Runtime Environment. <br />

## Built With <br />
[Eclipse](https://www.eclipse.org/downloads/) - The most widely used Java IDE. <br />
[IntelliJ](https://www.jetbrains.com/idea/download/#section=windows) - Developed by JetBrains. <br />

## Authors <br />
Tsai Tzung-yu <br />
Cheng Yu-min <br />
Chen Tz-yu <br />

## Acknowledgments <br />
The name "TLC" comes from the names of authors. <br />
T->Taffy->Cheng Yu-min <br />
L->Little Bird->Tsai Tzung-yu <br />
C->Chen->Chen Tz-yu <br />
Not from "triple-level cell". <br />