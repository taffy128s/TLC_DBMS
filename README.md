# TLC_DBMS
A mini database management system for CS4710 in National Tsing Hua University. <br />

## Getting Started
This project uses [Gradle](https://gradle.org/) build system.

Simply clone this repository and use gradle to build/run.
For more information, see below.

### Prerequisites
JDK 1.8 or higher installed. <br />

### Build
In project root directory, type `./gradlew build`. All files generated will be in `./build/` directory.

In Windows, use `gradlew.bat` instead of `./gradlew`.

### Run
In project root directory, type `./gradlew installDist`, an executable script will be placed in
`./build/install/TLC_DBMS/bin` called `TLC_DBMS`, `TLC_DBMS.bat`.

In Windows, use `gradlew.bat` instead of `./gradlew`.

### Import to IDEs
For Eclipse, you can import this project directly, or use `./gradlew eclipse` to generate files needed by eclipse.

For IntelliJ IDEA, you can import this project directly, or use `./gradlew idea` to generate files needed by IDEA.

### More information
Simply type `./gradlew tasks`.

## Syntax
Currently support CREATE, INSERT, SELECT, DROP, SHOW, DESC, LOAD, QUIT, EXIT commands.
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
    operand operator operand [{AND | OR} operand operator operand ...]

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

LOAD Syntax
```
LOAD SCRIPT [SILENT] INFILE script_filename;
```

QUIT Syntax
```
quit;
```

EXIT Syntax
```
exit;
```

## Built With
[Eclipse](https://www.eclipse.org/downloads/) - The most widely used Java IDE. <br />
[IntelliJ](https://www.jetbrains.com/idea/download/#section=windows) - Developed by JetBrains. <br />

## Authors
Tsai Tzung-yu <br />
Cheng Yu-min <br />
Chen Tz-yu <br />

## Acknowledgments
The name "TLC" comes from the names of authors. <br />
T->Taffy->Cheng Yu-min <br />
L->LittleBird->Tsai Tzung-yu <br />
C->Chen->Chen Tz-yu <br />
Not from "triple-level cell". <br />
