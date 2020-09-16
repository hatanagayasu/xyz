create table soa
(
  id int unsigned not null auto_increment primary key,
  origin char(255) not null,
  ns char(255) not null,
  mbox char(255) not null,
  serial int unsigned not null default 1,
  refresh int unsigned not null default 28800,
  retry int unsigned not null default 7200,
  expire int unsigned not null default 604800,
  minimum int unsigned not null default 86400,
  ttl int unsigned not null default 86400,
  active ENUM('Y','N') NOT NULL
);

create table rr
(
  id int unsigned not null auto_increment primary key,
  zone int unsigned not null,
  name char(64) not null,
  type enum('a','aaaa','cname','hinfo','mx','ns','ptr','rp','srv','txt') not null,
  data char(128) not null,
  aux int unsigned not null,
  ttl int unsigned not null default 86400,
  active ENUM('Y','N') NOT NULL
);
