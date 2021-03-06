CREATE TABLE "PERSON" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "NAME" VARCHAR(255) NOT NULL,
    "BORN" DATE NOT NULL
);
CREATE TABLE "ACCOUNT" (
    "NUMBER" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "PERSONID" BIGINT REFERENCES PERSON (ID),
    "NOTE" VARCHAR (255) ,
    "BALANCE" DOUBLE NOT NULL
);
