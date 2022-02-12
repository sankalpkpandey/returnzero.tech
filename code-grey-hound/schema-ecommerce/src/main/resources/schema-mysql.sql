DROP TABLE IF EXISTS `user`;

CREATE TABLE user (
    id bigint AUTO_INCREMENT NOT NULL,
    lastname varchar(255) NOT NULL,
    firstname varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    username varchar(255) NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    unique (username, email),
    index(username),
    index(lastname),
    index(firstname),
    index(email)
);

DROP TABLE IF EXISTS `product`;

CREATE TABLE product (
    id bigint AUTO_INCREMENT NOT NULL,
    sku varchar(255) NOT NULL,
    details longtext NOT NULL,
    category varchar(255) NOT NULL,
    quantity varchar(255) NOT NULL,
    thumbnail longtext NOT NULL,
    video longtext NOT NULL,
    imageone longtext NOT NULL,
    imagetwo longtext,
    imagethree longtext,
    imagefour longtext,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    index(sku),
    index(category)
);

DROP TABLE IF EXISTS `cart`;

CREATE TABLE cart (
    userid bigint NOT NULL,
    productid bigint NOT NULL,
    quantity int NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userid, productid)
);

DROP TABLE IF EXISTS `checkout`;

CREATE TABLE checkout (
    id BINARY(16) DEFAULT (uuid_to_bin(uuid())) NOT NULL,
    userid bigint NOT NULL,
    stage varchar(255) NOT NULL,
    outcome longtext NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    unique (userid, stage),
    index(userid),
    index(stage)
);

DROP TABLE IF EXISTS `pricing`;

CREATE TABLE pricing (
    productid bigint NOT NULL,
    price double NOT NULL,
    discount double NOT NULL,
    currency varchar(255) NOT NULL,
    coupencode varchar(255) NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (productid),
    index(discount),
    index(coupencode),
    index(price)
);

DROP TABLE IF EXISTS `resetpasswordtoken`;

CREATE TABLE resetpasswordtoken (
    token varchar(255) NOT NULL,
    email varchar(255) NOT NULL,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (token),
    unique (email),
    index(email)
);

DROP EVENT IF EXISTS deletepasswordtokens;

CREATE EVENT deletepasswordtokens ON SCHEDULE EVERY 30 MINUTE COMMENT 'Clear out older tokens.' DO
DELETE from
    resetpasswordtoken
WHERE
    TIMESTAMPDIFF(MINUTE, createdon, NOW()) > 30;

DROP TABLE IF EXISTS `configration`;

CREATE TABLE configration (
    identifier varchar(255) NOT NULL,
    jsonconfig longtext NOT NULL,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (identifier),
    unique (identifier)
);