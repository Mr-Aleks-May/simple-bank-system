# Simple Bank System Application

### Содержание:

1. Установка и запуск
2. Робота с БД.
3. Примеры запросов.  
4. API для тестирования
  
   
  

### I. Setup and config
Для создания данного проекта была использована IDE Spring Tool Suite™ 3 ([sts-3.9.11.RELEASE](https://download.springsource.com/release/STS/3.9.11.RELEASE/dist/e4.14/spring-tool-suite-3.9.11.RELEASE-e4.14.0-win32-x86_64.zip)).
Последнюю версию Вы можете загрузить с [официального сайта](https://spring.io/tools3/sts/all).


### II. Working with DB
В данном разделе будет приведена информация по настройке и работе с базой данных.
В качестве базы данных была выбрана Postgresql (v9.6).
Ниже, будут приведены запросы для создания всех необходимых таблиц и их описание.

Для доступа к базе данных (в разработаном приложении) используються следущие данные:
```
databasename: bank
username: root
password: qwerty1
```
Примечания:\
*1. Если у Вас они отличаються, то измените их в классе DBSettings.*

/********\
**Customers** - данная таблица необходима для хранения персональной информации о пользователе.\
*********/\
*CREATE TABLE customers\
(\
    id serial NOT NULL,\
    email character(48) NOT NULL,\
    password character(32) NOT NULL,\
    PRIMARY KEY (id)\
)*

/*********\
**Tokens** - в этой таблице храняться (временные) ключи доступа. Они необходимы для того, чтобы после аутентификации пользователь больше не отправлял свои email и пароль (является не очень безопастным), а получил временный токен, используя который, можно было проводить различные операции с аккаунтом (снятие, пополнение, просмотр баланса...).  

Примечания:\
*1. После успешной аутентификации старый токен становиться не действительным. То есть, в одно и тоже время, пользователь может работать только с 1 приложения\устройства.*  
**********/\
*CREATE TABLE tokens\
(\
    id serial NOT NULL,\
    customer_id integer NOT NULL,\
    token character(32) NOT NULL,\
    PRIMARY KEY (id)\
)*

/*********\
**PrimaryAccount** - храниться информация о главном счете пользователя.\
**********/\
*CREATE TABLE primary_account\
(\
    id serial NOT NULL,\
    customer_id integer NOT NULL,\
    balance numeric NOT NULL,\
    currency smallint NOT NULL,\
    PRIMARY KEY (id)\
)*

/*********\
**PrimaryTransaction** - в данной таблице находиться информация о транзакциях проведенных в главном счете пользователя.\
**********/\
*CREATE TABLE primary_account_transactions\
(\
    id serial NOT NULL,\
    customer_id integer NOT NULL,\
    name character(20),\
    type smallint NOT NULL,\
    amount numeric NOT NULL,\
    balance_after numeric NOT NULL,\
    date timestamp with time zone NOT NULL DEFAULT NOW(),\
    description character(256),\
    location character(64),\
    PRIMARY KEY (id)\
)*

### III. Examles of requests
**1. Запрос для регистрации нового клиента:**\
<**POST**> `http://localhost:8080/api/signup?email=test@test.com&password=5f4dcc3b5aa765d61d8327deb882cf99`

***Возможные ответы:***\
Параметр "status":
>0 - клиент успешно зарегистрирован.\
100 - не коректный email.\
101 - не коректный токен.\
200 - клиент с таким email уже существует.\
201 - произовшла ошибка при открытии нового счета для клиента.\
-1 - при выполнении запроса на сервере произошла ошибка.  


**2. Запрос аутентификации от клиента:**\
<**POST**> `http://localhost:8080/api/signin?email=test@test.com&password=5f4dcc3b5aa765d61d8327deb882cf99`

***Возможные ответы:***\
Параметр "status":
>0 - успешная аутентификация.\
100 - не коректный email.\
101 - не коректный токен.\
200 - клиента с указанныс email не существует.\
201 - пароли не совпадают.\
-1 - при выполнении запроса на сервере произошла ошибка.  

***Примечание:*** *пароли должны приходить в виде хеша полученого с помощью алгоритма md5, и иметь длину 32 символа.*  

Параметр "token":
>Если "status" равно 0, то будет возвращен временный ключ доступа в параметре "token", иначе данного параметра не будет в ответе.  


**3. Запрос на внесение средств на счет:**\
<**POST**> `http://localhost:8080/api/account/deposite?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwW&account=primary&amount=100.66`

***Возможные ответы:***\
Параметр "status":
>0 - указанная сумма была успешно внесена на счет.\
100 - не коректный токен.\
200 - токен недействителен.\
201 - для указаного клиента не был открыт счет (primary).\
-1 - при выполнении запроса на сервере произошла ошибка.  

Параметр "balance":
>Если "status" = 0, то возвращается текущий баланс счета. Иначе, параметра "balance" не будет.  


**4. Запрос на снятие средств со счета:**\
<**POST**> `http://localhost:8080/api/account/withdraw?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwW&account=primary&amount=75.66`

***Возможные ответы:***\
Параметр "status":
>0 - указанная сумма была успешно снята со счета.\
100 - не коректный токен.\
200 - токен недействителен.\
201 - для указаного клиента не был открыт счет (primary).\
202 - на счету не достаточно денег.\
-1 - при выполнении запроса на сервере произошла ошибка.  

Параметр "balance":
>Если "status" равен 0 или 202, то возвращается текущий баланс счета. Иначе, параметра "balance" не будет.  


**5. Запрос остатка на счету:**\
<**POST**> `http://localhost:8080/api/account/getBalance?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwW&account=primary`

***Возможные ответы:***\
Параметр "status":
>0 - остаток на балансе был отправлен в параметре "balance".\
100 - не коректный токен.\
200 - для указаного клиента не был открыт счет (primary).\
-1 - при выполнении запроса на сервере произошла ошибка.

Параметр "balance":
>Если в параметре "status" возвращено 0, то в параметре "balance" будет возвраще остаток на счету (primary account).\
Иначе, параметра "balance" не будет.


**6. Запрос на получение истории транзакций:**\
<**POST**> `http://localhost:8080/api/account/view/transactions?token=ykPQqt38yBDHeGi4OIK9obdRxtrX7rwW&account=primary&from=1.01.2020&to=2.2.2020`

***Возможные ответы:***\
Параметр "status":  
>0 - список транзакций будет возвращен в параметре "transactions" (масив).\
100 - не коректный токен.\
200 - дата в параметре from больше даты в параметре to (должно быть наоборот).\
-1 - при выполнении запроса на сервере произошла ошибка.



### IV. API requests for testing
<**GET**> `http://localhost:8080/api/test/signup?email=user@test.com&password=5f4dcc3b5aa765d61d8327deb882cf99`  
<**GET**> `http://localhost:8080/api/test/signin?email=user@test.com&password=5f4dcc3b5aa765d61d8327deb882cf99`  
<**GET**> `http://localhost:8080/api/test/account/deposite?token=9vSTQyHOSslYhJZK01HqSuUgGwNEVb8g&account=primary&amount=100`  
<**GET**> `http://localhost:8080/api/test/account/withdraw?token=9vSTQyHOSslYhJZK01HqSuUgGwNEVb8g&account=primary&amount=75.06`  
<**GET**> `http://localhost:8080/api/test/account/getBalance?token=9vSTQyHOSslYhJZK01HqSuUgGwNEVb8g&account=primary`  
<**GET**> `http://localhost:8080/api/test/account/view/transactions?token=9vSTQyHOSslYhJZK01HqSuUgGwNEVb8g&account=primary&from=1.1.2020&to=2.2.2020`  