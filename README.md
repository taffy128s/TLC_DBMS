# TLC_DBMS <br />
A mini database management system for CS4710 in National Tsing Hua University. <br />

## Getting Started <br />
Clone the entire repository and add it into a new Java project. <br />

### Prerequisites <br />
Any computer with Java IDE and JDK 1.7 or higher installed. <br />

## Syntax
Currently support CREATE, INSERT, SELECT, DROP, SHOW, DESC, QUIT, EXIT commands.
All keywords(like CREATE, INSERT) are case-insensitive.

CREATE Syntax:
```
CREATE TABLE table_name(attr[, attr ...]);

attr:
    attr_name type [PRIMARY KEY | KEY [BPLUSTREE | HASH]]

type:
    { INT | VARCHAR(length) }
```

INSERT Syntax
```
INSERT INTO table_name[(attr_name[, attr_name ...])] VALUES(data[, data...]);
```

SELECT Syntax
```
SELECT select_target FROM table_references [WHERE where_condition] [ORDER BY sort_target [ASC | DESC]] [LIMIT limitation];

select_target:
    * | target [, target ...]

target:
    attr_name | prefix.{* | attr_name}

table_references:
    table_name [AS table_alias] [, table_name [AS table_alias] ...]
    
where_condition:
    operand operator operand [{AND | OR} operand operator operand]

operand:
    [prefix.]attr_name | constant

sort_target:
    [prefix.]attr_name

operator:
    > | >= | < | <= | <> | =
```

DROP Syntax
```
DROP TABLE table_name[, table_name...];
or
DROP ALL TABLES;
```

SHOW Syntax
```
SHOW TABLES;
or
SHOW TABLE [FULL] table_name [ORDER BY attr_name [ASC | DESC]] [LIMIT length];
```

DESC Syntax
```
DESC [FULL] table_name;
```

QUIT Syntax
```
quit;
```

EXIT Syntax
```
exit;
```

## Running the tests <br />
Tests can be done through three ways: <br />
(1) Create a text file filled with SQL commands, and pass the file name to the exported jar by argument. <br />
(2) Add testcases like the format in any file under directory "test". Then, execute them using JUnit Test. <br />
(3) Straightforwardly run SQL commands in the console. <br />

### Testing example <br />
In the file "SQLParserTest.java", we have "Create table f();". <br />
Just like any SQL language, we should reject this command because a table without columns is not allowed. <br />

## Deployment <br />
Export a jar executable in any Java IDE, and run it on any machine that supports Java Runtime Environment. <br />

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
