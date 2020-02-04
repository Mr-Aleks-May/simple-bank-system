DROP TABLE IF EXISTS customers, tokens, primary_account, primary_account_transactions;
DROP TABLE IF EXISTS customers, tokens, accounts, transactions, currencys;

/********
Customers - данная таблица необходима для хранения персональной информации о пользователе.
*********/
CREATE TABLE customers
(
id serial NOT NULL,
email character(48) NOT NULL,
password character(32) NOT NULL,
PRIMARY KEY (id)
);

/*********
Tokens - в этой таблице храняться (временные) ключи доступа. Они необходимы для того, чтобы после аутентификации пользователь больше не отправлял свои email и пароль (является не очень безопастным), а получил временный токен, используя который, можно было проводить различные операции с аккаунтом (снятие, пополнение, просмотр баланса...).

Примечания:
1. После успешной аутентификации старый токен становиться не действительным. То есть, в одно и тоже время, пользователь может работать только с 1 приложения\устройства.
**********/
CREATE TABLE tokens
(
id serial NOT NULL,
customer_id integer NOT NULL,
token character(32) NOT NULL,
PRIMARY KEY (id)
);

/*********
Accounts - храниться информация о счетах пользователей.
**********/
CREATE TABLE accounts
(
id bigserial NOT NULL,
customer_id integer NOT NULL,
currency smallint NOT NULL,
balance numeric NOT NULL,
PRIMARY KEY (id)
);

/*********
Transaction - в данной таблице находиться информация о транзакциях проведенных пользователями.
**********/
CREATE TABLE transactions
(
id serial NOT NULL,
customer_id integer NOT NULL,
account_id bigint NOT NULL,
name character(20),
type smallint NOT NULL,
currency smallint NOT NULL,
amount numeric NOT NULL,
balance_after numeric NOT NULL,
date timestamp without time zone NOT NULL DEFAULT NOW(),
description character(256),
location character(64),
PRIMARY KEY (id)
);

/*********
Currencys - в этой таблице находиться дополнительная информация про валюты в которых может быть открыт счет.
**********/
CREATE TABLE currencys
(
   code smallint NOT NULL,
   short character(20),
   PRIMARY KEY (code)
);