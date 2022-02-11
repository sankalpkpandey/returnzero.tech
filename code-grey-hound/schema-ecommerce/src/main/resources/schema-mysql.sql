CREATE TABLE user (
    id bigint NOT NULL AUTO_INCREMENT,
    lastname NOT NULL varchar(255),
    firstname NOT NULL varchar(255),
    emailaddress NOT NULL varchar(255),
    username NOT NULL varchar(255),
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    unique (username, emailaddress),
    index(username, lastname, firstname, emailaddress)
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
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    index(name, type)
);

CREATE TABLE cart (
    userid bigint NOT NULL,
    productid bigint NOT NULL,
    quantity int NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userid, productid)
);

CREATE TABLE order (
    id BINARY(16) DEFAULT (uuid_to_bin(uuid())) NOT NULL,
    userid bigint NOT NULL,
    status NOT NULL varchar(255),
    outcome NOT NULL longtext,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    unique (userid, status),
    index(userid, status)
);

CREATE TABLE pricing (
    productid bigint NOT NULL,
    price double NOT NULL,
    discountpercent double NOT NULL,
    currency NOT NULL varchar(255),
    coupencode NOT NULL varchar(255),
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (productid),
    index(discountpercent, coupencode, price)
);

CREATE TABLE resetpasswordtoken (
    token NOT NULL varchar(255),
    emailaddress NOT NULL varchar(255),
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (token),
    unique (emailaddress),
    index(emailaddress)
);

CREATE EVENT deletepasswordtokens ON SCHEDULE EVERY 30 MINUTE COMMENT 'Clear out older tokens.' DO
DELETE from
    resetpasswordtoken
WHERE
    TIMESTAMPDIFF(MINUTE, createdon, NOW()) > 30;