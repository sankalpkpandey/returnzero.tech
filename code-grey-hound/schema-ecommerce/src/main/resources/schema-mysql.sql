CREATE TABLE user (
    id bigint NOT NULL AUTO_INCREMENT,
    lastname NOT NULL varchar(255),
    firstname NOT NULL varchar(255),
    emailaddress NOT NULL varchar(255),
    username NOT NULL varchar(255 UNIQUE),
    PRIMARY KEY (id),
    unique (username),
    unique (emailaddress)
);

CREATE TABLE product (
    id bigint NOT NULL AUTO_INCREMENT,
    name NOT NULL varchar(255),
    description NOT NULL longtext,
    type NOT NULL varchar(255),
    quantity NOT NULL varchar(255),
    thumbnail NOT NULL varchar(255),
    video NOT NULL varchar(255),
    imageone NOT NULL varchar(255),
    imagetwo varchar(255),
    imagethree varchar(255),
    imagefour varchar(255),
    PRIMARY KEY (id)
);

CREATE TABLE cart (
    userid bigint NOT NULL,
    productid bigint NOT NULL,
    quantity int NOT NULL,
    PRIMARY KEY (userid, productid)
);

CREATE TABLE order (
    id bigint NOT NULL AUTO_INCREMENT,
    userid bigint NOT NULL,
    status NOT NULL varchar(255),
    outcome NOT NULL longtext,
    PRIMARY KEY (id),
    unique (userid, status)
);

CREATE TABLE pricing (
    productid bigint NOT NULL,
    price double NOT NULL,
    discountpercent double NOT NULL,
    currency NOT NULL varchar(255),
    coupencode NOT NULL varchar(255),
    PRIMARY KEY (productid)
);