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
    details longtext NOT NULL,
    category varchar(255) NOT NULL,
    featured boolean,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name varchar(255) NOT NULL,
    features longtext NOT NULL,
    subcategory varchar(255),
    PRIMARY KEY (id),
    index(category),
    index(name),
    index(featured),
    index(subcategory),
    unique (name)
);


DROP TABLE IF EXISTS `cart`;

CREATE TABLE cart (
    userid bigint NOT NULL,
    productoptionid bigint NOT NULL,
    quantity int NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userid, productoptionid)
);

DROP TABLE IF EXISTS `checkout`;

CREATE TABLE checkout (
    id varchar(64) NOT NULL,
    userid bigint NOT NULL,
    guestemail varchar(255) ,
    stage varchar(255) NOT NULL,
    outcome longtext NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    index(userid),
    index(stage)
);

DROP TABLE IF EXISTS `pricing`;

CREATE TABLE pricing (
    productoptionid  bigint NOT NULL,
    productid bigint NOT NULL,
    price double NOT NULL,
    discount double NOT NULL,
    currency varchar(255) NOT NULL,
    coupencode varchar(255) NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (productoptionid),
    index(discount),
    index(coupencode),
    index(price),
    index(productid),
    index(productoptionid)
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

DROP TABLE IF EXISTS `ratings`;

CREATE TABLE ratings (
    userid bigint NOT NULL,
    productid bigint NOT NULL,
    rating double NOT NULL DEFAULT 0.0,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userid, productid),
    index(rating)
);

DROP TABLE IF EXISTS `reviews`;

CREATE TABLE reviews (
    userid bigint NOT NULL,
    productid bigint NOT NULL,
    title varchar(255) NOT NULL,
    location varchar(255) NOT NULL,
    review text NOT NULL,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userid, productid),
    index(title)
);

DROP TABLE IF EXISTS `reviewsummary`;
CREATE TABLE reviewsummary (
    productid bigint NOT NULL,
    avgrating double  ,
    reviewcount int ,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (productid),
    index(reviewcount),
    index(avgrating)
);



DROP TRIGGER IF EXISTS reviewsummarytrigger_reviews;
delimiter //
CREATE TRIGGER reviewsummarytrigger_reviews AFTER INSERT ON reviews
       FOR EACH ROW
       BEGIN
         insert into reviewsummary values 
         	(NEW.productid ,
         	(select avg(rating) from ratings r where r.productid = NEW.productid group by r.productid),
         	(select count(review)   from reviews re  where re.productid = NEW.productid  group by re.productid ),
         	CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) 
         	ON DUPLICATE KEY
         	UPDATE 
         	avgrating = (select avg(rating) from ratings r where r.productid = NEW.productid group by r.productid) ,
            reviewcount = (select count(review)   from reviews  re where re.productid = NEW.productid  group by re.productid); 
       END//
delimiter ;
      
DROP TRIGGER IF EXISTS reviewsummarytrigger_ratings;
delimiter //
CREATE TRIGGER reviewsummarytrigger_ratings AFTER INSERT ON ratings
       FOR EACH ROW
       BEGIN
         insert into reviewsummary values 
         	(NEW.productid ,
         	(select avg(rating) from ratings r where r.productid = NEW.productid group by r.productid),
         	(select count(review)   from reviews re  where re.productid = NEW.productid  group by re.productid ),
         	CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) 
         	ON DUPLICATE KEY
         	UPDATE 
         	avgrating = (select avg(rating) from ratings r where r.productid = NEW.productid group by r.productid) ,
            reviewcount = (select count(review)   from reviews  re where re.productid = NEW.productid  group by re.productid); 
       END//
delimiter ;


DROP TABLE IF EXISTS `address`;
CREATE TABLE address (
    id bigint AUTO_INCREMENT NOT NULL,
    userid bigint NOT NULL,
    defaultaddress boolean,
    type varchar(255) NOT NULL,
    person varchar(255) NOT NULL,
    lineone varchar(255) NOT NULL,
    linetwo varchar(255) NOT NULL,
    city varchar(255) NOT NULL,
    state varchar(255) NOT NULL,
    countrycode varchar(255) NOT NULL,
    pincode varchar(255) NOT NULL,
    landmark varchar(255) NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    index(city),
    index(defaultaddress),
    index(state),
    index(pincode),
    index(countrycode),
    index(person),
    index(userid),
    unique (userid, type)
    
);

DROP TABLE IF EXISTS `productoptions`;
CREATE TABLE productoptions (
    id bigint AUTO_INCREMENT NOT NULL,
    productid bigint NOT NULL,
    sku varchar(255) NOT NULL,
    quantity varchar(255) NOT NULL,
    net_weight double NOT NULL,
    gross_weight double NOT NULL,
    length double NOT NULL,
    width double NOT NULL,
    height double NOT NULL,
    thumbnail longtext NOT NULL,
    imageone longtext NOT NULL,
    imagetwo longtext,
    imagethree longtext,
    imagefour longtext,
    PRIMARY KEY (id),
    index(`sku`),
    unique (productid,quantity,net_weight,gross_weight,length,width,height),
    unique (sku),
    index(quantity),
    index(net_weight),
    index(gross_weight),
    index(length),
    index(width),
    index(height)
);


DROP TABLE IF EXISTS `websitelinks`;
CREATE TABLE websitelinks (
    linkname varchar(255) NOT NULL,
    linkaddress text NOT NULL,
    updatedon TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createdon TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (linkname)
);

DROP TRIGGER IF EXISTS productinserttoreview;
delimiter //
CREATE TRIGGER productinserttoreview AFTER INSERT ON product
       FOR EACH ROW
       BEGIN
         insert into reviewsummary values (NEW.id  , 0.0, 0, CURRENT_TIMESTAMP,CURRENT_TIMESTAMP) 
         	ON DUPLICATE KEY
         	UPDATE productid=productid;
       END//
delimiter ;

DROP TRIGGER IF EXISTS productdeletetoreview;
delimiter //
CREATE TRIGGER productdeletetoreview AFTER DELETE ON product
       FOR EACH ROW
       BEGIN
         delete from reviewsummary r where r.productid = OLD.id ;
       END//
delimiter ;