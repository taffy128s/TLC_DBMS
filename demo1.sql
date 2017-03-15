CREATE TABLE Article (
	articleid int PRIMARY KEY, 
	title varchar(20), 
	author varchar(20), 
	view int
);

CREATE TABLE Comment
	commentid int PRIMARY KEY,
	author varchar(20),
	content varchar(40),
	articleid int
);

create TABLE Comment (
	commentid int PRIMARY KEY,
	author varchar(20),
	content varchar(40),
	articleid int
);

CREATE TABLE Links (
	linkid int PRIMARY KEY,
	url text
);

CREATE TABLE Links (
	linkid int PRIMARY KEY, 
	url varchar(40)
);

INSERT INTO Article VALUES (1, 'Hello', 'Sys', 33);
INSERT INTO Article VALUES (2, 'Hello Spacel!', 'Astronaut', 33);
INSERT INTO Article VALUES (3, 'Cool#Hashtag', 'Sys', 33);
INSERT INTO Article VALUES (4, 'Hello', 'Sys', 0);

INSERT INTO Article 
VALUES (1, 'Last One', 'Sys', 33);

INSERT INTO Comment
VALUES (1, 'Sys', 'I can leave a comment', 'Hello');

INSERT INTO Comment 
VALUES (1, 'SomeoneReallyHaveALongName', 'Blahblah', 1);

InSeRt InTo Links
ValUeS(1, 'http://www.nthu.edu.tw');